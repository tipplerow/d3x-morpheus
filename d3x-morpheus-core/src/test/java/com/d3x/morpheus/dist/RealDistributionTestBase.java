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

import com.d3x.morpheus.numerictests.NumericTestBase;
import com.d3x.morpheus.stats.StatSummary;

import lombok.NonNull;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.testng.Assert;

import java.util.stream.DoubleStream;

/**
 * @author Scott Shaffer
 */
public abstract class RealDistributionTestBase extends NumericTestBase {
    private static final int SUM_SAMPLE_SIZE = 100000;

    /**
     * Creates a new test class with the default numeric tolerance.
     */
    protected RealDistributionTestBase() {
        super();
    }

    /**
     * Creates a new test class with a fixed numeric tolerance.
     *
     * @param tolerance the tolerance for numeric comparisons.
     */
    protected RealDistributionTestBase(double tolerance) {
        super(tolerance);
    }

    /**
     * Enumerates supported sampling methods.
     */
    public enum SampleMethod { DEFAULT, REJECT, TRANSFORM }

    /**
     * Generates sample observations using the rejection method.
     *
     * @param distribution the distribution to test.
     * @param sampleCount  the desired sample size.
     *
     * @return the generated sample.
     */
    public double[] reject(@NonNull RealDistribution distribution, int sampleCount) {
        var generator = random();
        var observations = new double[sampleCount];

        for (int index = 0; index < sampleCount; ++index)
            observations[index] = distribution.reject(generator);

        return observations;
    }

    /**
     * Tests the decile locations of an empirical data sample against
     * those returned by a distribution.
     *
     * @param distribution the distribution to test.
     * @param sample       an empirical data sample.
     * @param tolerance    the tolerance for the numerical errors.
     * @param verbose      whether to write a summary to the console.
     */
    public void runDecileTest(@NonNull RealDistribution distribution,
                              double[] sample,
                              double tolerance,
                              boolean verbose) {
        var percentile = new Percentile();
        percentile.setData(sample);

        if (verbose) {
            System.out.println();
            System.out.printf("DECILE    ACTUAL     EXPECTED      ERROR  %n");
            System.out.printf("------  ----------  ----------  ----------%n");
        }

        for (int decile = 1; decile <= 9; ++decile) {
            var actual = percentile.evaluate(10.0 * decile);
            var expected = distribution.quantile(0.1 * decile);
            var error = actual - expected;

            if (verbose) {
                System.out.printf("%6d  %10.6f  %10.6f  %10.6f%n", decile, actual, expected, error);
            }

            Assert.assertTrue(Math.abs(error) < tolerance);
        }
    }

    /**
     * Tests the density and cumulative distributions by verifying that
     * the derivative of the CDF is equal to the PDF over the range of
     * support.
     *
     * @param distribution the distribution to test.
     * @param tolerance    the tolerance for the numerical errors.
     */
    public void runDistributionTest(@NonNull RealDistribution distribution, double tolerance) {
        var h = 0.00001 * distribution.sdev();

        streamX(distribution, 100).forEach(x -> {
            var pdf = distribution.pdf(x);
            var cdf1 = distribution.cdf(x - h);
            var cdf2 = distribution.cdf(x + h);
            Assert.assertEquals(pdf, (cdf2 - cdf1) / (2.0 * h), tolerance);
        });
    }

    /**
     * Tests the theoretical moments of a distribution against an empirical sample.
     *
     * @param distribution the distribution to test.
     * @param sampleMethod the sampling method.
     * @param sampleSize   the empirical sample size.
     * @param tolerance    tolerance for the sample statistics.
     * @param verbose      whether to write a summary to the console.
     */
    public void runMomentTest(@NonNull RealDistribution distribution,
                              @NonNull SampleMethod sampleMethod,
                              int sampleSize,
                              double tolerance,
                              boolean verbose) {
        var sample = sample(distribution, sampleMethod, sampleSize);
        runMomentTest(distribution, sample, tolerance, verbose);
    }

