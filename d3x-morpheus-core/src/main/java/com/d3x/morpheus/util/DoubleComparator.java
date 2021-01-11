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
package com.d3x.morpheus.util;

import java.util.Comparator;

/**
 * Defines an interface for comparing double precision values while
 * allowing for a finite tolerance, which may be expressed in absolute
 * or relative terms.
 *
 * @author Scott Shaffer
 */
public interface DoubleComparator extends Comparator<Double> {
    /**
     * Compares two double precision values, allowing for the finite
     * tolerance of this comparator.
     *
     * @param x the first value to compare.
     * @param y the second value to compare.
     *
     * @return an integer less than, equal to, or greater than zero
     * according to whether {@code x} is considered to be less than,
     * equal to, or greater than {@code y} by this comparator.
     */
    int compare(double x, double y);

    /**
     * Returns a comparator with the default fixed tolerance (suitable
     * for most situations unless the values being compared are extreme
     * in magnitude).
     */
    DoubleComparator FIXED_DEFAULT = FixedDoubleComparator.DEFAULT;

    /**
     * Returns a comparator with a fixed tolerance, independent of the
     * magnitudes of the values being compared.
     *
     * @param tolerance the fixed tolerance for the comparator.
     */
    static DoubleComparator fixed(double tolerance) {
        return FixedDoubleComparator.withTolerance(tolerance);
    }

    /**
     * Compares two double precision values, allowing for the finite
     * tolerance defined for this comparator.
     *
     * @param x the first value to compare.
     * @param y the second value to compare.
     *
     * @return an integer less than, equal to, or greater than zero
     * according to whether {@code x} is considered to be less than,
     * equal to, or greater than {@code y} by this comparator.
     *
     * @throws NullPointerException if either argument is {@code null}.
     */
    @Override
    default int compare(@lombok.NonNull Double x, @lombok.NonNull Double y) {
        return compare(x.doubleValue(), y.doubleValue());
    }

    /**
     * Tests two double precision values for (near) equality.
     *
     * @param x the first value to compare.
     * @param y the second value to compare.
     *
     * @return {@code true} iff the absolute value of the difference
     * between the arguments is less than or equal to the tolerance of
     * this comparator.
     */
    default boolean equals(double x, double y) {
        return compare(x, y) == 0;
    }

    /**
     * Tests two double precision arrays for (near) equality.
     *
     * @param x the first array to compare.
     * @param y the second array to compare.
     *
     * @return {@code true} iff the arrays have equal length and this
     * comparator determines that all corresponding elements are equal.
     */
    default boolean equals(double[] x, double[] y) {
        if (x.length != y.length)
            return false;

        for (int i = 0; i < x.length; i++) {
            if (!equals(x[i], y[i]))
                return false;
        }

        return true;
    }

    /**
     * Tests two double precision matrices for (near) equality.
     *
     * @param x the first matrix to compare.
     * @param y the second matrix to compare.
     *
     * @return {@code true} iff the matrices have equal dimensions and this
     * comparator determines that all corresponding elements are equal.
     */
    default boolean equals(double[][] x, double[][] y) {
        if (x.length != y.length)
            return false;

        for (int i = 0; i < x.length; i++) {
            if (!equals(x[i], y[i]))
                return false;
        }

        return true;
    }

    /**
     * Determines whether a double precision value is negative (by an
     * amount greater than the tolerance of this comparator).
     *
     * @param x the value to test.
     *
     * @return {@code true} iff the input value is less than zero by
     * an amount greater than the tolerance of this comparator.
     */
    default boolean isNegative(double x) {
        return compare(x, 0.0) < 0;
    }

    /**
     * Determines whether a double precision value is positive (by an
     * amount greater than the tolerance of this comparator).
     *
     * @param x the value to test.
     *
     * @return {@code true} iff the input value is greater than zero by
     * an amount greater than the tolerance of this comparator.
     */
    default boolean isPositive(double x) {
        return compare(x, 0.0) > 0;
    }

    /**
     * Determines whether a double precision value is zero within the
     * tolerance of this comparator.
     *
     * @param x the value to test.
     *
     * @return {@code true} iff the input value is equal to zero within
     * the tolerance of this comparator.
     */
    default boolean isZero(double x) {
        return compare(x, 0.0) == 0;
    }

    /**
     * Determines whether a double precision value differs from zero (by
     * an amount greater than the tolerance of this comparator).
     *
     * @param x the value to test.
     *
     * @return {@code true} iff the absolute value of the input argument
     * is greater than the tolerance of this comparator.
     */
    default boolean isNonZero(double x) {
        return compare(x, 0.0) != 0;
    }

    /**
     * Determines whether a double precision value is greater than or equal
     * to zero (by an amount exceeding the tolerance of this comparator).
     *
     * @param x the value to test.
     *
     * @return {@code true} iff the input value is greater than or equal to
     * zero by an amount exceeding the tolerance of this comparator.
     */
    default boolean isNonNegative(double x) {
        return compare(x, 0.0) >= 0;
    }

    /**
     * Determines whether a double precision value is less than or equal
     * to zero (by an amount exceeding the tolerance of this comparator).
     *
     * @param x the value to test.
     *
     * @return {@code true} iff the input value is less than or equal to
     * zero by an amount exceeding the tolerance of this comparator.
     */
    default boolean isNonPositive(double x) {
        return compare(x, 0.0) <= 0;
    }

    /**
     * Returns the sign of a double precision value.
     *
     * @param x the value to test.
     *
     * @return {@code -1} if the input value is negative (in excess
     * of the comparison tolerance), {@code 0} if the input value is
     * equal to zero (within the comparison tolerance), or {@code +1}
     * if the input value is positive (in excess of the comparison
     * tolerance).
     */
    default int sign(double x) {
        if (isNegative(x))
            return -1;
        else if (isPositive(x))
            return 1;
        else
            return 0;
    }
}
