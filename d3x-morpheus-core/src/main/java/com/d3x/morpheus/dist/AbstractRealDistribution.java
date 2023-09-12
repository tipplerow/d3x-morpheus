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

import com.d3x.morpheus.root.BrentRootFinder;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.DoubleInterval;

import java.util.random.RandomGenerator;

/**
 * Provides a base class for real distribution implementations.
 *
 * @author Scott Shaffer
 */
public abstract class AbstractRealDistribution implements RealDistribution {
    // Sum distributions become normal, regardless of the underlying
    // distribution, above this number of variables...
    private static final int NORMAL_SUM = 30;

    // Number of trials used to generate an empirical sum distribution...
    private static final int SUM_SAMPLE = 100000;

    /**
     * Computes a standard Z-score for a real distribution.
     *
     * @param x    a sample from the distribution.
     * @param mean the mean of the distribution.
     * @param sdev the standard deviation of the distribution.
     *
     * @return the standard Z-score: {@code (x - mean) / sdev}.
     */
    public static double scoreZ(double x, double mean, double sdev) {
        validateSD(sdev);
        return (x - mean) / sdev;
    }

    /**
     * Validates a quantile value.
     *
     * @param F the quantile to validate.
     *
     * @throws RuntimeException unless the quantile value is in the
     * interval {@code [0, 1]}.
     */
    public static void validateQuantile(double F) {
        if (!DoubleInterval.FRACTIONAL.contains(F))
            throw new IllegalArgumentException("Non-fractional quantile value.");
    }

    /**
     * Validates a standard deviation.
     *
     * @param sdev the standard deviation to validate.
     *
     * @throws RuntimeException unless the standard deviation is positive.
     */
    public static void validateSD(double sdev) {
        if (sdev <= 0.0)
            throw new IllegalArgumentException("Non-positive standard deviation.");
    }

    /**
     * Computes a standard Z-score for this distribution.
     *
     * @param x a sample from this distribution.
     *
     * @return the standard Z-score: {@code (x - mean()) / sdev()}.
     */
    public double scoreZ(double x) {
        return scoreZ(x, mean(), sdev());
    }

    /**
     * Computes the quantile function by numerically inverting the cumulative
     * distribution function.
     *
     * @param F the cumulative probability.
     *
     * @return the quantile value {@code x} such that {@code cdf(x) == F}.
     *
     * @throws RuntimeException unless F is a fractional value.
     */
    protected double invertCDF(double F) {
        validateQuantile(F);
        var mean = mean();
        var sdev = sdev();
        var lower = support().getLower();
        var upper = support().getUpper();

        if (DoubleComparator.DEFAULT.isZero(F)) {
            return lower;
        }
        else if (DoubleComparator.DEFAULT.equals(F, 1.0)) {
            return upper;
        }

        if (!Double.isFinite(lower)) {
            lower = mean;

            while (cdf(lower) > F)
                lower -= sdev;
        }

        if (!Double.isFinite(upper)) {
            upper = mean;

            while (cdf(upper) < F)
                upper += sdev;
        }

        var tol = 1.0E-06 * sdev;
        var finder = new BrentRootFinder(tol);
        var initial = lower + F * (upper - lower);
        var interval = DoubleInterval.closed(lower, upper);
        return finder.solve(x -> cdf(x) - F, interval, initial);
    }

    @Override
    public RealDistribution sum(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Variable count must be positive.");
        }
        else if (count == 1) {
            return this;
        }
        else if (count < NORMAL_SUM) {
            return generateSumDistribution(count);
        }
        else {
            return NormalDistribution.sum(count, mean(), sdev());
        }
    }

    private RealDistribution generateSumDistribution(int count) {
        var sample = new double[SUM_SAMPLE];
        var generator = RandomGenerator.getDefault();

        for  (int index = 0; index < sample.length; ++index) {
            sample[index] = stream(generator, count).sum();
        }

        return new KernelDensityDistribution(sample);
    }
}
