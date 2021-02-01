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
 * Compares double precision values using a relative tolerance which is a
 * function of the magnitudes of the values being compared.
 *
 * <p><b>Tolerance factor.</b> Relative comparators are created with a
 * tolerance factor, which is used to compute an absolute tolerance for
 * each pair of double precision values being compared.  Let {@code x}
 * and {@code y} be two double values and {@code tol} be the tolerance
 * factor. The absolute tolerance used for the comparison is then:
 * {@code max(tol, tol * (|x| + |y|)}.
 *
 * @author Scott Shaffer
 */
public final class RelativeDoubleComparator implements DoubleComparator {
    /** The fixed tolerance factor for this comparator. */
    @lombok.Getter
    private final double toleranceFactor;

    private RelativeDoubleComparator(double toleranceFactor) {
        if (toleranceFactor <= 0.0)
            throw new IllegalArgumentException("Tolerance factor must be strictly positive.");

        this.toleranceFactor = toleranceFactor;
    }
    /**
     * The tolerance factor for the default comparator, suitable for most
     * situations.
     */
    public static final double DEFAULT_TOLERANCE_FACTOR = 1.0E-12;

    /**
     * A comparator with the default fixed tolerance.
     */
    public static final RelativeDoubleComparator DEFAULT = new RelativeDoubleComparator(DEFAULT_TOLERANCE_FACTOR);

    /**
     * Returns a relative comparator with a fixed tolerance factor.
     *
     * @param toleranceFactor the fixed tolerance factor (see class header comments).
     *
     * @return a relative comparator with the specified tolerance factor.
     *
     * @throws IllegalArgumentException unless the tolerance factor is positive.
     */
    public static RelativeDoubleComparator withToleranceFactor(double toleranceFactor) {
        return new RelativeDoubleComparator(toleranceFactor);
    }

    /**
     * Computes the absolute comparison tolerance for two double precision values
     * and a relative tolerance factor.
     *
     * @param x   the first value to compare.
     * @param y   the second value to compare.
     * @param fac the relative tolerance factor.
     *
     * @return {@code max(fac, fac * (|x| + |y|)}.
     */
    public static double computeTolerance(double x, double y, double fac) {
        return Math.max(fac, fac * (Math.abs(x) + Math.abs(y)));
    }

    @Override
    public int compareFinite(double x, double y) {
        return FixedDoubleComparator.compareFinite(x, y, computeTolerance(x, y, toleranceFactor));
    }
}
