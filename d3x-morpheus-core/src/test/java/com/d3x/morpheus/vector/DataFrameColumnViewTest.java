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

import org.apache.commons.math3.linear.ArrayRealVector;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DataFrameColumnViewTest {
    private final DataFrame<String, String> finalFrame = newFrame();

    private final DataFrameColumnView<String, String> finalCol2 = DataFrameColumnView.of(finalFrame, "col2");
    private final DataFrameColumnView<String, String> finalCol4 = DataFrameColumnView.of(finalFrame, "col4");

    private static final double TOLERANCE = 1.0E-12;

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
    public void testColumnKey() {
        assertEquals(finalCol2.getColKey(), "col2");
        assertEquals(finalCol4.getColKey(), "col4");
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testColumnKeyMissing() {
        DataFrameColumnView.of(finalFrame, "row2");
    }

    @Test
    public void testRowKeys() {
        assertEquals(finalCol2.getRowKeys(), rowKeys);
        assertEquals(DataFrameColumnView.of(finalFrame, List.of("row3", "row1"), "col1").getRowKeys(), List.of("row3", "row1"));
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRowKeysMissing() {
        DataFrameColumnView.of(finalFrame, List.of("row1", "col3"), "col2");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRowKeysImmutable() {
        finalCol2.getRowKeys().set(0, "foo");
    }

    @Test
    public void testEquals() {
        assertTrue(finalCol2.equals(new ArrayRealVector(new double[] { 12.0, 22.0, 32.0 })));
        assertTrue(finalCol4.equals(new ArrayRealVector(new double[] { 14.0, 24.0, 34.0 })));

        assertFalse(finalCol2.equals(new double[] { 12.0, 22.0, 32.0 }));
        assertFalse(finalCol4.equals(new double[] { 14.0, 24.0, 34.0 }));
    }

    @Test
    public void testEqualsArray() {
        assertTrue(finalCol2.equalsArray(12.0, 22.0, 32.0));
        assertTrue(finalCol4.equalsArray(14.0, 24.0, 34.0));

        assertFalse(finalCol2.equalsArray(32.0, 22.0, 12.0));
        assertFalse(finalCol4.equalsArray(34.0, 24.0, 14.0));
    }

    @Test
    public void testEqualsVector() {
        assertTrue(finalCol2.equalsVector(D3xVector.of(12.0, 22.0, 32.0)));
        assertTrue(finalCol4.equalsVector(D3xVector.of(14.0, 24.0, 34.0)));

        assertFalse(finalCol2.equalsVector(D3xVector.of(12.0, 22.0, 12.0)));
        assertFalse(finalCol4.equalsVector(D3xVector.of(14.0, 24.0, 14.0)));
    }

    @Test
    public void testGet() {
        assertEquals(finalCol2.getEntry(0), 12.0, TOLERANCE);
        assertEquals(finalCol2.getEntry(1), 22.0, TOLERANCE);
        assertEquals(finalCol2.getEntry(2), 32.0, TOLERANCE);

        assertEquals(finalCol4.getEntry(0), 14.0, TOLERANCE);
        assertEquals(finalCol4.getEntry(1), 24.0, TOLERANCE);
        assertEquals(finalCol4.getEntry(2), 34.0, TOLERANCE);

        // Test a subset of the rows, in different order...
        DataFrameColumnView<String, String> vec = DataFrameColumnView.of(finalFrame, List.of("row3", "row1"), "col4");

        assertEquals(vec.getEntry(0), 34.0, TOLERANCE);
        assertEquals(vec.getEntry(1), 14.0, TOLERANCE);
    }

    @Test
    public void testGetDimension() {
        assertEquals(finalCol2.getDimension(), 3);
        assertEquals(DataFrameColumnView.of(finalFrame, List.of("row3"), "col4").getDimension(), 1);
        assertEquals(DataFrameColumnView.of(finalFrame, List.of("row3", "row1"), "col4").getDimension(), 2);
    }

    @Test
    public void testSet() {
        //
        // Create a new local frame so the finalFrame is not modified...
        //
        DataFrame<String, String> localFrame = newFrame();

        DataFrameColumnView<String, String> col1 = DataFrameColumnView.of(localFrame, "col1");
        DataFrameColumnView<String, String> col3 = DataFrameColumnView.of(localFrame, List.of("row3", "row2"), "col3");

        // Ensure that the entry assignment changes BOTH the DataFrameColumnView AND the underlying DataFrame...
        assertEquals(col1.getEntry(0), 11.0, TOLERANCE);
        assertEquals(col1.getEntry(1), 21.0, TOLERANCE);
        assertEquals(col1.getEntry(2), 31.0, TOLERANCE);

        assertEquals(localFrame.getDouble("row1", "col1"), 11.0, TOLERANCE);
        assertEquals(localFrame.getDouble("row2", "col1"), 21.0, TOLERANCE);
        assertEquals(localFrame.getDouble("row3", "col1"), 31.0, TOLERANCE);

        col1.setEntry(0, 333.0);
        col1.setEntry(1, 444.0);
        col1.setEntry(2, 555.0);

        assertEquals(col1.getEntry(0), 333.0, TOLERANCE);
        assertEquals(col1.getEntry(1), 444.0, TOLERANCE);
        assertEquals(col1.getEntry(2), 555.0, TOLERANCE);

        assertEquals(localFrame.getDouble("row1", "col1"), 333.0, TOLERANCE);
        assertEquals(localFrame.getDouble("row2", "col1"), 444.0, TOLERANCE);
        assertEquals(localFrame.getDouble("row3", "col1"), 555.0, TOLERANCE);

        assertEquals(col3.getEntry(0), 33.0, TOLERANCE);
        assertEquals(col3.getEntry(1), 23.0, TOLERANCE);

        assertEquals(localFrame.getDouble("row3", "col3"), 33.0, TOLERANCE);
        assertEquals(localFrame.getDouble("row2", "col3"), 23.0, TOLERANCE);

        col3.setEntry(0, 777.0);
        col3.setEntry(1, 888.0);

        assertEquals(col3.getEntry(0), 777.0, TOLERANCE);
        assertEquals(col3.getEntry(1), 888.0, TOLERANCE);

        assertEquals(localFrame.getDouble("row3", "col3"), 777.0, TOLERANCE);
        assertEquals(localFrame.getDouble("row2", "col3"), 888.0, TOLERANCE);
    }
}
