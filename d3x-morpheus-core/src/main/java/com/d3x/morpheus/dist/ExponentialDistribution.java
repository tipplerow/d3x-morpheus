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
 * Implements the exponential probability distribution.
 *
 * @author Scott Shaffer
 */
public final class ExponentialDistribution extends AbstractRealDistribution {
    /**
     * The rate parameter.
     */
    @Getter
    private final double rate;

    /**
     * Creates a new exponential distribution with a fixed rate parameter.
     *
     * @param rate the (positive) rate parameter.
     */
    public ExponentialDistribution(double rate) {
        validateRate(rate);
        this.rate = rate;
    }

    /**
     * The exponential distribution with unit rate.
     */
    public static final ExponentialDistribution UNIT = new ExponentialDistribution(1.0);

    /**
     * Computes the cumulative distribution function for an exponential
     * distribution with a given location and scale.
     *
     * @param x     the point at which to evaluate the CDF.
     * @param gamma the rate parameter for the distribution.
     *
     * @return the cumulative distribution function at the given point.
     *
     * @throws RuntimeException unless the rate parameter is positive.
     */
    public static double cdf(double x, double gamma) {
        validateRate(gamma);
        return 1.0 - Math.exp(-gamma * x);
    }

    /**
     * Computes the probability density function for an exponential
     * distribution with a given location and scale.
     *
     * @param x     the point at which to evaluate the PDF.
     * @param gamma the rate parameter for the distribution.
     *
     * @return the probability density function evaluated at the given
     * location.
     *
     * @throws RuntimeException unless the rate parameter is positive.
     */
    public static double pdf(double x, double gamma) {
        validateRate(gamma);
        return gamma * Math.exp(-gamma * x);
    }

    /**
     * Computes the quantile (inverse CDF) function for an exponential
     * distribution with a given location and scale.
     *
     * <p>The quantile function {@code Q(F)} is the point which the
     * cumulative distribution function takes value {@code F}.
     *
     * @param F     the cumulative probability.
     * @param gamma the rate parameter for the distribution.
     *
     * @return the quantile (inverse CDF) function evaluated at the given
     * cumulative probability.
     *
     * @throws RuntimeException unless the rate parameter is positive.
     */
    public static double quantile(double F, double gamma) {
        validateRate(gamma);
        validateQuantile(F);
        return -Math.log(1.0 - F) / gamma;
    }

    @Override
    public double cdf(double x) {
        return cdf(x, rate);
    }

    @Override
    public double pdf(double x) {
        return pdf(x, rate);
    }

    @Override
    public double quantile(double F) {
        return quantile(F, rate);
    }

    @Override
    public double mean() {
        return 1.0 / rate;
    }

    @Override
    public double median() {
        return DoubleUtil.LOG2 / rate;
    }

    @Override
    public double mode() {
        return 0.0;
    }

    @Override
    public double sdev() {
        return 1.0 / rate;
    }

    @Override
    public double sample(@NonNull RandomGenerator generator) {
        return generator.nextExponential() / rate;
    }

    @Override
    public DoubleInterval support() {
        return DoubleInterval.NON_NEGATIVE;
    }

    private static void validateRate(double rate) {
        if (rate <= 0.0)
            throw new IllegalArgumentException("Non-positive rate parameter.");
    }
}
