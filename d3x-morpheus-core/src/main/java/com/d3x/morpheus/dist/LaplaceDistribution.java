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
import com.d3x.morpheus.util.DoubleUtil;

import lombok.Getter;
import lombok.NonNull;

import java.util.random.RandomGenerator;

/**
 * Implements the Laplace (double exponential) probability distribution.
 *
 * @author Scott Shaffer
 */
public final class LaplaceDistribution extends AbstractRealDistribution {
    /**
     * The location parameter (mu).
     */
    @Getter
    private final double location;

    /**
     * The scale parameter (b).
     */
    @Getter
    private final double scale;

    /**
     * Creates a new Laplace distribution with fixed location and scale.
     *
     * @param location the location parameter (mu).
     * @param scale    the scale parameter (b).
     */
    public LaplaceDistribution(double location, double scale) {
        validateScale(scale);
        this.location = location;
        this.scale = scale;
    }

    /**
     * Creates a new Laplace distribution with fixed location and width.
     *
     * @param location the location parameter (mu).
     * @param width    either the native scale parameter (b) or the standard deviation.
     * @param type     the scale parameter type.
     */
    public LaplaceDistribution(double location, double width, @NonNull ScaleType type) {
        this(location, resolveScale(width, type));
    }

    /**
     * The Laplace distribution with zero mean and unit scale.
     */
    public static final LaplaceDistribution UNIT = new LaplaceDistribution(0.0, 1.0);

    /**
     * Computes the cumulative distribution function for a Laplace distribution
     * with a given location and scale.
     *
     * @param x  the point at which to evaluate the CDF.
     * @param mu the location parameter for the distribution.
     * @param b  the scale parameter for the distribution.
     *
     * @return the cumulative distribution function at the given point.
     *
     * @throws RuntimeException unless the scale parameter is positive.
     */
    public static double cdf(double x, double mu, double b) {
        validateScale(b);

        if (x < mu)
            return 0.5 * Math.exp((x - mu) / b);
        else
            return 1.0 - 0.5 * Math.exp((mu - x) / b);
    }

    /**
     * Computes the probability density function for a Laplace distribution
     * with a given location and scale.
     *
     * @param x  the point at which to evaluate the PDF.
     * @param mu the location parameter for the distribution.
     * @param b  the scale parameter for the distribution.
     *
     * @return the probability density function evaluated at the given
     * location.
     *
     * @throws RuntimeException unless the scale parameter is positive.
     */
    public static double pdf(double x, double mu, double b) {
        validateScale(b);
        return Math.exp(-Math.abs(x - mu) / b) / (2.0 * b);
    }

    /**
     * Computes the quantile (inverse CDF) function for a Laplace distribution
     * with a given location and scale.
     *
     * <p>The quantile function {@code Q(F)} is the point which the
     * cumulative distribution function takes value {@code F}.
     *
     * @param F  the cumulative probability.
     * @param mu the location parameter for the distribution.
     * @param b  the scale parameter for the distribution.
     *
     * @return the quantile (inverse CDF) function evaluated at the given
     * cumulative probability.
     *
     * @throws RuntimeException unless the scale parameter is positive.
     */
    public static double quantile(double F, double mu, double b) {
        validateScale(b);
        validateQuantile(F);

        if (F < 0.5)
            return mu + b * Math.log(2.0 * F);
        else
            return mu - b * Math.log(2.0 - 2.0 * F);
    }

    @Override
    public double cdf(double x) {
        return cdf(x, location, scale);
    }

    @Override
    public double pdf(double x) {
        return pdf(x, location, scale);
    }

    @Override
    public double quantile(double F) {
        return quantile(F, location, scale);
    }

    @Override
    public double mean() {
        return location;
    }

    @Override
    public double median() {
        return location;
    }

    @Override
    public double mode() {
        return location;
    }

    @Override
    public double sdev() {
        return DoubleUtil.SQRT2 * scale;
    }

    @Override
    public double variance() {
        return 2.0 * scale * scale;
    }

    @Override
    public double sample(@NonNull RandomGenerator generator) {
        var U = generator.nextDouble(-0.5, 0.5);

        if (U < 0.0)
            return location + scale * Math.log(1.0 + 2.0 * U);
        else
            return location - scale * Math.log(1.0 - 2.0 * U);
    }

    @Override
    public DoubleInterval support() {
        return DoubleInterval.INFINITE;
    }

    private static double resolveScale(double width, ScaleType type) {
        return switch (type) {
            case NATIVE -> width;
            case SDEV   -> width / DoubleUtil.SQRT2;
        };
    }

    private static void validateScale(double scale) {
        if (scale <= 0.0)
            throw new IllegalArgumentException("Non-positive scale parameter.");
    }
}
