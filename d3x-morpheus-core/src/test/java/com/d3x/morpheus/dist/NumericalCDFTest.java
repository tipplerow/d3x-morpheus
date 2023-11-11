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

import com.d3x.morpheus.numerictests.NumericTestBase;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * @author Scott Shaffer
 */
public class NumericalCDFTest extends NumericTestBase {
    private static final double UNIT_STEP = 1.0E-02;
    private static final double THRESHOLD = 1.0E-06;

    @Test
    public void testBiweight() {
        runTest(BiweightKernel.INSTANCE, 0.000001, false);
    }

    @Test
    public void testCosine() {
        runTest(CosineKernel.INSTANCE, 0.000001, false);
    }

    @Test
    public void testEpanechnikov() {
        runTest(EpanechnikovKernel.INSTANCE, 0.000001, false);
    }

    @Test
    public void testExponential() {
        runTest(ExponentialDistribution.UNIT, 0.00001, false);
    }

    @Test
    public void testLaplace() {
        runTest(LaplaceDistribution.UNIT, 0.00001, false);
    }

    @Test
    public void testNormal() {
        runTest(NormalDistribution.STANDARD, 0.000001, false);
    }

    @Test
    public void testTriangular() {
        runTest(TriangularDistribution.KERNEL, 0.000001, false);
    }

    @Test
    public void testUniform() {
        runTest(UniformDistribution.KERNEL, 0.000001, false);
    }

    private void runTest(RealDistribution dist, double tolerance, boolean verbose) {
        var mean = dist.mean();
        var sdev = dist.sdev();
        var minX = Math.max(dist.support().getLower(), mean - 5.0 * sdev);
        var maxX = Math.min(dist.support().getUpper(), mean + 5.0 * sdev);
        var cdf = NumericalCDF.create(dist, UNIT_STEP, THRESHOLD);
        var rand = new Random(194336723L);

        for (int trial = 0; trial < 1000; ++trial) {
            var x = rand.nextDouble(minX, maxX);
            var actual = cdf.applyAsDouble(x);
            var expected = dist.cdf(x);

            if (verbose)
                System.out.printf("%.6f, %.8f, %.8f, %.8f%n", x, actual, expected, Math.abs(actual - expected));

            Assert.assertEquals(actual, expected, tolerance);
        }
    }
}
