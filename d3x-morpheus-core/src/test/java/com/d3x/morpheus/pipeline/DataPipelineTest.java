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

import java.util.Map;
import java.util.Random;

import com.d3x.morpheus.stats.Max;
import com.d3x.morpheus.stats.Min;
import com.d3x.morpheus.testng.NumericTestBase;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.vector.DataVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DataPipelineTest extends NumericTestBase {
    private static final double NA = Double.NaN;
    private static final DoubleComparator comparator = DoubleComparator.fixed(0.000001);

    private static final DataVector<String> vector1 =
            DataVector.of(Map.of(
                    "A", -2.0,
                    "B",  1.0,
                    "C",   NA,
                    "D",  3.0,
                    "E", -0.5));

    private static final DataVector<String> vector2 =
            DataVector.of(Map.of(
                    "A", 2.0,
                    "B", 1.0,
                    "C",  NA,
                    "D", 3.0,
                    "E", 0.5));

    private void assertPipeline(DataPipeline pipeline, DataVector<String> vector, double... expectedValues) {
        DataVector<String> actualVector = pipeline.apply(DataVector.copy(vector));
        DataVector<String> expectedVector =
                DataVector.of(Map.of(
                        "A", expectedValues[0],
                        "B", expectedValues[1],
                        "C", expectedValues[2],
                        "D", expectedValues[3],
                        "E", expectedValues[4]));

        assertTrue(actualVector.equalsView(expectedVector, comparator));
    }

    private void assertPipeline1(DataPipeline pipeline, double... expectedValues) {
        assertPipeline(pipeline, vector1, expectedValues);
    }

    private void assertPipeline2(DataPipeline pipeline, double... expectedValues) {
        assertPipeline(pipeline, vector2, expectedValues);
    }

    @Test
    public void testAbs() {
        assertPipeline1(DataPipeline.abs, 2.0, 1.0, NA, 3.0, 0.5);
    }
    
    @Test
    public void testAdd() {
        assertPipeline1(DataPipeline.add(2.5), 0.5, 3.5, NA, 5.5, 2.0);
    }

    @Test
    public void testBound() {
        assertPipeline1(DataPipeline.bound(-1.75, 1.5), -1.75, 1.0, NA, 1.5, -0.5);
    }

    @Test
    public void testComposite() {
        // Order matters...
        assertPipeline1(DataPipeline.composite(DataPipeline.add(0.1), DataPipeline.abs), 1.9, 1.1, NA, 3.1, 0.4);
        assertPipeline1(DataPipeline.composite(DataPipeline.abs, DataPipeline.add(0.1)), 2.1, 1.1, NA, 3.1, 0.6);
    }

    @Test
    public void testDivide() {
        assertPipeline1(DataPipeline.divide(2.0), -1.0, 0.5, NA, 1.5, -0.25);
    }

    @Test
    public void testDemean() {
        assertPipeline1(DataPipeline.demean, -2.375, 0.625, NA, 2.625, -0.875);
    }

    @Test
    public void testFlip() {
        assertPipeline1(DataPipeline.flip, 2.0, -1.0, NA, -3.0, 0.5);
    }

    @Test
    public void testIdentity() {
        assertPipeline1(DataPipeline.identity, -2.0, 1.0, NA, 3.0, -0.5);
    }

    @Test
    public void testLog() {
        assertPipeline2(DataPipeline.log, 0.6931472, 0.0, NA, 1.0986123, -0.6931472);
    }

    @Test
    public void testMultiply() {
        assertPipeline1(DataPipeline.multiply(2.0), -4.0, 2.0, NA, 6.0, -1.0);
    }

    @Test
    public void testReplaceNaN() {
        assertPipeline1(DataPipeline.replaceNaN(8.8), -2.0, 1.0, 8.8, 3.0, -0.5);
    }

    @Test
    public void testSign() {
        // The IEEE standard declares that Double.NaN is greater than all other
        // floating point values, including positive infinity, so its sign is 1.
        assertPipeline1(DataPipeline.sign, -1.0, 1.0, 1.0, 1.0, -1.0);
    }

    @Test
    public void testSqrt() {
        assertPipeline2(DataPipeline.sqrt,  1.4142136, 1.0, NA, 1.7320508, 0.7071068);
    }

    @Test
    public void testStandardize() {
        assertPipeline1(DataPipeline.standardize, -1.1118909, 0.2926029, NA, 1.2289320, -0.4096440);
    }

    @Test
    public void testSubtract() {
        assertPipeline1(DataPipeline.subtract(2.4), -4.4, -1.4, NA, 0.6, -2.9);
    }

    @Test
    public void testTrim() {
        Random random = new Random(20210505);
        DataVector<Integer> vector = DataVector.create();

        for (int key = 0; key < 100000; ++key)
            vector.setElement(key, 2.0 * random.nextDouble());

        DataPipeline.trim(0.05).apply(vector);

        double min = new Min().compute(vector);
        double max = new Max().compute(vector);

        assertEquals(min, 0.10, 0.01);
        assertEquals(max, 1.90, 0.01);
    }
}
