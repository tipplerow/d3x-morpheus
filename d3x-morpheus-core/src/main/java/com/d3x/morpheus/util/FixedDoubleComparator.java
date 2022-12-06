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

/**
 * Compares double precision values using a fixed tolerance, independent
 * of the magnitudes of the values being compared.  The tolerance is
 * assigned during object creation and cannot be changed.
 *
 * @author Scott Shaffer
 */
public final class FixedDoubleComparator implements DoubleComparator {
    /** The fixed tolerance for this comparator. */
    @lombok.Getter
    private final double tolerance;

    private FixedDoubleComparator(double tolerance) {
        if (tolerance <= 0.0)
            throw new IllegalArgumentException("Tolerance must be strictly positive.");

        this.tolerance = tolerance;
    }
    /**
     * The fixed tolerance for the default comparator, suitable for most
     * situations unless the values being compared are extreme in their
     * magnitude.
     */
    public static final double DEFAULT_TOLERANCE = 1.0E-12;

    /**
     * A comparator with the default fixed tolerance.
     */
    public static final FixedDoubleComparator DEFAULT = new FixedDoubleComparator(DEFAULT_TOLERANCE);

    /**
     * Returns a comparator with a fixed tolerance.
     *
     * @param tolerance the fixed comparison tolerance: double precision
     *                  values must differ by more than this tolerance to
     *                  be considered unequal.
     *
     * @return a comparator with the specified tolerance.
     *
     * @throws IllegalArgumentException unless the tolerance is positive.
     */
    public static FixedDoubleComparator withTolerance(double tolerance) {
        return new FixedDoubleComparator(tolerance);
    }

    /**
     * Compares two finite double precision values with an absolute tolerance.
     *
     * @param x          the first value to compare.
     * @param y          the second value to compare.
     * @param tolerance  the absolute comparison tolerance.
     *
     * @return an integer less than zero if {@code x < y - tolerance},
     * an integer greater than zero if {@code x > y + tolerance}, or
     * zero otherwise ({@code |x - y| <= tolerance}).
     */
    public static int compareFinite(double x, double y, double tolerance) {
        double diff = x - y;

        if (diff < -tolerance)
            return -1;
        else if (diff > tolerance)
            return 1;
        else
            return 0;
    }

    @Override
    public int compareFinite(double x, double y) {
        return compareFinite(x, y, tolerance);
    }

    @Override
    public double nextDown(double x) {
        return Math.nextDown(x - tolerance);
    }

    @Override
    public double nextUp(double x) {
        return Math.nextUp(x + tolerance);
    }
}
