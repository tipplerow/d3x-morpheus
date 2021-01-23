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

import java.util.List;
import com.d3x.morpheus.frame.DataFrame;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class D3xVectorTest {
    private static final double TOLERANCE = 1.0E-12;
    private final DataFrame<String, String> dataFrame = newFrame();

    /** Row keys for the DataFrame created by {@code newFrame()}. */
    public static final List<String> rowKeys = List.of("row1", "row2", "row3");

    /** Column keys for the DataFrame created by {@code newFrame()}. */
    public static final List<String> colKeys = List.of("col1", "col2", "col3", "col4");

    /** Elements for the DataFrame created by {@code newFrame()}. */
    public static final double[][] finalData =
            new double[][] {
                    { 11.0, 12.0, 13.0, 14.0 },
                    { 21.0, 22.0, 23.0, 24.0 },
                    { 31.0, 32.0, 33.0, 34.0 } };

    /**
     * Creates a new DataFrame for standardized testing.
     *
     * @return a new DataFrame with standardized keys and data.
     */
    public static DataFrame<String, String> newFrame() {
        DataFrame<String, String> frame = DataFrame.ofDoubles(rowKeys, colKeys);

        for (int irow = 0; irow < frame.rowCount(); irow++)
            for (int jcol = 0; jcol < frame.colCount(); jcol++)
                frame.setDoubleAt(irow, jcol, finalData[irow][jcol]);

        return frame;
    }

    @Test
    public void testCombine() {
        double c1 = 3.0;
        double c2 = 2.0;

        double[] arr1 = new double[] {   1.0,   2.0,   3.0 };
        double[] arr2 = new double[] { 100.0, 200.0, 300.0 };
        double[] arr3 = new double[] { 203.0, 406.0, 609.0 };

        D3xVector vec1 = D3xVector.copyOf(arr1);
        D3xVector vec2 = D3xVector.copyOf(arr2);
        D3xVector vec3 = vec1.combine(c1, c2, vec2);

        assertNotSame(vec3, vec1);
        assertTrue(vec1.equalsArray(arr1));
        assertTrue(vec2.equalsArray(arr2));
        assertTrue(vec3.equalsArray(arr3));

        D3xVector vec4 = vec1.combineInPlace(c1, c2, vec2);

        assertSame(vec4, vec1);
        assertTrue(vec1.equalsArray(arr3));
        assertTrue(vec2.equalsArray(arr2));
        assertTrue(vec4.equalsArray(arr3));
    }

    @Test
    public void testMinus() {
        double[] arr1 = new double[] { 10.0, 20.0, 30.0 };
        double[] arr2 = new double[] {  1.0,  2.0,  3.0 };
        double[] arr3 = new double[] {  9.0, 18.0, 27.0 };

        D3xVector vec1 = D3xVector.copyOf(arr1);
        D3xVector vec2 = D3xVector.copyOf(arr2);
        D3xVector vec3 = vec1.minus(vec2);

        assertNotSame(vec3, vec1);
        assertTrue(vec1.equalsArray(arr1));
        assertTrue(vec2.equalsArray(arr2));
        assertTrue(vec3.equalsArray(arr3));

        D3xVector vec4 = vec1.subtractInPlace(vec2);

        assertSame(vec4, vec1);
        assertTrue(vec1.equalsArray(arr3));
        assertTrue(vec2.equalsArray(arr2));
        assertTrue(vec4.equalsArray(arr3));
    }

    @Test
    public void testPlus() {
        double[] arr1 = new double[] {  1.0,  2.0,  3.0 };
        double[] arr2 = new double[] { 10.0, 20.0, 30.0 };
        double[] arr3 = new double[] { 11.0, 22.0, 33.0 };

        D3xVector vec1 = D3xVector.copyOf(arr1);
        D3xVector vec2 = D3xVector.copyOf(arr2);
        D3xVector vec3 = vec1.plus(vec2);

        assertNotSame(vec3, vec1);
        assertTrue(vec1.equalsArray(arr1));
        assertTrue(vec2.equalsArray(arr2));
        assertTrue(vec3.equalsArray(arr3));

        D3xVector vec4 = vec1.addInPlace(vec2);

        assertSame(vec4, vec1);
        assertTrue(vec1.equalsArray(arr3));
        assertTrue(vec2.equalsArray(arr2));
        assertTrue(vec4.equalsArray(arr3));
    }

    @Test
    public void testCopyOfArray() {
        double[] array = new double[] { 1.0, 2.0, 3.0 };
        D3xVector vector = D3xVector.copyOf(array);

        assertEquals(vector.length(), 3);
        assertEquals(vector.get(0), 1.0, TOLERANCE);
        assertEquals(vector.get(1), 2.0, TOLERANCE);
        assertEquals(vector.get(2), 3.0, TOLERANCE);

        vector.set(0, 11.0);
        vector.set(1, 22.0);
        vector.set(2, 33.0);

        // The original array is not changed...
        assertEquals(array[0], 1.0, TOLERANCE);
        assertEquals(array[1], 2.0, TOLERANCE);
        assertEquals(array[2], 3.0, TOLERANCE);

        assertEquals(vector.get(0), 11.0, TOLERANCE);
        assertEquals(vector.get(1), 22.0, TOLERANCE);
        assertEquals(vector.get(2), 33.0, TOLERANCE);
    }

    @Test
    public void testCopyColumn() {
        assertTrue(D3xVector.copyColumn(dataFrame, "col2").equalsArray(12.0, 22.0, 32.0));
        assertTrue(D3xVector.copyColumn(dataFrame, List.of("row3", "row1"), "col4").equalsArray(34.0, 14.0));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCopyColumnInvalid1() {
        D3xVector.copyColumn(dataFrame, "row1");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCopyColumnInvalid2() {
        D3xVector.copyColumn(dataFrame, List.of("col3", "col4"), "col1");
    }

    @Test
    public void testCopyRow() {
        assertTrue(D3xVector.copyRow(dataFrame, "row2").equalsArray(21.0, 22.0, 23.0, 24.0));
        assertTrue(D3xVector.copyRow(dataFrame, "row3", List.of("col4", "col1")).equalsArray(34.0, 31.0));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCopyRowInvalid1() {
        D3xVector.copyRow(dataFrame, "col1");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCopyRowInvalid2() {
        D3xVector.copyRow(dataFrame, "row1", List.of("row5", "row6"));
    }

    @Test
    public void testCopyOfSelf() {
        D3xVector orig = D3xVector.copyOf(1.0, 2.0, 3.0);
        D3xVector copy = orig.copy();

        assertEquals(copy.length(), 3);
        assertEquals(copy.get(0), 1.0, TOLERANCE);
        assertEquals(copy.get(1), 2.0, TOLERANCE);
        assertEquals(copy.get(2), 3.0, TOLERANCE);

        copy.set(0, 11.0);
        copy.set(1, 22.0);
        copy.set(2, 33.0);

        // The original vector is not changed...
        assertEquals(orig.get(0), 1.0, TOLERANCE);
        assertEquals(orig.get(1), 2.0, TOLERANCE);
        assertEquals(orig.get(2), 3.0, TOLERANCE);

        assertEquals(copy.get(0), 11.0, TOLERANCE);
        assertEquals(copy.get(1), 22.0, TOLERANCE);
        assertEquals(copy.get(2), 33.0, TOLERANCE);
    }

    @Test
    public void testDense() {
        D3xVector vector = D3xVector.dense(3);
        assertEquals(vector.length(), 3);

        assertEquals(vector.get(0), 0.0, TOLERANCE);
        assertEquals(vector.get(1), 0.0, TOLERANCE);
        assertEquals(vector.get(2), 0.0, TOLERANCE);

        vector.set(0, 11.0);
        vector.set(1, 22.0);
        vector.set(2, 33.0);

        assertEquals(vector.get(0), 11.0, TOLERANCE);
        assertEquals(vector.get(1), 22.0, TOLERANCE);
        assertEquals(vector.get(2), 33.0, TOLERANCE);
    }

    @Test
    public void testSparse() {
        D3xVector vector = D3xVector.sparse(1000);
        assertEquals(vector.length(), 1000);

        assertEquals(vector.get(0), 0.0, TOLERANCE);
        assertEquals(vector.get(1), 0.0, TOLERANCE);
        assertEquals(vector.get(2), 0.0, TOLERANCE);

        vector.set(0, 11.0);
        vector.set(1, 22.0);
        vector.set(2, 33.0);

        assertEquals(vector.get(0), 11.0, TOLERANCE);
        assertEquals(vector.get(1), 22.0, TOLERANCE);
        assertEquals(vector.get(2), 33.0, TOLERANCE);
    }

    @Test
    public void testWrap() {
        double[] array = new double[] { 1.0, 2.0, 3.0 };
        D3xVector vector = D3xVector.wrap(array);

        assertEquals(vector.length(), 3);
        assertEquals(vector.get(0), 1.0, TOLERANCE);
        assertEquals(vector.get(1), 2.0, TOLERANCE);
        assertEquals(vector.get(2), 3.0, TOLERANCE);

        vector.set(0, 11.0);
        vector.set(1, 22.0);
        vector.set(2, 33.0);

        // The original array is also changed...
        assertEquals(array[0], 11.0, TOLERANCE);
        assertEquals(array[1], 22.0, TOLERANCE);
        assertEquals(array[2], 33.0, TOLERANCE);

        assertEquals(vector.get(0), 11.0, TOLERANCE);
        assertEquals(vector.get(1), 22.0, TOLERANCE);
        assertEquals(vector.get(2), 33.0, TOLERANCE);
    }
}

