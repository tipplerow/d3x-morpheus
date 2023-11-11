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

import lombok.NonNull;

import org.apache.commons.math3.special.Erf;

import java.util.random.RandomGenerator;

/**
 * Implements the normal (Gaussian) probability distribution.
 *
 * @author Scott Shaffer
 */
public final class NormalDistribution extends AbstractRealDistribution {
    private final double mean;
    private final double sdev;

    /**
     * Creates a new normal distribution with a fixed mean and standard
     * deviation.
     *
     * @param mean the distribution mean.
     * @param sdev the distribution standard deviation.
     */
    public NormalDistribution(double mean, double sdev) {
        validateSD(sdev);
        this.mean = mean;
        this.sdev = sdev;
    }

    /**
     * The standard normal distribution with zero mean and unit variance.
     */
    public static final NormalDistribution STANDARD = new NormalDistribution(0.0, 1.0);

    /**
     * Computes the cumulative distribution function for a normal distribution
     * with a given mean and standard deviation.
     *
     * @param x    the point at which to evaluate the CDF.
     * @param mean the mean of the distribution.
     * @param sdev the standard deviation of the distribution.
     *
     * @return the cumulative distribution function at the given point.
     *
     * @throws RuntimeException unless the standard deviation is positive.
     */
    public static double cdf(double x, double mean, double sdev) {
        var z = scoreZ(x, mean, sdev);
        return 0.5 * (1.0 + Erf.erf(z / DoubleUtil.SQRT2));
    }

    /**
     * Computes the probability density function for a normal distribution
     * with a given mean and standard deviation.
     *
     * @param x    the point at which to evaluate the PDF.
     * @param mean the mean of the distribution.
     * @param sdev the standard deviation of the distribution.
     *
     * @return the probability density function at the given point.
     *
     * @throws RuntimeException unless the standard deviation is positive.
     */
    public static double pdf(double x, double mean, double sdev) {
        double z = scoreZ(x, mean, sdev);
        return Math.exp(-0.5 * z * z) / (sdev * DoubleUtil.SQRT_TWO_PI);
    }

    /**
     * Computes the quantile (inverse CDF) function for a normal distribution
     * with a given mean and standard deviation.
     *
     * <p>The quantile function {@code Q(F)} is the point which the
     * cumulative distribution function takes value {@code F}.
     *
     * @param F    the cumulative probability.
     * @param mean the mean of the distribution.
     * @param sdev the standard deviation of the distribution.
     *
     * @return the quantile (inverse CDF) function evaluated at the given
     * cumulative probability.
     *
     * @throws RuntimeException unless the standard deviation is positive.
     */
    public static double quantile(double F, double mean, double sdev) {
        validateSD(sdev);
        validateQuantile(F);
        return mean + DoubleUtil.SQRT2 * sdev * Erf.erfInv(2.0 * F - 1.0);
    }

    /**
     * Creates a normal distribution describing the sum of {@code count}
     * independent and identically distributed normal random variables.
     *
     * @param count the number of variables in the sum.
     * @param mean  the individual variable mean.
     * @param sdev  the individual variable standard deviation.
     *
     * @return a normal distribution describing the sum of {@code count}
     * independent and identically distributed normal random variables
     * with mean equal {@code mean} and standard deviation {@code sdev}.
     */
    public static NormalDistribution sum(int count, double mean, double sdev) {
        if (count > 0)
            return new NormalDistribution(count * mean, Math.sqrt(count) * sdev);
        else
            throw new IllegalArgumentException("Variable count must be positive.");
    }

    @Override
    public double cdf(double x) {
        return cdf(x, mean, sdev);
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
        return mean;
    }

    @Override
    public double pdf(double x) {
        return pdf(x, mean, sdev);
    }

    @Override
    public double quantile(double F) {
        return quantile(F, mean, sdev);
    }

    @Override
    public double sdev() {
        return sdev;
    }

    @Override
    public double sample(@NonNull RandomGenerator generator) {
        return generator.nextGaussian(mean, sdev);
    }

    @Override
    public NormalDistribution sum(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Variable count must be positive.");
        }
        else if (count == 1) {
            return this;
        }
        else {
            return sum(count, mean, sdev);
        }
    }

    @Override
    public DoubleInterval support() {
        return DoubleInterval.INFINITE;
    }
}
