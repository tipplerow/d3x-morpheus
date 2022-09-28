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
package com.d3x.morpheus.root;

import java.util.function.DoubleUnaryOperator;

import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.DoubleInterval;
import com.d3x.morpheus.util.MorpheusException;

import lombok.NonNull;

/**
 * Provides a base class for univariate root-finding algorithms.
 *
 * @author Scott Shaffer
 */
public abstract class UnivariateRootFinder {
    private static final int BRACKET_MAX_ITER = 100;
    private static final double BRACKET_FACTOR = 1.6;

    /**
     * Brackets the root of a univariate function.
     *
     * @param function the function to solve.
     * @param interval an initial guess for bounding interval.
     *
     * @return an interval known to contain the root.
     *
     * @throws RuntimeException unless a valid interval can be found.
     */
    public static DoubleInterval bracket(@NonNull DoubleUnaryOperator function, @NonNull DoubleInterval interval) {
        if (!DoubleComparator.DEFAULT.isPositive(interval.getWidth()))
            throw new MorpheusException("Initial interval must have finite width.");

        var lower = interval.getLower();
        var upper = interval.getUpper();

        var funcLower = function.applyAsDouble(lower);
        var funcUpper = function.applyAsDouble(upper);

        for (int iter = 0; iter < BRACKET_MAX_ITER; ++iter) {
            if (Math.signum(funcLower) * Math.signum(funcUpper) < 0.0) {
                return DoubleInterval.open(lower, upper);
            }
            else if (Math.abs(funcLower) < Math.abs(funcUpper)) {
                lower -= BRACKET_FACTOR * (upper - lower);
                funcLower = function.applyAsDouble(lower);
            }
            else {
                upper += BRACKET_FACTOR * (upper - lower);
                funcUpper = function.applyAsDouble(upper);
            }
        }

        throw new MorpheusException("No root in interval [%f, %f].", lower, upper);
    }

    /**
     * Finds the root of a univariate function.
     *
     * @param function the function to solve.
     * @param interval an interval known to contain the root.
     *
     * @return the root of the function.
     */
    public double solve(@NonNull DoubleUnaryOperator function, @NonNull DoubleInterval interval) {
        return solve(function, interval, interval.getMidPoint());
    }

    /**
     * Finds the root of a univariate function.
     *
     * @param function the function to solve.
     * @param interval an interval known to contain the root.
     * @param initial  an initial guess for the location of the root.
     *
     * @return the root of the function.
     */
    public abstract double solve(DoubleUnaryOperator function, DoubleInterval interval, double initial);
}
