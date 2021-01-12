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
package com.d3x.morpheus.apache;

import java.util.List;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ApacheMatrixTest extends ApacheTestBase {
    private final List<String> partialRows = List.of("row3", "row1");
    private final List<String> partialCols = List.of("col2", "col4", "col1");

    private final ApacheMatrix<String, String> fullMatrix = ApacheMatrix.wrap(finalFrame);
    private final ApacheMatrix<String, String> partialMatrix = ApacheMatrix.wrap(finalFrame, partialRows, partialCols);

    private static final double[][] unequalData =
            new double[][]{
                    {11.0, 12.0, 13.0, 14.0},
                    {21.0, 22.0, 23.0, 24.0},
                    {31.0, 32.0, 88.0, 34.0}}; // Note the 88.0 in this row...

    private static final RealMatrix equalMatrix = new Array2DRowRealMatrix(finalData);
    private static final RealMatrix unequalMatrix = new Array2DRowRealMatrix(unequalData);

    private static final double[][] partialData =
            new double[][]{
                    {32.0, 34.0, 31.0},
                    {12.0, 14.0, 11.0}
            };

    @Test
    public void testColumnKeys() {
        assertEquals(fullMatrix.getColKeys(), colKeys);
        assertEquals(partialMatrix.getColKeys(), partialCols);
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testColumnKeyMissing() {
        ApacheMatrix.wrap(finalFrame, rowKeys, List.of("col8"));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testColKeysImmutable() {
        fullMatrix.getColKeys().set(0, "foo");
    }

    @Test
    public void testRowKeys() {
        assertEquals(fullMatrix.getRowKeys(), rowKeys);
        assertEquals(partialMatrix.getRowKeys(), partialRows);
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRowKeysMissing() {
        ApacheMatrix.wrap(finalFrame, List.of("row77"), colKeys);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testRowKeysImmutable() {
        fullMatrix.getRowKeys().set(0, "foo");
    }

    @Test
    public void testEquals() {
        assertTrue(fullMatrix.equals(equalMatrix));
        assertFalse(fullMatrix.equals(unequalMatrix));
    }

    @Test
    public void testEqualsData() {
        assertTrue(fullMatrix.equalsData(finalData));
        assertFalse(fullMatrix.equalsData(unequalData));
    }

    @Test
    public void testEqualsMatrix() {
        assertTrue(fullMatrix.equalsMatrix(equalMatrix));
        assertFalse(fullMatrix.equalsMatrix(unequalMatrix));
    }

    @Test
    public void testGet() {
        assertTrue(comparator.equals(fullMatrix.getData(), finalData));
        assertTrue(comparator.equals(partialMatrix.getData(), partialData));
    }

    @Test
    public void testGetDimensions() {
        assertEquals(fullMatrix.getRowDimension(), 3);
        assertEquals(fullMatrix.getColumnDimension(), 4);

        assertEquals(partialMatrix.getRowDimension(), 2);
        assertEquals(partialMatrix.getColumnDimension(), 3);
    }

    @Test
    public void testSet1() {
        //
        // Create a new local frame so the finalFrame is not modified...
        //
        DataFrame<String, String> frame = newFrame();
        ApacheMatrix<String, String> matrix = ApacheMatrix.wrap(frame);

        assertEquals(matrix.getEntry(1, 2), 23.0, TOLERANCE);
        assertEquals(frame.getDouble("row2", "col3"), 23.0, TOLERANCE);

        matrix.setEntry(1, 2, 345.0);

        assertEquals(matrix.getEntry(1, 2), 345.0, TOLERANCE);
        assertEquals(frame.getDouble("row2", "col3"), 345.0, TOLERANCE);

        assertEquals(matrix.getEntry(2, 1), 32.0, TOLERANCE);
        assertEquals(frame.getDouble("row3", "col2"), 32.0, TOLERANCE);

        matrix.setEntry(2, 1, 678.0);

        assertEquals(matrix.getEntry(2, 1), 678.0, TOLERANCE);
        assertEquals(frame.getDouble("row3", "col2"), 678.0, TOLERANCE);
    }

    @Test
    public void testSet2() {
        //
        // Create a new local frame so the finalFrame is not modified...
        //
        DataFrame<String, String> frame = newFrame();
        ApacheMatrix<String, String> matrix = ApacheMatrix.wrap(frame, partialRows, partialCols);

        // Note the difference in ordinal indexes created by the partial row and column keys...
        assertEquals(matrix.getEntry(0, 1), 34.0, TOLERANCE);
        assertEquals(frame.getDouble("row3", "col4"), 34.0, TOLERANCE);

        matrix.setEntry(0, 1, 888.0);

        assertEquals(matrix.getEntry(0, 1), 888.0, TOLERANCE);
        assertEquals(frame.getDouble("row3", "col4"), 888.0, TOLERANCE);
    }
}
