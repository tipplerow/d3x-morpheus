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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import com.d3x.morpheus.series.DoubleSeries;

/**
 * Defines a single linear equality constraint on the coefficients estimated
 * in a linear regression.
 *
 * @param <C> the runtime type of the regressor (column) variables.
 *
 * @author Scott Shaffer
 */
@AllArgsConstructor
public final class RegressionConstraint<C> {
    /**
     * A unique name for the constraint.
     */
    @Getter @NonNull
    private final String name;

    /**
     * The right-hand side value for the linear equality constraint.
     */
    @Getter
    private final double value;

    /**
     * The left-hand side terms for the linear equality constraint
     * (the coefficients multiplying the regression coefficients).
     */
    @Getter @NonNull
    private final DoubleSeries<C> terms;

    /**
     * Returns a list of the regressors affected by this constraint.
     * @return a list of the regressors affected by this constraint.
     */
    public List<C> listRegressors() {
        return terms.listKeys();
    }
}
