/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.conreg;

import java.util.List;
import java.util.Set;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.series.DoubleSeriesBuilder;
import com.d3x.morpheus.vector.D3xVector;

/**
 * Defines a linear regression constraint for a set of <em>category variables</em>.
 *
 * <p>Category variables (often called dummy variables) are used to place observations
 * into discrete groups, like countries or industries in a model of asset returns. The
 * category variables must sum to one for each observation.  They are often restricted
 * to be exactly zero or one, but that restriction may be relaxed for observations that
 * span multiple categories (for a company that conducts business across more than one
 * industry, for example).</p>
 *
 * <p>The model must contain an added intercept or a regressor variable that serves as
 * an intercept (a column of ones). The regression coefficients (betas) for the category
 * variables may be interpreted as offsets relative to the overall level quantified by
 * the intercept. The regression coefficients (betas) will sum to zero when weighted by
 * the regression weights.</p>
 *
 * <p>Given a design matrix (or DataFrame) of observations {@code A} and a vector (or
 * DoubleSeries) of observation weights {@code w}, the constraint coefficient term for
 * category {@code k} is computed as the weighted column sum over {@code w(i) * A(i,k)},
 * where {@code i} is the row index.</p>
 *
 * @param <R> the runtime type of the observation rows.
 * @param <C> the runtime type of the category regressor variables.
 *
 * @author Scott Shaffer
 */
public final class CategoryConstraint<R,C> {
    private final String categoryName;
    private final Set<C> regressorKeys;
    private final DataFrame<R,C> observationFrame;
    private final DoubleSeries<R> observationWeights;

    private final List<R> rowKeys;
    private final D3xVector weightVector;

    // All category constraints have a zero right-hand side value...
    private static final double RHS_VALUE = 0.0;

    private CategoryConstraint(String categoryName,
                               Set<C> regressorKeys,
                               DataFrame<R,C> observationFrame,
                               DoubleSeries<R> observationWeights) {
        this.categoryName = categoryName;
        this.regressorKeys = regressorKeys;
        this.observationFrame = observationFrame;
        this.observationWeights = observationWeights;

        this.rowKeys = observationFrame.listRowKeys();
        this.weightVector = D3xVector.copyOf(observationWeights, rowKeys, 0.0).normalize();

        validateData();
    }

    private void validateData() {
        observationWeights.requireKeys(rowKeys);
        observationFrame.requireNumericColumns(regressorKeys);
    }

    /**
     * Creates a constraint for a set of category variables.
     *
     * @param categoryName the unique name for the category (e.g., COUNTRY).
     *
     * @param regressorKeys the column keys for the category regressor variables.
     *
     * @param observationFrame a DataFrame of observations containing the values of the
     *                         category regressor variables (the dummy variables or the
     *                         partial category loadings); additional regressors may be
     *                         present as well but are ignored.
     *
     * @param observationWeights the weight of each observation in the constraint terms.
     *
     * @return a new regression constraint for the specified category.
     *
     * @throws RuntimeException unless the observation frame contains columns for all
     * category variables are regressors in this model and the weight series contains
     * values for all observations in the frame.
     */
    public static <R,C> RegressionConstraint<C> build(String categoryName,
                                                      Set<C> regressorKeys,
                                                      DataFrame<R,C> observationFrame,
                                                      DoubleSeries<R> observationWeights) {
        CategoryConstraint<R,C> constraint =
                new CategoryConstraint<>(categoryName, regressorKeys, observationFrame, observationWeights);

        return constraint.build();
    }

    private RegressionConstraint<C> build() {
        return new RegressionConstraint<C>(categoryName, RHS_VALUE, buildTerms());
    }

    private DoubleSeries<C> buildTerms() {
        DoubleSeriesBuilder<C> builder = DoubleSeries.builder(observationFrame.cols().keyClass());

        for (C regressorKey : regressorKeys)
            builder.putDouble(regressorKey, computeTerm(regressorKey));

        return builder.build();
    }

    private double computeTerm(C regressorKey) {
        return computeTerm(observationFrame.col(regressorKey));
    }

    private double computeTerm(DataFrameColumn<R,C> column) {
        double term = 0.0;

        for (int rowIndex = 0; rowIndex < rowKeys.size(); ++rowIndex)
            term += weightVector.get(rowIndex) * column.getDoubleAt(rowIndex);

        return term;
    }
}
