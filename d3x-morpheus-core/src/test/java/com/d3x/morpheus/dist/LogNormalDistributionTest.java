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
public class LogNormalDistributionTest extends RealDistributionTestBase {
    private static final int SAMPLE_SIZE = 1000000;
    private static final double TOLERANCE = 1.0E-06;

    private final LogNormalDistribution dist1 = new LogNormalDistribution(0.0, 1.0);
    private final LogNormalDistribution dist2 = new LogNormalDistribution(1.0, 2.0);

    private final double[] sample1 = dist1.sample(random(), SAMPLE_SIZE);
    private final double[] sample2 = dist2.sample(random(), SAMPLE_SIZE);

    public LogNormalDistributionTest() {
        super(TOLERANCE);
    }

    @Test
    public void testCDF() {
        assertDouble(dist1.cdf(0.1), 0.0106511);
        assertDouble(dist1.cdf(0.5), 0.2441086);
        assertDouble(dist1.cdf(1.0), 0.5000000);
        assertDouble(dist1.cdf(2.0), 0.7558914);
        assertDouble(dist1.cdf(3.0), 0.8640314);

        assertDouble(dist2.cdf(0.1), 0.04933943);
        assertDouble(dist2.cdf(0.5), 0.19861642);
        assertDouble(dist2.cdf(1.0), 0.30853754);
        assertDouble(dist2.cdf(2.0), 0.43903101);
        assertDouble(dist2.cdf(3.0), 0.51966234);
    }

    @Test
    public void testDeciles() {
        runDecileTest(dist1, sample1, 0.006, true);
    }

    @Test
    public void testDistributions() {
        runDistributionTest(dist1, 0.00001);
        runDistributionTest(dist2, 0.00001);
    }

    @Test
    public void testMoments() {
        runMomentTest(dist1, sample1, 0.01, true);
    }

    @Test
    public void testParameters() {
        assertDouble(dist1.getMu(), 0.0);
        assertDouble(dist2.getMu(), 1.0);
        assertDouble(dist1.getSigma(), 1.0);
        assertDouble(dist2.getSigma(), 2.0);
    }

    @Test
    public void testPDF() {
        assertDouble(dist1.pdf(0.1), 0.28159019);
        assertDouble(dist1.pdf(0.5), 0.62749608);
        assertDouble(dist1.pdf(1.0), 0.39894228);
        assertDouble(dist1.pdf(2.0), 0.15687402);
        assertDouble(dist1.pdf(3.0), 0.07272826);

        assertDouble(dist2.pdf(0.1), 0.51023486);
        assertDouble(dist2.pdf(0.5), 0.27879405);
        assertDouble(dist2.pdf(1.0), 0.17603266);
        assertDouble(dist2.pdf(2.0), 0.09856858);
        assertDouble(dist2.pdf(3.0), 0.06640961);
    }

    @Test
    public void testQuantile() {
        assertDouble(dist1.quantile(0.01),  0.09765173);
        assertDouble(dist1.quantile(0.05),  0.19304082);
        assertDouble(dist1.quantile(0.25),  0.50941628);
        assertDouble(dist1.quantile(0.50),  1.00000000);
        assertDouble(dist1.quantile(0.75),  1.96303108);
        assertDouble(dist1.quantile(0.95),  5.18025160);
        assertDouble(dist1.quantile(0.99), 10.24047366);

        assertDouble(dist2.quantile(0.01),   0.02592116);
        assertDouble(dist2.quantile(0.05),   0.10129611);
        assertDouble(dist2.quantile(0.25),   0.70540759);
        assertDouble(dist2.quantile(0.50),   2.71828183);
        assertDouble(dist2.quantile(0.75),  10.47487466);
        assertDouble(dist2.quantile(0.95),  72.94511098);
        assertDouble(dist2.quantile(0.99), 285.05887791);

        runQuantileTest(dist1, TOLERANCE);
        runQuantileTest(dist2, TOLERANCE);
    }

    @Test
    public void testSum() {
        runSumTest(dist1,  2, 0.10, true);
        runSumTest(dist1,  5, 0.20, true);
    }
}

