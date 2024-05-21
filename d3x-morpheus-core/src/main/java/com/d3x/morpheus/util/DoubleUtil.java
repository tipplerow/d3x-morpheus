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
package com.d3x.morpheus.util;

/**
 * Provides static utility methods operating on double values.
 *
 * @author Scott Shaffer
 */
public final class DoubleUtil {
    /**
     * A named constant for {@code Math.log(2.0)}.
     */
    public static final double LOG2 = Math.log(2.0);

    /**
     * A named constant for {@code Math.sqrt(2.0)}.
     */
    public static final double SQRT2 = Math.sqrt(2.0);

    /**
     * A named constant for {@code Math.sqrt(2.0 * Math.PI)}.
     */
    public static final double SQRT_TWO_PI = Math.sqrt(2.0 * Math.PI);

    /**
     * Bounds a floating-point value between lower and upper bounds.
     *
     * @param value the value to bound.
     * @param lower the lower bound.
     * @param upper the upper bound.
     *
     * @return the bounded value.
     */
    public static double bound(double value, double lower, double upper) {
        if (value < lower) {
            return lower;
        }
        else if (value > upper) {
            return upper;
        }
        else {
            return value;
        }
    }

    /**
     * Computes the floating-point ratio of two integers.
     *
     * @param numer the integral numerator.
     * @param denom the integral denominator.
     *
     * @return the floating-point ratio of the two integers.
     */
    public static double ratio(int numer, int denom) {
        // Must cast to double because int / int is computed using
        // integer arithmetic with a remainder...
        return ((double) numer) / ((double) denom);
    }

    /**
     * Rounds a double precision value to the nearest whole number of units
     * with a given magnitude.
     *
     * <p>For example:
     * <pre>
     *     round(0.234567, 0.01) == 0.23
     *     round(0.234567, 0.0001) == 0.2346
     *     round(1.234567, 0.25) == 1.25
     * </pre>
     * </p>
     *
     * @param value the value to round.
     * @param unit  the positive unit size.
     *
     * @return the input value rounded to the nearest whole number of units.
     *
     * @throws RuntimeException unless the unit size is positive.
     */
    public static double round(double value, double unit) {
        if (unit > 0.0)
            return unit * Math.round(value / unit);
        else
            throw new MorpheusException("Unit must be positive.");
    }
}
