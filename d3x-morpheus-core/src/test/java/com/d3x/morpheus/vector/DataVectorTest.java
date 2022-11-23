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
import java.util.stream.Stream;

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

    @Test
    public void testCollect() {
        runTest(DataVector.collect(Stream.of(
                DataVectorElement.of("A", 1.0),
                DataVectorElement.of("B", 2.0),
                DataVectorElement.of("C", 3.0))));
    }

    @Test
    public void testIsEmpty() {
        DataVector<String> vector = DataVector.of(new HashMap<>());
        assertTrue(vector.isEmpty());

        vector.setElement("A", 1.0);
        assertFalse(vector.isEmpty());
    }

    @Test
    public void testSetElements() {
        DataVector<String> v1 = DataVector.create();
        DataVector<String> v2 = DataVector.create();
        DataVector<String> v3 = DataVector.create();

        v1.setElement("A", 1.0);
        v1.setElement("B", 2.0);

        v2.setElement("B", 3.0);
        v2.setElement("C", 4.0);

        v3.setElement("A", 1.0);
        v3.setElement("B", 3.0);
        v3.setElement("C", 4.0);

        v1.setElements(v2);
        assertTrue(v1.equalsView(v3));
    }

    @Test
    public void testApply() {
        var vector1 = DataVector.create();
        var vector2 = DataVector.create();

        vector1.setElement("A", 25.0);
        vector1.setElement("B", 36.0);
        vector1.setElement("C", 49.0);

        vector2.setElement("A", 5.0);
        vector2.setElement("B", 6.0);
        vector2.setElement("C", 7.0);

        vector1.apply(Math::sqrt);
        assertTrue(vector1.equalsView(vector2));
    }

    @Test
    public void testFilter() {
        var map1 = Map.of("A", 1.0, "B", 0.0, "C", -2.0);
        var map2 = Map.of("A", 1.0);
        var map3 = Map.of("A", 1.0, "C", -2.0);

        var vector1 = DataVector.of(map1);
        var vector2 = DataVector.filter(vector1, x -> x.getValue() > 0.5);
        var vector3 = DataVector.nonZeros(vector1);

        assertTrue(vector1.equalsView(DataVector.of(map1)));
        assertTrue(vector2.equalsView(DataVector.of(map2)));
        assertTrue(vector3.equalsView(DataVector.of(map3)));
    }

    @Test
    public void testSubset() {
        var map1 = Map.of("A", 1.0, "B", 0.0, "C", -2.0);
        var map2 = Map.of("A", 1.0);
        var map3 = Map.of("A", 1.0, "C", -2.0);

        var vector1 = DataVector.of(map1);
        var vector2 = DataVector.subset(vector1, Set.of("A"));
        var vector3 = DataVector.subset(vector1, Set.of("A", "C"));
        var vector4 = DataVector.subset(vector1, Set.of("D", "E"));

        assertTrue(vector1.equalsView(DataVector.of(map1)));
        assertTrue(vector2.equalsView(DataVector.of(map2)));
        assertTrue(vector3.equalsView(DataVector.of(map3)));
        assertTrue(vector4.isEmpty());
    }
}

