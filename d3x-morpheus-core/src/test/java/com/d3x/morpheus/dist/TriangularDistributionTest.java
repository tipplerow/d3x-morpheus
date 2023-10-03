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
public class TriangularDistributionTest extends RealDistributionTestBase {
    private static final int SAMPLE_SIZE = 1000000;
    private static final double TOLERANCE = 1.0E-07;

    private final TriangularDistribution dist1 = TriangularDistribution.KERNEL;
    private final TriangularDistribution dist2 = new TriangularDistribution(-2.0, 3.0, 1.0);

    private final double[] sample1 = dist1.sample(random(), SAMPLE_SIZE);
    private final double[] sample2 = dist2.sample(random(), SAMPLE_SIZE);

    public TriangularDistributionTest() {
        super(TOLERANCE);
    }

    @Test
    public void testDeciles() {
        runDecileTest(dist1, sample1, 0.002, true);
        runDecileTest(dist2, sample2, 0.003, true);
    }

    @Test
    public void testDistributions() {
        runDistributionTest(dist1, 0.00001);
        runDistributionTest(dist2, 0.00001);
    }

    @Test
    public void testMoments() {
        runMomentTest(dist1, SampleMethod.DEFAULT, SAMPLE_SIZE, 0.001, true);
        runMomentTest(dist2, SampleMethod.DEFAULT, SAMPLE_SIZE, 0.003, true);
        runMomentTest(dist1, SampleMethod.REJECT, SAMPLE_SIZE, 0.003, true);
        runMomentTest(dist2, SampleMethod.TRANSFORM, SAMPLE_SIZE, 0.003, true);
    }

    @Test
    public void testQuantile() {
        runQuantileTest(dist1, TOLERANCE);
        runQuantileTest(dist2, TOLERANCE);
    }
}
