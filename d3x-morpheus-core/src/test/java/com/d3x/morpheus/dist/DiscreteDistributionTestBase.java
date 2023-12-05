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

import org.testng.Assert;

/**
 * Provides a base class for testing discrete probability distributions.
 *
 * @author Scott Shaffer
 */
public abstract class DiscreteDistributionTestBase extends NumericTestBase {
    /**
     * Compares the explicit cumulative distribution function to the sum of
     * probability mass functions.
     *
     * @param dist      the distribution to test.
     * @param range     the range of observations.
     * @param tolerance the numerical tolerance.
     */
    public void runCDFTest(DiscreteDistribution dist, IntSupport range, double tolerance) {
        var sum = 0.0;

        for (int j = range.getLower(); j <= range.getUpper(); ++j)
            sum += dist.pmf(j);

        var cdf = dist.cdf(range);
        Assert.assertEquals(cdf, sum, tolerance);
    }

    /**
     * Tests the theoretical moments of a distribution against an empirical sample.
     *
     * @param distribution the distribution to test.
     * @param sample       the empirical sample.
     * @param tolerance    tolerance for the sample statistics.
     * @param verbose      whether to write a summary to the console.
     */
    public void runMomentTest(@NonNull DiscreteDistribution distribution,
                              int[] sample,
                              double tolerance,
                              boolean verbose) {
        var summary = StatSummary.of(sample);
        var meanError = distribution.mean() - summary.getMean();
        var sdevError = distribution.sdev() - summary.getSD();

        if (verbose) {
            System.out.println();
            System.out.printf("Mean error:   %12.8f%n", meanError);
            System.out.printf("SD error:     %12.8f%n", sdevError);
        }

        Assert.assertTrue(Math.abs(meanError) <= tolerance);
        Assert.assertTrue(Math.abs(sdevError) <= tolerance);
    }
}
