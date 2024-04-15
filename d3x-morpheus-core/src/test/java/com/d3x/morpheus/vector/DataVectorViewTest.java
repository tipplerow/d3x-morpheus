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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.numerictests.NumericTestBase;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DataVectorViewTest extends NumericTestBase {
    private static final double TOLERANCE = 1.0E-12;

    private static final List<String> keyList = List.of("A", "B", "C");
    private static final List<Double> valueList = List.of(1.0, 2.0, 3.0);
    private static final double[] valueVector = new double[] { 1.0, 2.0, 3.0 };
    private static final DataVectorView<String> baseView = DoubleSeries.build(String.class, keyList, valueList);

    private void runTest(DataVectorView<String> vector) {
        assertEquals(3, vector.length());
        assertEquals(vector.collectKeys(), Set.copyOf(keyList));

        assertEquals(vector.getElement("A"), 1.0, TOLERANCE);
        assertEquals(vector.getElement("B"), 2.0, TOLERANCE);
        assertEquals(vector.getElement("C"), 3.0, TOLERANCE);

        assertEquals(vector.getElement("A", 0.0), 1.0, TOLERANCE);
        assertEquals(vector.getElement("B", 0.0), 2.0, TOLERANCE);
        assertEquals(vector.getElement("C", 0.0), 3.0, TOLERANCE);
        assertEquals(vector.getElement("D", 0.0), 0.0, TOLERANCE);

        assertTrue(vector.equalsView(baseView));

        Collection<DataVectorElement<String>> elements = vector.collectElements();

        assertEquals(3, elements.size());
        assertTrue(elements.contains(DataVectorElement.of("A", 1.0)));
        assertTrue(elements.contains(DataVectorElement.of("B", 2.0)));
        assertTrue(elements.contains(DataVectorElement.of("C", 3.0)));
    }

    @Test
    public void testEmpty() {
        DataVectorView<String> view1 = DataVectorView.empty();
        DataVectorView<Integer> view2 = DataVectorView.empty();

        assertEquals(view1.length(), 0);
        assertEquals(view2.length(), 0);

        assertFalse(view1.containsElement("abc"));
        assertFalse(view2.containsElement(88));
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
        runTest(DataVectorView.of(Map.of("A", 1.0, "B", 2.0, "C", 3.0)));
    }

    @Test
    public void testNorm1() {
        DoubleSeries<String> vector = DoubleSeries.build(String.class, List.of("A", "B", "C"), List.of(1.0, -2.0, 3.0));
        assertDouble(vector.norm1(), 6.0);
    }

    @Test
    public void testNorm2() {
        DoubleSeries<String> vector = DoubleSeries.build(String.class, List.of("A", "B", "C"), List.of(1.0, -2.0, 3.0));
        assertDouble(baseView.norm2(), Math.sqrt(14.0));
    }

    @Test
    public void testPlusMinus() {
        var v1 = DataVector.create(String.class);
        var v2 = DataVector.create(String.class);

        v1.setElement("A", 11.0);
        v1.setElement("B", 12.0);
        v2.setElement("B", 22.0);
        v2.setElement("C", 23.0);

        var v3 = v1.plus(v2);
        var v4 = v1.minus(v2);

        assertEquals(v1.collectKeys(), Set.of("A", "B"));
        assertEquals(v2.collectKeys(), Set.of("B", "C"));
        assertEquals(v3.collectKeys(), Set.of("A", "B", "C"));
        assertEquals(v4.collectKeys(), Set.of("A", "B", "C"));
        assertEquals(v1.getElement("A"), 11.0, TOLERANCE);
        assertEquals(v1.getElement("B"), 12.0, TOLERANCE);
        assertEquals(v2.getElement("B"), 22.0, TOLERANCE);
        assertEquals(v2.getElement("C"), 23.0, TOLERANCE);
        assertEquals(v3.getElement("A"), 11.0, TOLERANCE);
        assertEquals(v3.getElement("B"), 34.0, TOLERANCE);
        assertEquals(v3.getElement("C"), 23.0, TOLERANCE);
        assertEquals(v4.getElement("A"), 11.0, TOLERANCE);
        assertEquals(v4.getElement("B"), -10.0, TOLERANCE);
        assertEquals(v4.getElement("C"), -23.0, TOLERANCE);
    }

    @Test
    public void testSeries() {
        runTest(DoubleSeries.build(String.class, keyList, valueList));
    }

    @Test
    public void testInnerProduct() {
        DataVectorView<String> s1 =
                DoubleSeries.build(String.class, List.of("A", "B", "C", "D", "E"), List.of(1.0, 2.0, 3.0, 4.0, 5.0));

        DataVectorView<String> s2 =
                DataFrame.ofDoubles("row", List.of("B", "C", "D", "F", "G", "H"), new double[] { 10.0, 20.0, 30.0, 40.0, 50.0, 60.0 }).row("row");

        DataVectorView<String > wt =
                DoubleSeries.build(String.class, List.of("C", "D"), List.of(0.25, 0.75));

        assertEquals(s1.innerProduct(s1), 55.0, TOLERANCE);
        assertEquals(s1.innerProduct(s2), 2.0 * 10.0 + 3.0 * 20.0 + 4.0 * 30.0, TOLERANCE);
        assertEquals(s2.innerProduct(s1), 2.0 * 10.0 + 3.0 * 20.0 + 4.0 * 30.0, TOLERANCE);

        assertEquals(s1.innerProduct(s1, wt), 14.25, TOLERANCE);
        assertEquals(s1.innerProduct(s2, wt), 0.25 * 3.0 * 20.0 + 0.75 * 4.0 * 30.0, TOLERANCE);
        assertEquals(s2.innerProduct(s1, wt), 0.25 * 3.0 * 20.0 + 0.75 * 4.0 * 30.0, TOLERANCE);
    }

    @Test
    public void testPad() {
        DataVectorView<String> padded = DataVectorView.pad(baseView, List.of("A", "B", "F", "G"), 0.2);
        DataVectorView<String> expected = DoubleSeries.build(String.class, List.of("A", "B", "F", "G"), List.of(1.0, 2.0, 0.2, 0.2));

        assertTrue(padded.equalsView(expected));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRequireElementAbsent() {
        baseView.requireElement("foo");
    }

    @Test
    public void testRequireElementPresent() {
        baseView.requireElement("A");
    }

    @Test
    public void testToSeries() {
        DataVectorView<String> view1 = DataVector.of(Map.of("A", 1.0, "B", 2.0, "C", 3.0));
        DataVectorView<String> view2 = view1.toSeries(String.class);

        assertSame(baseView.toSeries(String.class), baseView);
        assertNotSame(view1, view2);
        assertTrue(view1.equalsView(view2));
    }

    @Test
    public void testStream() {
        DataVectorView<String> view = DataVector.of(Map.of("A", 1.0, "B", 2.0, "C", 3.0));
        assertEquals(view.streamKeys().count(), 3);
        assertEquals(view.streamValues().count(), 3);
        assertEquals(view.streamElements().count(), 3);
        assertEquals(view.streamKeys().collect(Collectors.toSet()), Set.of("A", "B", "C"));
        assertEquals(view.streamValues().sum(), 6.0, TOLERANCE);
        assertEquals(view.streamElements().collect(Collectors.toSet()),
                Set.of(DataVectorElement.of("A", 1.0),
                        DataVectorElement.of("B", 2.0),
                        DataVectorElement.of("C", 3.0)));
    }

    @Test
    public void testWeightedMean() {
        var target = DataVector.of(Map.of("B", 1.0, "C", 3.0, "D", 5.0, "E", 7.0, "F", 88.8));
        var weight = DataVector.of(Map.of("A", 99.9, "B", 2.0, "C", 1.5, "D", 1.0, "E", 0.5));
        assertEquals(target.weightedMean(weight), 3.0, TOLERANCE);
    }
}

