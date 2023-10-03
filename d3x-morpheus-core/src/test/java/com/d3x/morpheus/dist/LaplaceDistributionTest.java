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

import com.d3x.morpheus.stats.StatSummary;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class LaplaceDistributionTest extends RealDistributionTestBase {
    private static final int SAMPLE_SIZE = 1000000;
    private static final double TOLERANCE = 1.0E-06;

    private final LaplaceDistribution dist1 = LaplaceDistribution.UNIT;
    private final LaplaceDistribution dist2 = new LaplaceDistribution(1.0, 2.0);

    private final double[] sample1 = dist1.sample(random(), SAMPLE_SIZE);
    private final double[] sample2 = dist2.sample(random(), SAMPLE_SIZE);

    public LaplaceDistributionTest() {
        super(TOLERANCE);
    }

    @Test
    public void testCDF() {
        assertDouble(dist1.cdf(-3.0), 0.02489353);
        assertDouble(dist1.cdf(-2.0), 0.06766764);
        assertDouble(dist1.cdf(-1.0), 0.18393972);
        assertDouble(dist1.cdf( 0.0), 0.50000000);
        assertDouble(dist1.cdf( 1.0), 0.81606028);
        assertDouble(dist1.cdf( 2.0), 0.93233236);
        assertDouble(dist1.cdf( 3.0), 0.97510647);

        assertDouble(dist2.cdf(-3.0), 0.06766764);
        assertDouble(dist2.cdf(-2.0), 0.11156508);
        assertDouble(dist2.cdf(-1.0), 0.18393972);
        assertDouble(dist2.cdf( 0.0), 0.30326533);
        assertDouble(dist2.cdf( 1.0), 0.50000000);
        assertDouble(dist2.cdf( 2.0), 0.69673467);
        assertDouble(dist2.cdf( 3.0), 0.81606028);
    }

    @Test
    public void testDeciles() {
        runDecileTest(dist1, sample1, 0.005, true);
        runDecileTest(dist2, sample2, 0.010, true);
    }

    @Test
    public void testDistributions() {
        runDistributionTest(dist1, 0.00001);
        runDistributionTest(dist2, 0.00001);
    }

    @Test
    public void testMoments() {
        runMomentTest(dist1, sample1, 0.002, true);
        runMomentTest(dist2, sample2, 0.010, true);
    }

    @Test
    public void testParameters() {
        assertDouble(dist1.getLocation(), 0.0);
        assertDouble(dist2.getLocation(), 1.0);

        assertDouble(dist1.getScale(), 1.0);
        assertDouble(dist2.getScale(), 2.0);

        assertDouble(dist1.mean(), 0.0);
        assertDouble(dist1.sdev(), Math.sqrt(2.0));
        assertDouble(dist1.median(), 0.0);
        assertDouble(dist1.variance(), 2.0);

        assertDouble(dist2.mean(), 1.0);
        assertDouble(dist2.sdev(), 2.0 * Math.sqrt(2.0));
        assertDouble(dist2.median(), 1.0);
        assertDouble(dist2.variance(), 8.0);
    }

    @Test
    public void testPDF() {
        assertDouble(dist1.pdf(-3.0), 0.02489353);
        assertDouble(dist1.pdf(-2.0), 0.06766764);
        assertDouble(dist1.pdf(-1.0), 0.18393972);
        assertDouble(dist1.pdf( 0.0), 0.50000000);
        assertDouble(dist1.pdf( 1.0), 0.18393972);
        assertDouble(dist1.pdf( 2.0), 0.06766764);
        assertDouble(dist1.pdf( 3.0), 0.02489353);

        assertDouble(dist2.pdf(-3.0), 0.03383382);
        assertDouble(dist2.pdf(-2.0), 0.05578254);
        assertDouble(dist2.pdf(-1.0), 0.09196986);
        assertDouble(dist2.pdf( 0.0), 0.15163266);
        assertDouble(dist2.pdf( 1.0), 0.25000000);
        assertDouble(dist2.pdf( 2.0), 0.15163266);
        assertDouble(dist2.pdf( 3.0), 0.09196986);
    }

    @Test
    public void testQuantile() {
        assertDouble(dist1.quantile(0.01), -3.9120230);
        assertDouble(dist1.quantile(0.05), -2.3025851);
        assertDouble(dist1.quantile(0.25), -0.6931472);
        assertDouble(dist1.quantile(0.50),  0.0000000);
        assertDouble(dist1.quantile(0.75),  0.6931472);
        assertDouble(dist1.quantile(0.95),  2.3025851);
        assertDouble(dist1.quantile(0.99),  3.9120230);

        assertDouble(dist2.quantile(0.01), -6.8240460);
        assertDouble(dist2.quantile(0.05), -3.6051702);
        assertDouble(dist2.quantile(0.25), -0.3862944);
        assertDouble(dist2.quantile(0.50),  1.0000000);
        assertDouble(dist2.quantile(0.75),  2.3862944);
        assertDouble(dist2.quantile(0.95),  5.6051702);
        assertDouble(dist2.quantile(0.99),  8.8240460);

        runQuantileTest(dist1, TOLERANCE);
        runQuantileTest(dist2, TOLERANCE);
    }

    @Test
    public void testScaleType() {
        var mean = 2.0;
        var sdev = 0.5;
        var dist = new LaplaceDistribution(mean, sdev, ScaleType.SDEV);
        var sample = dist.sample(random(), SAMPLE_SIZE);
        var summary = StatSummary.of(sample);

        Assert.assertEquals(summary.getSD(), sdev, 0.001);
        Assert.assertEquals(summary.getMean(), mean, 0.001);
    }

    @Test
    public void testSum() {
        runSumTest(dist1,  2, 0.05, true);
        runSumTest(dist1,  5, 0.15, true);
        runSumTest(dist1, 30, 0.25, true);

        runSumTest(dist2,  2, 0.10, true);
        runSumTest(dist2,  5, 0.30, true);
        runSumTest(dist2, 30, 0.50, true);
    }
}
