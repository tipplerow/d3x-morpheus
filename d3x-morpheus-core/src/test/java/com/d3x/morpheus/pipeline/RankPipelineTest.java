/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.pipeline;

import com.d3x.morpheus.numerictests.NumericTestBase;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.DataVector;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class RankPipelineTest extends NumericTestBase {
    private static final double NA = Double.NaN;
    private static final double NEG_INF = Double.NEGATIVE_INFINITY;
    private static final double POS_INF = Double.POSITIVE_INFINITY;

    private void runTest(double lower, double upper, double[] original, double[] expected) {
        var pipeline = DataPipeline.rank(lower, upper);
        var actualD3x = pipeline.apply(D3xVector.copyOf(original));
        Assert.assertTrue(actualD3x.equalsArray(expected));

        var actualData = DataVector.create();
        var expectedData = DataVector.create();

        for (var index = 0; index < original.length; ++index) {
            var key = Character.toString(65 + index);
            actualData.setElement(key, original[index]);
            expectedData.setElement(key, expected[index]);
        }

        pipeline.apply(actualData);
        Assert.assertTrue(actualData.equalsView(expectedData));
    }

    @Test
    public void testRank() {
        runTest(0.0, 1.0, new double[0], new double[0]);
        runTest(0.0, 1.0, new double[] { NA }, new double[] { NA });
        runTest(0.0, 1.0, new double[] { 88.88 }, new double[] { 0.5 });
        runTest(-1.0, 1.0, new double[] { 88.88 }, new double[] { 0.0 });
        runTest(-1.0, 5.0, new double[] { 88.88 }, new double[] { 2.0 });

        var original   = new double[] { 4.0, -2.0, NA, 0.0, POS_INF, 0.0, NEG_INF, 0.0, NA, 1.1 };
        var expected01 = new double[] { 0.8,  0.2, NA, 0.4,     1.0, 0.4,     0.0, 0.4, NA, 0.6 };
        var expected16 = new double[] { 5.0,  2.0, NA, 3.0,     6.0, 3.0,     1.0, 3.0, NA, 4.0 };

        runTest(0.0, 1.0, original, expected01);
        runTest(1.0, 6.0, original, expected16);
    }
}
