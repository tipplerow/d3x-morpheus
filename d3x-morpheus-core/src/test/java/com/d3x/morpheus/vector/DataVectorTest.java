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
package com.d3x.morpheus.vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.d3x.morpheus.frame.DataFrame;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DataVectorTest {
    private static final double TOLERANCE = 1.0E-12;

    private static final List<String> keyList = List.of("A", "B", "C");
    private static final double[] valueVector = new double[] { 1.0, 2.0, 3.0 };

    private void runTest(DataVector<String> vector) {
        assertEquals(3, vector.length());
        assertEquals(vector.collectKeys(), Set.copyOf(keyList));

        assertEquals(vector.getElement("A"), 1.0, TOLERANCE);
        assertEquals(vector.getElement("B"), 2.0, TOLERANCE);
        assertEquals(vector.getElement("C"), 3.0, TOLERANCE);

        assertEquals(vector.getElement("A", 0.0), 1.0, TOLERANCE);
        assertEquals(vector.getElement("B", 0.0), 2.0, TOLERANCE);
        assertEquals(vector.getElement("C", 0.0), 3.0, TOLERANCE);
        assertEquals(vector.getElement("D", 0.0), 0.0, TOLERANCE);

        vector.setElement("A", 11.0);
        vector.setElement("B", 22.0);
        vector.setElement("C", 33.0);

        assertEquals(vector.getElement("A"), 11.0, TOLERANCE);
        assertEquals(vector.getElement("B"), 22.0, TOLERANCE);
        assertEquals(vector.getElement("C"), 33.0, TOLERANCE);

        assertEquals(vector.getElement("A", 0.0), 11.0, TOLERANCE);
        assertEquals(vector.getElement("B", 0.0), 22.0, TOLERANCE);
        assertEquals(vector.getElement("C", 0.0), 33.0, TOLERANCE);
        assertEquals(vector.getElement("D", 0.0),  0.0, TOLERANCE);
    }

    @Test
    public void testFrameRow() {
        runTest(DataFrame.ofDoubles("row", keyList, valueVector).row("row"));
    }

    @Test
    public void testFrameColumn() {
        runTest(DataFrame.ofDoubles(keyList, "col", valueVector).col("col"));
    }

    @Test
    public void testMap() {
        Map<String, Double> map = new HashMap<>();

        map.put("A", 1.0);
        map.put("B", 2.0);
        map.put("C", 3.0);

        runTest(DataVector.of(map));
    }
}