    /**
     * Tests the theoretical moments of a distribution against an empirical sample.
     *
     * @param distribution the distribution to test.
     * @param sample       the empirical sample.
     * @param tolerance    tolerance for the sample statistics.
     * @param verbose      whether to write a summary to the console.
     */
    public void runMomentTest(@NonNull RealDistribution distribution,
                              double[] sample,
                              double tolerance,
                              boolean verbose) {
        var summary = StatSummary.of(sample);
        var iqrError = distribution.IQR() - summary.getIQR();
        var meanError = distribution.mean() - summary.getMean();
        var sdevError = distribution.sdev() - summary.getSD();
        var medianError = distribution.median() - summary.getMedian();

        if (verbose) {
            System.out.println();
            System.out.printf("Mean error:   %12.8f%n", meanError);
            System.out.printf("SD error:     %12.8f%n", sdevError);
            System.out.printf("IQR error:    %12.8f%n", iqrError);
            System.out.printf("Median error: %12.8f%n", medianError);
        }

        Assert.assertTrue(Math.abs(iqrError) <= tolerance);
        Assert.assertTrue(Math.abs(meanError) <= tolerance);
        Assert.assertTrue(Math.abs(sdevError) <= tolerance);
        Assert.assertTrue(Math.abs(medianError) <= tolerance);
    }

    /**
     * Tests the quantile function by verifying that it is the inverse
     * of the CDF over the range of support.
     *
     * @param distribution the distribution to test.
     * @param tolerance    the tolerance for the numerical errors.
     */
    public void runQuantileTest(@NonNull RealDistribution distribution, double tolerance) {
        streamX(distribution, 100).forEach(x -> {
            var F = distribution.cdf(x);
            var Q = distribution.quantile(F);
            Assert.assertEquals(Q, x, tolerance);
        });

        for (double F = 0.01; F < 0.999; F += 0.01) {
            var Q = distribution.quantile(F);
            var cdf = distribution.cdf(Q);
            Assert.assertEquals(cdf, F, tolerance);
        }
    }

    /**
     * Tests the distribution of the sum of IID variables drawn from a
     * distribution.
     *
     * @param distribution the distribution to test.
     * @param count        the number of variables in the sum.
     * @param tolerance    the tolerance for the numerical errors.
     * @param verbose      whether to write a summary to the console.
     */
    public void runSumTest(@NonNull RealDistribution distribution, int count, double tolerance, boolean verbose) {
        var sumDist = distribution.sum(count);
        var sumSample = new double[SUM_SAMPLE_SIZE];

        for  (int index = 0; index < sumSample.length; ++index) {
            sumSample[index] = distribution.stream(random(), count).sum();
        }

        runDecileTest(sumDist, sumSample, tolerance, verbose);
        runMomentTest(sumDist, sumSample, tolerance, verbose);
    }

    /**
     * Generates a sample observation using a given method.
     *
     * @param distribution the distribution to sample.
     * @param sampleMethod the sampling method.
     *
     * @return the generated sample.
     */
    public double sample(@NonNull RealDistribution distribution,
                         @NonNull SampleMethod sampleMethod) {
        var generator = random();
        return switch (sampleMethod) {
            case DEFAULT -> distribution.sample(generator);
            case REJECT -> distribution.reject(generator);
            case TRANSFORM -> distribution.transform(generator);
        };
    }

    /**
     * Generates sample observations using a given method.
     *
     * @param distribution the distribution to sample.
     * @param sampleMethod the sampling method.
     * @param sampleSize   the desired sample size.
     *
     * @return the generated sample.
     */
    public double[] sample(@NonNull RealDistribution distribution,
                           @NonNull SampleMethod sampleMethod,
                           int sampleSize) {
        var sample = new double[sampleSize];

        for (var index = 0; index < sampleSize; ++index)
            sample[index] = sample(distribution, sampleMethod);

        return sample;
    }

    /**
     * Generates a stream of test points in the range of support.
     *
     * @param dist  the distribution to test.
     * @param count the number of test points.
     *
     * @return a stream of test points in the range of support.
     */
    public DoubleStream streamX(@NonNull RealDistribution dist, int count) {
        var mean = dist.mean();
        var sdev = dist.sdev();

        var lower = dist.support().getLower();
        var upper = dist.support().getUpper();
        var width = dist.support().getWidth();

        var xmin = Math.max(lower + 0.01 * width, mean - 3.0 * sdev);
        var xmax = Math.min(upper - 0.01 * width, mean + 3.0 * sdev);
        var step = (xmax - xmin) / count;

        return DoubleStream.iterate(xmin, x -> x <= xmax, x -> x + step);
    }

    /**
     * Generates sample observations using the transformation method.
     *
     * @param distribution the distribution to test.
     * @param sampleCount  the desired sample size.
     *
     * @return the generated sample.
     */
    public double[] transform(@NonNull RealDistribution distribution, int sampleCount) {
        var generator = random();
        var observations = new double[sampleCount];

        for (int index = 0; index < sampleCount; ++index)
            observations[index] = distribution.transform(generator);

        return observations;
    }
}
