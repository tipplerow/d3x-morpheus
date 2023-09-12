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
public class EpanechnikovKernelTest extends RealDistributionTestBase {
    private static final int SAMPLE_SIZE = 1000000;
    private static final double TOLERANCE = 1.0E-07;
    private static final EpanechnikovKernel dist = EpanechnikovKernel.INSTANCE;

    private final double[] sample = dist.sample(random(), SAMPLE_SIZE);

    public EpanechnikovKernelTest() {
        super(TOLERANCE);
    }

    @Test
    public void testDeciles() {
        runDecileTest(dist, sample, 0.002, true);
    }

    @Test
    public void testDistributions() {
        runDistributionTest(dist, 0.00001);
    }

    @Test
    public void testMoments() {
        runMomentTest(dist, SampleMethod.DEFAULT, SAMPLE_SIZE, 0.002, true);
        runMomentTest(dist, SampleMethod.REJECT, SAMPLE_SIZE, 0.002, true);
        runMomentTest(dist, SampleMethod.TRANSFORM, SAMPLE_SIZE, 0.002, true);
    }

    @Test
    public void testQuantile() {
        runQuantileTest(dist, 0.00001);
    }
}
