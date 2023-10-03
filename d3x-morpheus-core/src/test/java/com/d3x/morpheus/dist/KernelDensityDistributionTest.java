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
public class KernelDensityDistributionTest extends RealDistributionTestBase {
    private static final int SOURCE_SAMPLE_SIZE = 1000;
    private static final int KERNEL_SAMPLE_SIZE = 100000;

    private final RealDistribution sourceDist1 = NormalDistribution.STANDARD;
    private final RealDistribution sourceDist2 = new ExponentialDistribution(0.5);
    private final RealDistribution sourceDist3 = new LaplaceDistribution(1.0, 2.0);

    private final double[] sourceSample1 = sample(sourceDist1, SampleMethod.DEFAULT, SOURCE_SAMPLE_SIZE);
    private final double[] sourceSample2 = sample(sourceDist2, SampleMethod.DEFAULT, SOURCE_SAMPLE_SIZE);
    private final double[] sourceSample3 = sample(sourceDist3, SampleMethod.DEFAULT, SOURCE_SAMPLE_SIZE);

    private final KernelDensityDistribution kernelDist11 = new KernelDensityDistribution(sourceSample1, KernelType.UNIFORM);
    private final KernelDensityDistribution kernelDist12 = new KernelDensityDistribution(sourceSample1, KernelType.TRIANGULAR);
    private final KernelDensityDistribution kernelDist13 = new KernelDensityDistribution(sourceSample1, KernelType.EPANECHNIKOV);
    private final KernelDensityDistribution kernelDist14 = new KernelDensityDistribution(sourceSample1, KernelType.BIWEIGHT);
    private final KernelDensityDistribution kernelDist15 = new KernelDensityDistribution(sourceSample1, KernelType.GAUSSIAN);
    private final KernelDensityDistribution kernelDist16 = new KernelDensityDistribution(sourceSample1, KernelType.COSINE);

    private final KernelDensityDistribution kernelDist21 = new KernelDensityDistribution(sourceSample2, KernelType.UNIFORM);
    private final KernelDensityDistribution kernelDist22 = new KernelDensityDistribution(sourceSample2, KernelType.TRIANGULAR);
    private final KernelDensityDistribution kernelDist23 = new KernelDensityDistribution(sourceSample2, KernelType.EPANECHNIKOV);
    private final KernelDensityDistribution kernelDist24 = new KernelDensityDistribution(sourceSample2, KernelType.BIWEIGHT);
    private final KernelDensityDistribution kernelDist25 = new KernelDensityDistribution(sourceSample2, KernelType.GAUSSIAN);
    private final KernelDensityDistribution kernelDist26 = new KernelDensityDistribution(sourceSample2, KernelType.COSINE);

    private final KernelDensityDistribution kernelDist31 = new KernelDensityDistribution(sourceSample3, KernelType.UNIFORM);
    private final KernelDensityDistribution kernelDist32 = new KernelDensityDistribution(sourceSample3, KernelType.TRIANGULAR);
    private final KernelDensityDistribution kernelDist33 = new KernelDensityDistribution(sourceSample3, KernelType.EPANECHNIKOV);
    private final KernelDensityDistribution kernelDist34 = new KernelDensityDistribution(sourceSample3, KernelType.BIWEIGHT);
    private final KernelDensityDistribution kernelDist35 = new KernelDensityDistribution(sourceSample3, KernelType.GAUSSIAN);
    private final KernelDensityDistribution kernelDist36 = new KernelDensityDistribution(sourceSample3, KernelType.COSINE);

    private final double[] kernelSample11 = kernelDist11.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample12 = kernelDist12.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample13 = kernelDist13.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample14 = kernelDist14.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample15 = kernelDist15.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample16 = kernelDist16.sample(random(), KERNEL_SAMPLE_SIZE);

    private final double[] kernelSample21 = kernelDist21.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample22 = kernelDist22.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample23 = kernelDist23.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample24 = kernelDist24.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample25 = kernelDist25.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample26 = kernelDist26.sample(random(), KERNEL_SAMPLE_SIZE);

