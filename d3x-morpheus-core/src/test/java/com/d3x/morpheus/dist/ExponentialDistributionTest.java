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
public class ExponentialDistributionTest extends RealDistributionTestBase {
    private static final int SAMPLE_SIZE = 1000000;
    private static final double TOLERANCE = 1.0E-06;

    private final ExponentialDistribution dist1 = ExponentialDistribution.UNIT;
    private final ExponentialDistribution dist2 = new ExponentialDistribution(3.0);

    private final double[] sample1 = dist1.sample(random(), SAMPLE_SIZE);
    private final double[] sample2 = dist2.sample(random(), SAMPLE_SIZE);

    public ExponentialDistributionTest() {
        super(TOLERANCE);
    }

    @Test
    public void testDeciles() {
        runDecileTest(dist1, sample1, 0.006, true);
        runDecileTest(dist2, sample2, 0.003, true);
    }

    @Test
    public void testCDF() {
        assertDouble(dist1.cdf(0.1), 0.09516258);
        assertDouble(dist1.cdf(0.5), 0.39346934);
        assertDouble(dist1.cdf(1.0), 0.63212056);
        assertDouble(dist1.cdf(2.0), 0.86466472);
        assertDouble(dist1.cdf(3.0), 0.95021293);

        assertDouble(dist2.cdf(0.1), 0.2591818);
        assertDouble(dist2.cdf(0.5), 0.7768698);
        assertDouble(dist2.cdf(1.0), 0.9502129);
        assertDouble(dist2.cdf(2.0), 0.9975212);
        assertDouble(dist2.cdf(3.0), 0.9998766);
    }

    @Test
    public void testDistributions() {
        runDistributionTest(dist1, 0.00001);
        runDistributionTest(dist2, 0.00001);
    }

    @Test
    public void testMoments() {
        runMomentTest(dist1, sample1, 0.0035, true);
        runMomentTest(dist2, sample2, 0.0015, true);
    }

    @Test
    public void testParameters() {
        assertDouble(dist1.getRate(), 1.0);
        assertDouble(dist2.getRate(), 3.0);

        assertDouble(dist1.mean(), 1.0);
        assertDouble(dist1.sdev(), 1.0);
        assertDouble(dist1.median(), Math.log(2));
        assertDouble(dist1.variance(), 1.0);

        assertDouble(dist2.mean(), 1.0 / 3.0);
        assertDouble(dist2.sdev(), 1.0 / 3.0);
        assertDouble(dist2.median(), Math.log(2.0) / 3.0);
        assertDouble(dist2.variance(), 1.0 / 9.0);
    }

    @Test
    public void testPDF() {
        assertDouble(dist1.pdf(0.1), 0.9048374);
        assertDouble(dist1.pdf(0.5), 0.60653066);
        assertDouble(dist1.pdf(1.0), 0.36787944);
        assertDouble(dist1.pdf(2.0), 0.13533528);
        assertDouble(dist1.pdf(3.0), 0.04978707);

        assertDouble(dist2.pdf(0.1), 2.222455);
        assertDouble(dist2.pdf(0.5), 0.6693905);
        assertDouble(dist2.pdf(1.0), 0.1493612);
        assertDouble(dist2.pdf(2.0), 0.007436257);
        assertDouble(dist2.pdf(3.0), 0.0003702294);
    }

    @Test
    public void testQuantile() {
        assertDouble(dist1.quantile(0.01), 0.01005034);
        assertDouble(dist1.quantile(0.05), 0.05129329);
        assertDouble(dist1.quantile(0.25), 0.28768207);
        assertDouble(dist1.quantile(0.50), 0.69314718);
        assertDouble(dist1.quantile(0.75), 1.38629436);
        assertDouble(dist1.quantile(0.95), 2.99573227);
        assertDouble(dist1.quantile(0.99), 4.60517019);

        assertDouble(dist2.quantile(0.01), 0.003350112);
        assertDouble(dist2.quantile(0.05), 0.017097765);
        assertDouble(dist2.quantile(0.25), 0.095894024);
        assertDouble(dist2.quantile(0.50), 0.231049060);
        assertDouble(dist2.quantile(0.75), 0.462098120);
        assertDouble(dist2.quantile(0.95), 0.998577425);
        assertDouble(dist2.quantile(0.99), 1.535056729);

        runQuantileTest(dist1, TOLERANCE);
        runQuantileTest(dist2, TOLERANCE);
    }
}
