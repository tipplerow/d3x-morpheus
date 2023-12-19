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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public final class BinomialDistributionTest extends DiscreteDistributionTestBase {
    private final BinomialDistribution dist1 = new BinomialDistribution(10, 0.25);
    private final BinomialDistribution dist2 = new BinomialDistribution(10, 0.50);
    private final BinomialDistribution dist3 = new BinomialDistribution(10, 0.75);
    private final BinomialDistribution dist4 = new BinomialDistribution(180, 0.01);

    private final double[] cdf1 = new double[] {
            0.05631351, 0.24402523, 0.52559280, 0.77587509, 0.92187309, 0.98027229,
            0.99649429, 0.99958420, 0.99997044, 0.99999905, 1.00000000,
    };

    private final double[] cdf2 = new double[] {
            0.0009765625, 0.0107421875, 0.0546875000, 0.1718750000, 0.3769531250, 0.6230468750,
            0.8281250000, 0.9453125000, 0.9892578125, 0.9990234375, 1.0000000000
    };

    private final double[] cdf3 = new double[]{
            0.0000009536, 0.0000295639, 0.0004158020, 0.0035057068, 0.0197277069, 0.0781269073,
            0.2241249084, 0.4744071960, 0.7559747696, 0.9436864853, 1.0000000000
    };

    private final double[] pmf1 = new double[] {
            5.631351e-02, 1.877117e-01, 2.815676e-01, 2.502823e-01, 1.459980e-01, 5.839920e-02,
            1.622200e-02, 3.089905e-03, 3.862381e-04, 2.861023e-05, 9.536743e-07
    };

    private final double[] pmf2 = new double[] {
            0.0009765625, 0.0097656250, 0.0439453125, 0.1171875000, 0.2050781250, 0.2460937500,
            0.2050781250, 0.1171875000, 0.0439453125, 0.0097656250, 0.0009765625
    };

    private final double[] pmf3 = new double[] {
            9.536743e-07, 2.861023e-05, 3.862381e-04, 3.089905e-03, 1.622200e-02, 5.839920e-02,
            1.459980e-01, 2.502823e-01, 2.815676e-01, 1.877117e-01, 5.631351e-02
    };

    @Test
    public void testCDF() {
        for (int k = 0; k < 10; ++k) {
            Assert.assertEquals(dist1.cdf(k), cdf1[k], 1e-08);
            Assert.assertEquals(dist2.cdf(k), cdf2[k], 1e-08);
            Assert.assertEquals(dist3.cdf(k), cdf3[k], 1e-08);
        }

        runCDFTest(dist1, IntSupport.over(5, 5), 1e-08);
        runCDFTest(dist1, IntSupport.over(4, 6), 1e-08);
        runCDFTest(dist1, IntSupport.over(3, 7), 1e-08);

        runCDFTest(dist2, IntSupport.over(5, 5), 1e-08);
        runCDFTest(dist2, IntSupport.over(4, 6), 1e-08);
        runCDFTest(dist2, IntSupport.over(3, 7), 1e-08);

        runCDFTest(dist3, IntSupport.over(5, 5), 1e-08);
        runCDFTest(dist3, IntSupport.over(4, 6), 1e-08);
        runCDFTest(dist3, IntSupport.over(3, 7), 1e-08);
    }

    @Test
    public void testPMF() {
        for (int k = 0; k < 10; ++k) {
            Assert.assertEquals(dist1.pmf(k), pmf1[k], 1e-06);
            Assert.assertEquals(dist2.pmf(k), pmf2[k], 1e-09);
            Assert.assertEquals(dist3.pmf(k), pmf3[k], 1e-06);
        }
    }

    @Test
    public void testSample() {
        var sample = dist1.sample(random(), 100000);
        runMomentTest(dist1, sample, 0.01, true);
    }

    @Test
    public void testTest() {
        Assert.assertEquals(dist2.test(5), 0.0, 1.0E-8);
        Assert.assertEquals(dist2.test(4), pmf2[5], 1.0E-8);
        Assert.assertEquals(dist2.test(6), pmf2[5], 1.0E-8);
        Assert.assertEquals(dist2.test(3), pmf2[4] + pmf2[5] + pmf2[6], 1.0E-8);
        Assert.assertEquals(dist2.test(7), pmf2[4] + pmf2[5] + pmf2[6], 1.0E-8);
        Assert.assertEquals(dist2.test(2), pmf2[3] + pmf2[4] + pmf2[5] + pmf2[6] + pmf2[7], 1.0E-8);
        Assert.assertEquals(dist2.test(8), pmf2[3] + pmf2[4] + pmf2[5] + pmf2[6] + pmf2[7], 1.0E-8);

        Assert.assertEquals(dist4.test(2), 0.0, 1.0E-08);
        Assert.assertEquals(dist4.test(1), dist4.pmf(2), 1.0E-08);
        Assert.assertEquals(dist4.test(3), dist4.pmf(2), 1.0E-08);
        Assert.assertEquals(dist4.test(0), dist4.pmf(1) + dist4.pmf(2) + dist4.pmf(3), 1.0E-08);
        Assert.assertEquals(dist4.test(4), dist4.pmf(1) + dist4.pmf(2) + dist4.pmf(3), 1.0E-08);
        Assert.assertEquals(dist4.test(5), dist4.cdf(4), 1.0E-08);
        Assert.assertEquals(dist4.test(6), dist4.cdf(5), 1.0E-08);
    }
}
