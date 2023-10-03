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
 * Implements a uniform probability distribution.
 *
 * @author Scott Shaffer
 */
public final class UniformDistribution extends AbstractRealDistribution {
    private final double mean;
    private final double sdev;
    private final double density;

    private final double lower;
    private final double upper;
    private final double width;
    private final DoubleInterval support;

    /**
     * Creates a new uniform distribution over a fixed interval.
     *
     * @param lower the lower bound of the support interval.
     * @param upper the upper bound of the support interval.
     *
     * @throws RuntimeException unless the width of the support interval
     * is non-zero and finite.
     */
    public UniformDistribution(double lower, double upper) {
        this(DoubleInterval.closed(lower, upper));
    }

    /**
     * Creates a new uniform distribution over a fixed interval.
     *
     * @param support the support interval.
     *
     * @throws RuntimeException unless the width of the support interval
     * is non-zero and finite.
     */
    public UniformDistribution(@NonNull DoubleInterval support) {
        validateSupport(support);
        this.support = support;
        this.lower = support.getLower();
        this.upper = support.getUpper();
        this.width = support.getWidth();

        this.mean = support.getMidPoint();
        this.sdev = width / Math.sqrt(12.0);
        this.density = 1.0 / width;
    }

    /**
     * The uniform kernel density function.
     */
    public static UniformDistribution KERNEL = new UniformDistribution(-1.0, 1.0);

    @Override
    public double cdf(double x) {
        if (x < lower) {
            return 0.0;
        }
        else if (x < upper) {
            return density * (x - lower);
        }
        else {
            return 1.0;
        }
    }

    @Override
    public double pdf(double x) {
        return (lower <= x && x <= upper) ? density : 0.0;
    }

    @Override
    public double quantile(double F) {
        validateQuantile(F);
        return lower + F * width;
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double median() {
        return mean;
    }

    @Override
    public double mode() {
        return Double.NaN;
    }

    @Override
    public double sdev() {
        return sdev;
    }

    @Override
    public double sample(@NonNull RandomGenerator generator) {
        return generator.nextDouble(lower, upper);
    }

    @Override
    public DoubleInterval support() {
        return support;
    }

    private static void validateSupport(DoubleInterval support) {
        if (!Double.isFinite(support.getWidth()))
            throw new IllegalArgumentException("Support interval must be finite.");
    }
}
