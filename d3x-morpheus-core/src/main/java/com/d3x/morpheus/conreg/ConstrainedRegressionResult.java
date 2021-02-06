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
import com.d3x.morpheus.series.DoubleSeries;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

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
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ConstrainedRegressionResult<R, C> {
    /**
     * A DoubleSeries containing the regression coefficients.
     */
    @Getter @NonNull
    private final DoubleSeries<C> betaCoefficients;

    /**
     * A DoubleSeries containing the dual values for the linear
     * equality constraints on the regression coefficients.
     */
    @Getter @NonNull
    private final DoubleSeries<String> dualValues;

    /**
     * A DoubleSeries containing the fitted values for the model, equal to
     * {@code Ax}, where {@code A} is the design matrix of regressors and
     * {@code x} is the vector of regression coefficients.  Note that the
     * fitted values are computed for all rows in the observation frame,
     * even those that were excluded from the regression.
     */
    @Getter @NonNull
    private final DoubleSeries<R> fittedValues;

    /**
     * A DoubleSeries containing the regression residual values, equal to
     * {@code Ax - b}, where {@code A} is the design matrix of regressors,
     * {@code x} is the vector of regression coefficients, and {@code b} is
     * the vector of observations.  Note that residuals are computed for all
     *  rows in the observation frame, even those that were excluded from the
     *  regression.
     */
    @Getter @NonNull
    private final DoubleSeries<R> residuals;

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
        return betaCoefficients.getDouble(regressor);
    }
}
