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

import java.util.List;

/**
 * @author Scott Shaffer
 */
public class LaplaceSumDistributionTest extends RealDistributionTestBase {
    private static final int SAMPLE_SIZE = 1000000;
    private static final boolean VERBOSE = true;

    private final LaplaceDistribution parent1 = LaplaceDistribution.UNIT;
    private final LaplaceDistribution parent2 = new LaplaceDistribution(1.0, 2.0);
    private final LaplaceDistribution parent3 = new LaplaceDistribution(-2.0, 0.25);

    private final LaplaceSumDistribution sum11 = new LaplaceSumDistribution(parent1, 1);
    private final LaplaceSumDistribution sum12 = new LaplaceSumDistribution(parent1, 2);
    private final LaplaceSumDistribution sum13 = new LaplaceSumDistribution(parent1, 3);
    private final LaplaceSumDistribution sum14 = new LaplaceSumDistribution(parent1, 4);
    private final LaplaceSumDistribution sum15 = new LaplaceSumDistribution(parent1, 5);

    private final LaplaceSumDistribution sum21 = new LaplaceSumDistribution(parent2, 1);
    private final LaplaceSumDistribution sum22 = new LaplaceSumDistribution(parent2, 2);
    private final LaplaceSumDistribution sum23 = new LaplaceSumDistribution(parent2, 3);
    private final LaplaceSumDistribution sum24 = new LaplaceSumDistribution(parent2, 4);
    private final LaplaceSumDistribution sum25 = new LaplaceSumDistribution(parent2, 5);

    private final LaplaceSumDistribution sum31 = new LaplaceSumDistribution(parent3, 1);
    private final LaplaceSumDistribution sum32 = new LaplaceSumDistribution(parent3, 2);
    private final LaplaceSumDistribution sum33 = new LaplaceSumDistribution(parent3, 3);
    private final LaplaceSumDistribution sum34 = new LaplaceSumDistribution(parent3, 4);
    private final LaplaceSumDistribution sum35 = new LaplaceSumDistribution(parent3, 5);

    private final double[] sample11 = sum11.sample(random(), SAMPLE_SIZE);
    private final double[] sample12 = sum12.sample(random(), SAMPLE_SIZE);
    private final double[] sample13 = sum13.sample(random(), SAMPLE_SIZE);
    private final double[] sample14 = sum14.sample(random(), SAMPLE_SIZE);
    private final double[] sample15 = sum15.sample(random(), SAMPLE_SIZE);

    private final double[] sample21 = sum21.sample(random(), SAMPLE_SIZE);
    private final double[] sample22 = sum22.sample(random(), SAMPLE_SIZE);
    private final double[] sample23 = sum23.sample(random(), SAMPLE_SIZE);
    private final double[] sample24 = sum24.sample(random(), SAMPLE_SIZE);
    private final double[] sample25 = sum25.sample(random(), SAMPLE_SIZE);

    private final double[] sample31 = sum31.sample(random(), SAMPLE_SIZE);
    private final double[] sample32 = sum32.sample(random(), SAMPLE_SIZE);
    private final double[] sample33 = sum33.sample(random(), SAMPLE_SIZE);
    private final double[] sample34 = sum34.sample(random(), SAMPLE_SIZE);
    private final double[] sample35 = sum35.sample(random(), SAMPLE_SIZE);

    private final List<LaplaceSumDistribution> sums = List.of(
            sum11, sum12, sum13, sum14, sum15,
            sum21, sum22, sum23, sum24, sum25,
            sum31, sum32, sum33, sum34, sum35
    );

    private final List<double[]> samples = List.of(
            sample11, sample12, sample13, sample14, sample15,
            sample21, sample22, sample23, sample24, sample25,
            sample31, sample32, sample33, sample34, sample35
    );

    @Test
    public void testDeciles() {
        for (int k = 0; k < 5; ++k)
            runDecileTest(sums.get(k), samples.get(k), 0.010, VERBOSE);

        for (int k = 5; k < 10; ++k)
            runDecileTest(sums.get(k), samples.get(k), 0.015, VERBOSE);

        for (int k = 10; k < 15; ++k)
            runDecileTest(sums.get(k), samples.get(k), 0.005, VERBOSE);
    }

    @Test
    public void testDistributions() {
        for (var dist : sums)
            runDistributionTest(dist, 0.000001);
    }

    @Test
    public void testMoments() {
        for (int k = 0; k < 5; ++k)
            runMomentTest(sums.get(k), samples.get(k), 0.010, VERBOSE);

        for (int k = 5; k < 10; ++k)
            runMomentTest(sums.get(k), samples.get(k), 0.015, VERBOSE);

        for (int k = 10; k < 15; ++k)
            runMomentTest(sums.get(k), samples.get(k), 0.005, VERBOSE);
    }

    @Test
    public void testQuantiles() {
        for (var dist : sums)
            runQuantileTest(dist, 0.000001);
    }
}