    private final double[] kernelSample31 = kernelDist31.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample32 = kernelDist32.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample33 = kernelDist33.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample34 = kernelDist34.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample35 = kernelDist35.sample(random(), KERNEL_SAMPLE_SIZE);
    private final double[] kernelSample36 = kernelDist36.sample(random(), KERNEL_SAMPLE_SIZE);

    @Test
    public void testDeciles() {
        runDecileTest(kernelDist11, kernelSample11, 0.02, true);
        runDecileTest(kernelDist12, kernelSample12, 0.02, true);
        runDecileTest(kernelDist13, kernelSample13, 0.02, true);
        runDecileTest(kernelDist14, kernelSample14, 0.02, true);
        runDecileTest(kernelDist15, kernelSample15, 0.02, true);
        runDecileTest(kernelDist16, kernelSample16, 0.02, true);

        runDecileTest(kernelDist21, kernelSample21, 0.04, true);
        runDecileTest(kernelDist22, kernelSample22, 0.05, true);
        runDecileTest(kernelDist23, kernelSample23, 0.04, true);
        runDecileTest(kernelDist24, kernelSample24, 0.04, true);
        runDecileTest(kernelDist25, kernelSample25, 0.04, true);
        runDecileTest(kernelDist26, kernelSample26, 0.04, true);

        runDecileTest(kernelDist31, kernelSample31, 0.05, true);
        runDecileTest(kernelDist32, kernelSample32, 0.05, true);
        runDecileTest(kernelDist33, kernelSample33, 0.05, true);
        runDecileTest(kernelDist34, kernelSample34, 0.05, true);
        runDecileTest(kernelDist35, kernelSample35, 0.05, true);
        runDecileTest(kernelDist36, kernelSample36, 0.05, true);
    }

    @Test
    public void testDistributions() {
        runDistributionTest(kernelDist11, 0.00001);
        runDistributionTest(kernelDist12, 0.00001);
        runDistributionTest(kernelDist13, 0.00001);
        runDistributionTest(kernelDist14, 0.00001);
        runDistributionTest(kernelDist15, 0.00001);
        runDistributionTest(kernelDist16, 0.00001);

        runDistributionTest(kernelDist21, 0.00001);
        runDistributionTest(kernelDist22, 0.00001);
        runDistributionTest(kernelDist23, 0.00001);
        runDistributionTest(kernelDist24, 0.00001);
        runDistributionTest(kernelDist25, 0.00001);
        runDistributionTest(kernelDist26, 0.00001);

        runDistributionTest(kernelDist31, 0.0002);
        runDistributionTest(kernelDist32, 0.0002);
        runDistributionTest(kernelDist33, 0.0002);
        runDistributionTest(kernelDist34, 0.0002);
        runDistributionTest(kernelDist35, 0.0002);
        runDistributionTest(kernelDist36, 0.0002);
    }

    @Test
    public void testMoments() {
        runMomentTest(kernelDist11, kernelSample11, 0.03, true);
        runMomentTest(kernelDist12, kernelSample12, 0.03, true);
        runMomentTest(kernelDist13, kernelSample13, 0.03, true);
        runMomentTest(kernelDist14, kernelSample14, 0.03, true);
        runMomentTest(kernelDist15, kernelSample15, 0.03, true);
        runMomentTest(kernelDist16, kernelSample16, 0.03, true);

        runMomentTest(kernelDist21, kernelSample21, 0.03, true);
        runMomentTest(kernelDist22, kernelSample22, 0.05, true);
        runMomentTest(kernelDist23, kernelSample23, 0.03, true);
        runMomentTest(kernelDist24, kernelSample24, 0.04, true);
        runMomentTest(kernelDist25, kernelSample25, 0.04, true);
        runMomentTest(kernelDist26, kernelSample26, 0.04, true);

        runMomentTest(kernelDist31, kernelSample31, 0.03, true);
        runMomentTest(kernelDist32, kernelSample32, 0.05, true);
        runMomentTest(kernelDist33, kernelSample33, 0.03, true);
        runMomentTest(kernelDist34, kernelSample34, 0.04, true);
        runMomentTest(kernelDist35, kernelSample35, 0.04, true);
        runMomentTest(kernelDist36, kernelSample36, 0.04, true);
    }
}
