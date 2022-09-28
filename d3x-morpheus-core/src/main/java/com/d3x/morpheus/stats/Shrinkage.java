/*
 * Copyright (C) 2014-2022 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.stats;

import com.d3x.morpheus.util.DoubleInterval;

import lombok.Builder;
import lombok.Value;

/**
 * Shrinks estimated statistics toward a Bayesian prior value.
 *
 * @author Scott Shaffer
 */
@Value
@Builder
public class Shrinkage {
    /**
     * The Bayesian prior statistic value.
     */
    double prior;

    /**
     * The shrinkage factor on the interval {@code [0.0, 1.0]}, where
     * {@code 0.0} is no shrinkage (the empirical sample value is used
     * as is) and {@code 1.0} is complete shrinkage (the prior value is
     * always used).
     */
    double shrinkage;

    /**
     * Shrinks an empirical statistic value.
     *
     * @param sample a statistic value determined empirically from sample data.
     *
     * @return the shrunk statistic value.
     */
    public double shrink(double sample) {
        return shrinkage * prior + (1.0 - shrinkage) * sample;
    }

    /**
     * Ensures that the shrinkage factor is within its fractional bounds.
     *
     * @return this object, for operator chaining.
     *
     * @throws RuntimeException unless the shrinkage factor is within its
     * fractional bounds.
     */
    public Shrinkage validate() {
        DoubleInterval.FRACTIONAL.validate(shrinkage, "Shrinkage must be fractional.");
        return this;
    }
}
