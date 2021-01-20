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

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;

/**
 * Encapsulates the parameters for a constrained linear regression model.
 *
 * @param <R> the runtime type of the observation keys.
 * @param <C> the runtime type of the regressor and regressand keys.
 *
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public final class ConstrainedRegressionResult<R, C> {
    private final DataFrame<ConstrainedRegressionField, C> betaCoefficients;
    private final DataFrame<ConstrainedRegressionField, String> dualValues;
    private final DataFrame<ConstrainedRegressionField, R> fittedValues;
    private final DataFrame<ConstrainedRegressionField, R> residuals;

    /**
     * Creates a new constrained regression result set.
     *
     * @param betaCoefficients a single-column DataFrame of regression coefficients with
     *                         row key equal to {@code ConstrainedRegressionField.BETA}.
     *
     * @param dualValues       a single-column DataFrame containing the dual values for the
     *                         linear constraints in the model (indexed by constraint key)
     *                         with row key {@code ConstrainedRegressionField.DUAL}.
     *
     * @param fittedValues     a single-row DataFrame containing the fitted values, with the
     *                         column key equal to {@code ConstrainedRegressionField.FITTED}.
     *
     * @param residuals        a single-row DataFrame containing the residuals, with column
     *                         key equal to {@code ConstrainedRegressionField.RESIDUAL}.
     */
    public ConstrainedRegressionResult(DataFrame<ConstrainedRegressionField, C> betaCoefficients,
                                       DataFrame<ConstrainedRegressionField, String> dualValues,
                                       DataFrame<ConstrainedRegressionField, R> fittedValues,
                                       DataFrame<ConstrainedRegressionField, R> residuals) {
        this.betaCoefficients = betaCoefficients;
        this.dualValues = dualValues;
        this.fittedValues = fittedValues;
        this.residuals = residuals;
        validateResult();
    }

    private void validateResult() {
        betaCoefficients.requireRow(ConstrainedRegressionField.BETA);
        betaCoefficients.requireRowCount(1);

        dualValues.requireRow(ConstrainedRegressionField.DUAL);
        dualValues.requireRowCount(1);

        fittedValues.requireRow(ConstrainedRegressionField.FITTED);
        fittedValues.requireRowCount(1);

        residuals.requireRow(ConstrainedRegressionField.RESIDUAL);
        residuals.requireRowCount(1);

        if (!residuals.listColumnKeys().equals(fittedValues.listColumnKeys()))
            throw new DataFrameException("Residual and fitted value keys do not match.");
    }

    /**
     * Returns the value of the regression coefficient for a particular regressor.
     *
     * @param regressor the regressor key of interest.
     *
     * @return the value of the regression coefficient for the specified regressor.
     *
     * @throws RuntimeException unless the regressor key is valid.
     */
    public double getBetaCoefficient(C regressor) {
        return betaCoefficients.getDouble(ConstrainedRegressionField.BETA, regressor);
    }

    /**
     * Returns a single-column DataFrame containing the regression coefficients.
     * The row key is {@code ConstrainedRegressionField.BETA}.
     *
     * @return a single-column DataFrame containing the regression coefficients.
     */
    public DataFrame<ConstrainedRegressionField, C> getBetaCoefficients() {
        return betaCoefficients;
    }

    /**
     * Returns a single-column DataFrame containing the dual values for the
     * linear constraints in the model (indexed by the constraint key). The
     * row key is {@code ConstrainedRegressionField.DUAL}.
     *
     * @return a single-column DataFrame containing the dual values for the
     *         linear constraints in the model (indexed by the constraint key).
     */
    public DataFrame<ConstrainedRegressionField, String> getDualValues() {
        return dualValues;
    }

    /**
     * Returns a single-column DataFrame containing the fitted values for the model,
     * equal to {@code Ax}, where {@code A} is the design matrix of regressors and
     * {@code x} is the vector of regression coefficients.  Note that fitted values
     * are computed for all rows in the observation frame, even those that were
     * excluded from the regression.  The column key is {@code ConstrainedRegressionField.FITTED}.
     *
     * @return a single-column DataFrame containing the fitted values for the model.
     */
    public DataFrame<ConstrainedRegressionField, R> getFittedValues() {
        return fittedValues;
    }

    /**
     * Returns a single-column DataFrame containing the residuals for the model,
     * equal to {@code Ax - b}, where {@code A} is the design matrix of regressors,
     * {@code x} is the vector of regression coefficients, and {@code b} is the
     * vector of observations.  Note that residuals are computed for all rows in
     * the observation frame, even those that were excluded from the regression.
     * The column key is {@code ConstrainedRegressionField.RESIDUAL}.
     *
     * @return a single-column DataFrame containing the residuals for the model.
     */
    public DataFrame<ConstrainedRegressionField, R> getResiduals() {
        return residuals;
    }
}
