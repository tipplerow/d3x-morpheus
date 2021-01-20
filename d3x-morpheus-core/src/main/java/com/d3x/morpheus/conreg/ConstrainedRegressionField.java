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

/**
 * Enumerates the parameter fields contained in a regression result.
 *
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public enum ConstrainedRegressionField {
    /**
     * Least-squares regression coefficients.
     */
    BETA,

    /**
     * Dual values (Lagrange multipliers) for the linear equality constraints
     * on the regression coefficients.
     */
    DUAL,

    /**
     * The fitted or predicted observation values, equal to {@code A * x}, where
     * {@code A} is the design matrix of regressors and {@code x} is the vector
     * of regression coefficients (betas).
     */
    FITTED,

    /**
     * The regression residuals, equal to {@code A * x - b}, where {@code A} is
     * the design matrix of regressors, {@code x} is the vector of regression
     * coefficients (betas), and {@code b} is the vector of observations.
     */
    RESIDUAL;
}
