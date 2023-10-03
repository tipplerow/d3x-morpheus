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
public class UniformDistributionTest extends RealDistributionTestBase {
    private static final int SAMPLE_SIZE = 1000000;
    private static final double TOLERANCE = 1.0E-07;

    private final UniformDistribution dist1 = UniformDistribution.KERNEL;
    private final UniformDistribution dist2 = new UniformDistribution(-2.0, 3.0);

    private final double[] sample1 = dist1.sample(random(), SAMPLE_SIZE);
    private final double[] sample2 = dist2.sample(random(), SAMPLE_SIZE);

    public UniformDistributionTest() {
        super(TOLERANCE);
    }

    @Test
    public void testCDF() {
        assertDouble(dist1.cdf(-2.0), 0.0);
        assertDouble(dist1.cdf(-1.0), 0.0);
        assertDouble(dist1.cdf(-0.5), 0.25);
        assertDouble(dist1.cdf( 0.0), 0.50);
        assertDouble(dist1.cdf( 0.5), 0.75);
        assertDouble(dist1.cdf( 1.0), 1.0);
        assertDouble(dist1.cdf( 2.0), 1.0);

        assertDouble(dist2.cdf(-3.0), 0.0);
        assertDouble(dist2.cdf(-2.0), 0.0);
        assertDouble(dist2.cdf(-1.0), 0.2);
        assertDouble(dist2.cdf( 0.0), 0.4);
        assertDouble(dist2.cdf( 1.0), 0.6);
        assertDouble(dist2.cdf( 2.0), 0.8);
        assertDouble(dist2.cdf( 3.0), 1.0);
        assertDouble(dist2.cdf( 4.0), 1.0);
    }

    @Test
    public void testDeciles() {
        runDecileTest(dist1, sample1, 0.002, true);
        runDecileTest(dist2, sample2, 0.006, true);
    }

    @Test
    public void testDistributions() {
        runDistributionTest(dist1, 0.00001);
        runDistributionTest(dist2, 0.00001);
    }

    @Test
    public void testMoments() {
        runMomentTest(dist1, SampleMethod.DEFAULT, SAMPLE_SIZE, 0.002, true);
        runMomentTest(dist2, SampleMethod.DEFAULT, SAMPLE_SIZE, 0.005, true);
    }

    @Test
    public void testParameters() {
        assertDouble(dist1.mean(), 0.0);
        assertDouble(dist1.sdev(), Math.sqrt(1.0 / 3.0));
        assertDouble(dist1.median(), 0.0);
        assertDouble(dist1.variance(), 1.0 / 3.0);

        assertDouble(dist2.mean(), 0.5);
        assertDouble(dist2.sdev(), Math.sqrt(25.0 / 12.0));
        assertDouble(dist2.median(), 0.5);
        assertDouble(dist2.variance(), 25.0 / 12.0);
    }

    @Test
    public void testPDF() {
        assertDouble(dist1.pdf(-2.0),  0.0);
        assertDouble(dist1.pdf(-1.01), 0.0);
        assertDouble(dist1.pdf(-0.99), 0.5);
        assertDouble(dist1.pdf(-0.5),  0.5);
        assertDouble(dist1.pdf( 0.0),  0.5);
        assertDouble(dist1.pdf( 0.5),  0.5);
        assertDouble(dist1.pdf( 0.99), 0.5);
        assertDouble(dist1.pdf( 1.01), 0.0);
        assertDouble(dist1.pdf( 2.0),  0.0);

        assertDouble(dist2.pdf(-3.0),  0.0);
        assertDouble(dist2.pdf(-2.01), 0.0);
        assertDouble(dist2.pdf(-1.99), 0.2);
        assertDouble(dist2.pdf(-1.0),  0.2);
        assertDouble(dist2.pdf( 0.0),  0.2);
        assertDouble(dist2.pdf( 1.0),  0.2);
        assertDouble(dist2.pdf( 2.0),  0.2);
        assertDouble(dist2.pdf( 2.99), 0.2);
        assertDouble(dist2.pdf( 3.01), 0.0);
        assertDouble(dist2.pdf( 4.0),  0.0);
    }

    @Test
    public void testQuantile() {
        assertDouble(dist1.quantile(0.01), -0.98);
        assertDouble(dist1.quantile(0.05), -0.90);
        assertDouble(dist1.quantile(0.25), -0.50);
        assertDouble(dist1.quantile(0.50),  0.0);
        assertDouble(dist1.quantile(0.75),  0.50);
        assertDouble(dist1.quantile(0.95),  0.90);
        assertDouble(dist1.quantile(0.99),  0.98);

        assertDouble(dist2.quantile(0.01), -1.95);
        assertDouble(dist2.quantile(0.05), -1.75);
        assertDouble(dist2.quantile(0.25), -0.75);
        assertDouble(dist2.quantile(0.50),  0.50);
        assertDouble(dist2.quantile(0.75),  1.75);
        assertDouble(dist2.quantile(0.95),  2.75);
        assertDouble(dist2.quantile(0.99),  2.95);

        runQuantileTest(dist1, TOLERANCE);
        runQuantileTest(dist2, TOLERANCE);
    }
}
