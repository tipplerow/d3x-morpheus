/*
 * Copyright 2018-2023, Talos Trading - All Rights Reserved
 *
 * Licensed under a proprietary end-user agreement issued by D3X Systems.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.d3xsystems.com/static/eula/quanthub-eula.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.dist;

import com.d3x.morpheus.util.DoubleInterval;

import lombok.NonNull;

import java.util.random.RandomGenerator;

/**
 * @author Scott Shaffer
 */
public final class TriangularDistribution extends AbstractRealDistribution {
    private final double mean;
    private final double sdev;
    private final double median;

    private final double A, B, C;
    private final DoubleInterval support;

    // Factors in the PDF and CDF...
    private final double fac1;
    private final double fac2;
    private final double fac3;

    private TriangularDistribution(DoubleInterval support, double mode) {
        validateSupport(support);
        validateMode(support, mode);

        this.C = mode;
        this.A = support.getLower();
        this.B = support.getUpper();
        this.support = support;

        this.mean = computeMean();
        this.sdev = computeSD();
        this.median = computeMedian();

        this.fac1 = (B - A) * (C - A);
        this.fac2 = (B - A) * (B - C);
        this.fac3 = (C - A) / (B - A);
    }

    /**
     * Creates a new triangular distribution with a fixed interval and mode.
     *
     * @param lower the lower bound of the support interval.
     * @param upper the upper bound of the support interval.
     * @param mode  the mode of the distribution (the apex of the triangle).
     *
     * @throws RuntimeException unless the width of the support interval
     * is non-zero and finite and the mode lies within the interval.
     */
    public TriangularDistribution(double lower, double upper, double mode) {
        this(DoubleInterval.closed(lower, upper), mode);
    }

    /**
     * The triangular kernel distribution.
     */
    public static final TriangularDistribution KERNEL = new TriangularDistribution(-1.0, 1.0, 0.0);

    @Override
    public double cdf(double x) {
        if (x <= A) {
            return 0.0;
        }
        else if (x < C) {
            var y = x - A;
            return y * y / fac1;
        }
        else if (x < B) {
            var y = B - x;
            return 1.0 - y * y / fac2;
        }
        else {
            return 1.0;
        }
    }

    @Override
    public double pdf(double x) {
        if (x <= A) {
            return 0.0;
        }
        else if (x < C) {
            return 2.0 * (x - A) / fac1;
        }
        else if (x < B) {
            return 2.0 * (B - x) / fac2;
        }
        else {
            return 0.0;
        }
    }

    @Override
    public double quantile(double F) {
        validateQuantile(F);

        if (F < fac3) {
            return A + Math.sqrt(fac1 * F);
        }
        else {
            return B - Math.sqrt(fac2 * (1.0 - F));
        }
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double median() {
        return median;
    }

    @Override
    public double mode() {
        return C;
    }

    @Override
    public double sdev() {
        return sdev;
    }

    @Override
    public double sample(@NonNull RandomGenerator generator) {
        return transform(generator);
    }

    @Override
    public DoubleInterval support() {
        return support;
    }

    private static void validateSupport(DoubleInterval support) {
        if (!Double.isFinite(support.getWidth()))
            throw new IllegalArgumentException("Support interval must be finite.");
    }

    private static void validateMode(DoubleInterval support, double mode) {
        if (!support.contains(mode))
            throw new IllegalArgumentException("Support interval must contain the mode.");
    }

    private double computeMean() {
        return (A + B + C) / 3.0;
    }

    private double computeSD() {
        return Math.sqrt((A * A + B * B + C * C - A * B - A * C - B * C) / 18.0);
    }

    private double computeMedian() {
        var mid = 0.5 * (A + B);

        if (C >= mid) {
            return A + Math.sqrt(0.5 * (B - A) * (C - A));
        }
        else {
            return B - Math.sqrt(0.5 * (B - A) * (B - C));
        }
    }
}
