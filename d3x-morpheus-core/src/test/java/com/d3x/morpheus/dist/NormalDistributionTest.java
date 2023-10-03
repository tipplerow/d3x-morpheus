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

import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class NormalDistributionTest extends RealDistributionTestBase {
    private static final int SAMPLE_SIZE = 1000000;
    private static final double TOLERANCE = 1.0E-07;

    private final NormalDistribution dist1 = NormalDistribution.STANDARD;
    private final NormalDistribution dist2 = new NormalDistribution(1.0, 2.0);

    private final double[] sample1 = dist1.sample(random(), SAMPLE_SIZE);
    private final double[] sample2 = dist2.sample(random(), SAMPLE_SIZE);

    public NormalDistributionTest() {
        super(TOLERANCE);
    }

    @Test
    public void testCDF() {
        assertDouble(dist1.cdf(-3.0), 0.001349898);
        assertDouble(dist1.cdf(-2.0), 0.022750132);
        assertDouble(dist1.cdf(-1.0), 0.158655254);
        assertDouble(dist1.cdf( 0.0), 0.500000000);
        assertDouble(dist1.cdf( 1.0), 0.841344746);
        assertDouble(dist1.cdf( 2.0), 0.977249868);
        assertDouble(dist1.cdf( 3.0), 0.998650102);

        assertDouble(dist2.cdf(-3.0), 0.02275013);
        assertDouble(dist2.cdf(-2.0), 0.06680720);
        assertDouble(dist2.cdf(-1.0), 0.15865525);
        assertDouble(dist2.cdf( 0.0), 0.30853754);
        assertDouble(dist2.cdf( 1.0), 0.50000000);
        assertDouble(dist2.cdf( 2.0), 0.69146246);
        assertDouble(dist2.cdf( 3.0), 0.84134475);
    }

    @Test
    public void testDeciles() {
        runDecileTest(dist1, sample1, 0.003, true);
        runDecileTest(dist2, sample2, 0.005, true);
    }

    @Test
    public void testDistributions() {
        runDistributionTest(dist1, 0.00001);
        runDistributionTest(dist2, 0.00001);
    }

    @Test
    public void testMoments() {
        runMomentTest(dist1, sample1, 0.002, true);
        runMomentTest(dist2, sample2, 0.004, true);
    }

    @Test
    public void testParameters() {
        assertDouble(dist1.mean(), 0.0);
        assertDouble(dist1.sdev(), 1.0);
        assertDouble(dist1.median(), 0.0);
        assertDouble(dist1.variance(), 1.0);

        assertDouble(dist2.mean(), 1.0);
        assertDouble(dist2.sdev(), 2.0);
        assertDouble(dist2.median(), 1.0);
        assertDouble(dist2.variance(), 4.0);
    }

    @Test
    public void testPDF() {
        assertDouble(dist1.pdf(-3.0), 0.004431848);
        assertDouble(dist1.pdf(-2.0), 0.053990967);
        assertDouble(dist1.pdf(-1.0), 0.241970725);
        assertDouble(dist1.pdf( 0.0), 0.398942280);
        assertDouble(dist1.pdf( 1.0), 0.241970725);
        assertDouble(dist1.pdf( 2.0), 0.053990967);
        assertDouble(dist1.pdf( 3.0), 0.004431848);

        assertDouble(dist2.pdf(-3.0), 0.02699548);
        assertDouble(dist2.pdf(-2.0), 0.06475880);
        assertDouble(dist2.pdf(-1.0), 0.12098536);
        assertDouble(dist2.pdf( 0.0), 0.17603266);
        assertDouble(dist2.pdf( 1.0), 0.19947114);
        assertDouble(dist2.pdf( 2.0), 0.17603266);
        assertDouble(dist2.pdf( 3.0), 0.12098536);
    }

    @Test
    public void testQuantile() {
        assertDouble(dist1.quantile(0.01), -2.3263479);
        assertDouble(dist1.quantile(0.05), -1.6448536);
        assertDouble(dist1.quantile(0.25), -0.6744898);
        assertDouble(dist1.quantile(0.50),  0.0000000);
        assertDouble(dist1.quantile(0.75),  0.6744898);
        assertDouble(dist1.quantile(0.95),  1.6448536);
        assertDouble(dist1.quantile(0.99),  2.3263479);

        assertDouble(dist2.quantile(0.01), -3.6526957);
        assertDouble(dist2.quantile(0.05), -2.2897073);
        assertDouble(dist2.quantile(0.25), -0.3489795);
        assertDouble(dist2.quantile(0.50),  1.0000000);
        assertDouble(dist2.quantile(0.75),  2.3489795);
        assertDouble(dist2.quantile(0.95),  4.2897073);
        assertDouble(dist2.quantile(0.99),  5.6526957);

        runQuantileTest(dist1, TOLERANCE);
        runQuantileTest(dist2, TOLERANCE);
    }

    @Test
    public void testSum() {
        runSumTest(dist1,  2, 0.05, true);
        runSumTest(dist1,  5, 0.10, true);
        runSumTest(dist1, 30, 0.20, true);

        runSumTest(dist2,  2, 0.05, true);
        runSumTest(dist2,  5, 0.10, true);
        runSumTest(dist2, 30, 0.20, true);
    }
}
