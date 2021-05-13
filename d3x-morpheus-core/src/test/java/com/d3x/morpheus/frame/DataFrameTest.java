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
package com.d3x.morpheus.frame;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.vector.D3xVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Tests default methods implemented in the DataFrame interface.
 */
public class DataFrameTest extends DataFrameTestBase {
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testContainsColumn() {
        assertTrue(intFrame.containsColumn(col1));
        assertTrue(intFrame.containsColumn(col2));
        assertTrue(intFrame.containsColumn(col3));
        assertFalse(intFrame.containsColumn(col4));

    }

    @Test
    public void testContainsColumns() {
        assertTrue(intFrame.containsColumns(List.of(col1)));
        assertTrue(intFrame.containsColumns(List.of(col1, col2)));
        assertTrue(intFrame.containsColumns(List.of(col1, col2, col3)));
        assertFalse(intFrame.containsColumns(List.of(col1, col2, col3, col4)));
    }

    @Test
    public void testContainsRow() {
        assertTrue(intFrame.containsRow(row1));
        assertTrue(intFrame.containsRow(row2));
        assertFalse(intFrame.containsRow(row4));
    }

    @Test
    public void testContainsRows() {
        assertTrue(intFrame.containsRows(List.of(row1)));
        assertTrue(intFrame.containsRows(List.of(row1, row2)));
        assertFalse(intFrame.containsRows(List.of(row1, row2, row3)));
    }

    @Test
    public void testRequireColumnPresent() {
        intFrame.requireColumn(col1);
        intFrame.requireColumn(col2);
        intFrame.requireColumn(col3);
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRequireColumnAbsent() {
        intFrame.requireColumn(col4);
    }

    @Test
    public void testRequireDoubleColumnPresent() {
        doubleFrame.requireDoubleColumn(col1);
        doubleFrame.requireDoubleColumns(List.of(col2, col3));
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRequireDoubleColumnAbsent() {
        //
        // Integer columns are "numeric" but not "double"...
        //
        intFrame.requireDoubleColumn(col1);
    }

    @Test
    public void testRequireNumericColumnPresent() {
        intFrame.requireNumericColumn(col1);
        intFrame.requireNumericColumns(List.of(col2, col3));

        doubleFrame.requireNumericColumn(col1);
        doubleFrame.requireNumericColumns(List.of(col2, col3));
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRequireNumericColumnAbsent() {
        DataFrame<RowKey, ColKey> stringDataFrame = DataFrame.ofStrings(rowKeys, colKeys);
        stringDataFrame.requireNumericColumn(col1);
    }

    @Test
    public void testRequireRowPresent() {
        intFrame.requireRow(row1);
        intFrame.requireRow(row2);
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRequiredRowAbsent() {
        intFrame.requireRow(row4);
    }

    @Test
    public void testListRowKeys() {
        assertEquals(intFrame.listRowKeys(), rowKeys);
    }

    @Test
    public void testListColumnKeys() {
        assertEquals(intFrame.listColumnKeys(), colKeys);
    }

    @Test
    public void testGetMatrix1() {
        double[][] actual = doubleFrame.getDoubleMatrix();
        double[][] expected = new double[][] {
                { 11.0, 12.0, 13.0 },
                { 21.0, 22.0, 23.0 }
        };

        assertTrue(comparator.equals(actual, expected));
    }

    @Test
    public void testGetMatrix2() {
        double[][] actual =
                doubleFrame.getDoubleMatrix(List.of(row2, row1), List.of(col3, col1, col2));

        double[][] expected = new double[][] {
                { 23.0, 21.0, 22.0 },
                { 13.0, 11.0, 12.0 }
        };

        assertTrue(comparator.equals(actual, expected));
    }

    @Test
    public void testOnesColumn() {
        var colKey = col1;
        var colKeys = List.of(col1, col2, col3);

        var rowKey = row1;
        var rowKeys = List.of(row1, row2, row3);

        var colData0 = new double[][] {
                { 0.0 },
                { 0.0 },
                { 0.0 }
        };

        var colData1 = new double[][] {
                { 1.0 },
                { 1.0 },
                { 1.0 }
        };

        var rowData0 = new double[][] {
                { 0.0, 0.0, 0.0 }
        };

        var rowData1 = new double[][] {
                { 1.0, 1.0, 1.0 }
        };

        var colFrame0 = DataFrame.zerosColumn(rowKeys, colKey);
        var colFrame1 = DataFrame.onesColumn(rowKeys, colKey);

        var rowFrame0 = DataFrame.zerosRow(rowKey, colKeys);
        var rowFrame1 = DataFrame.onesRow(rowKey, colKeys);

        assertDoubleFrame(colFrame0, rowKeys, List.of(colKey), colData0);
        assertDoubleFrame(colFrame1, rowKeys, List.of(colKey), colData1);
        assertDoubleFrame(rowFrame0, List.of(rowKey), colKeys, rowData0);
        assertDoubleFrame(rowFrame1, List.of(rowKey), colKeys, rowData1);
    }

    @Test
    public void testZeros() {
        DataFrame<RowKey, ColKey> frame = DataFrame.zeros(rowKeys, colKeys);

        double[][] expected = new double[][] {
                { 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0 }
        };

        assertDoubleFrame(frame, rowKeys, colKeys, expected);
    }

    @Test
    public void testPatchyUpdate() {
        DataFrame<RowKey, ColKey> frame = DataFrame.zeros(rowKeys, colKeys);

        DataFrame<RowKey, ColKey> frameRow1 = DataFrame.ofDoubles(row1, List.of(col1, col2));
        frameRow1.setDouble(row1, col1, 11.0);
        frameRow1.setDouble(row1, col2, 12.0);

        DataFrame<RowKey, ColKey> frameRow2 = DataFrame.ofDoubles(row2, List.of(col2, col3));
        frameRow2.setDouble(row2, col2, 22.0);
        frameRow2.setDouble(row2, col3, 23.0);

        frame = frame.update(List.of(frameRow1, frameRow2), false, false);

        double[][] expected = new double[][] {
                { 11.0, 12.0,  0.0 },
                {  0.0, 22.0, 23.0 }
        };

        assertDoubleFrame(frame, rowKeys, colKeys, expected);
    }

    @Test
    public void testColumnFrame() {
        ColKey colKey = col1;
        List<RowKey> rowKeys = List.of(row1, row2, row3);
        double[] rowArray = new double[] { 1.0, 2.0, 3.0 };
        D3xVector rowValues = D3xVector.wrap(rowArray);

        DataFrame<RowKey, ColKey> frame1 = DataFrame.ofDoubles(rowKeys, colKey, rowArray);
        assertDoubleFrame(frame1, rowKeys, List.of(colKey), new double[][] {{ 1.0 }, { 2.0 }, { 3.0 }});

        DataFrame<RowKey, ColKey> frame2 = DataFrame.ofDoubles(rowKeys, colKey, rowValues);
        assertDoubleFrame(frame2, rowKeys, List.of(colKey), new double[][] {{ 1.0 }, { 2.0 }, { 3.0 }});
    }

    @Test
    public void testRowFrame() {
        RowKey rowKey = row1;
        List<ColKey> colKeys = List.of(col1, col2, col3);
        double[] colArray = new double[] { 1.0, 2.0, 3.0 };
        D3xVector colValues = D3xVector.wrap(colArray);

        DataFrame<RowKey, ColKey> frame1 = DataFrame.ofDoubles(rowKey, colKeys, colArray);
        assertDoubleFrame(frame1, List.of(rowKey), colKeys, new double[][] {{ 1.0, 2.0, 3.0 }});

        DataFrame<RowKey, ColKey> frame2 = DataFrame.ofDoubles(rowKey, colKeys, colValues);
        assertDoubleFrame(frame2, List.of(rowKey), colKeys, new double[][] {{ 1.0, 2.0, 3.0 }});
    }

    @Test
    public void testMatrixFrame() {
        double[][] values = new double[][] {
                { 11.0, 12.0, 13.0 },
                { 21.0, 22.0, 23.0 }
        };

        DataFrame<RowKey, ColKey> frame = DataFrame.ofDoubles(rowKeys, colKeys, D3xMatrix.wrap(values));
        assertDoubleFrame(frame, rowKeys, colKeys, values);
    }
    
    @Test
    public void testMatrixView() {
        assertEquals(doubleFrame.nrow(), 2);
        assertEquals(doubleFrame.ncol(), 3);
        assertEquals(doubleFrame.size(), 6);

        assertEquals(doubleFrame.get(0, 0), 11.0, TOLERANCE);
        assertEquals(doubleFrame.get(0, 1), 12.0, TOLERANCE);
        assertEquals(doubleFrame.get(0, 2), 13.0, TOLERANCE);

        assertEquals(doubleFrame.get(1, 0), 21.0, TOLERANCE);
        assertEquals(doubleFrame.get(1, 1), 22.0, TOLERANCE);
        assertEquals(doubleFrame.get(1, 2), 23.0, TOLERANCE);
    }

    @Test
    public void testSelect() {
        DataFrame<RowKey, ColKey> frame = DataFrame.ofDoubles(
                List.of(row1, row2, row3, row4),
                List.of(col1, col2, col3, col4),
                D3xMatrix.byrow(4, 4,
                        11.0, 12.0, 13.0, 14.0,
                        21.0, 22.0, 23.0, 24.0,
                        31.0, 32.0, 33.0, 34.0,
                        41.0, 42.0, 43.0, 44.0));

        DataFrame<RowKey, ColKey> sub1 = frame.selectRows(List.of(row4, row2));
        DataFrame<RowKey, ColKey> sub2 = frame.selectColumns(List.of(col3, col1));
        DataFrame<RowKey, ColKey> sub3 = frame.select(List.of(row2, row3), List.of(col3, col1));

        assertEquals(sub1.listRowKeys(), List.of(row4, row2));
        assertEquals(sub1.listColumnKeys(), List.of(col1, col2, col3, col4));
        assertTrue(D3xMatrix.wrap(sub1.getDoubleMatrix()).equalsMatrix(D3xMatrix.byrow(2, 4,
                41.0, 42.0, 43.0, 44.0,
                21.0, 22.0, 23.0, 24.0)));

        assertEquals(sub2.listRowKeys(), List.of(row1, row2, row3, row4));
        assertEquals(sub2.listColumnKeys(), List.of(col3, col1));
        assertTrue(D3xMatrix.wrap(sub2.getDoubleMatrix()).equalsMatrix(D3xMatrix.byrow(4, 2,
                13.0, 11.0,
                23.0, 21.0,
                33.0, 31.0,
                43.0, 41.0)));

        assertEquals(sub3.listRowKeys(), List.of(row2, row3));
        assertEquals(sub3.listColumnKeys(), List.of(col3, col1));
        assertTrue(D3xMatrix.wrap(sub3.getDoubleMatrix()).equalsMatrix(D3xMatrix.byrow(2, 2,
                23.0, 21.0,
                33.0, 31.0)));
    }

    @Test
    public void testRemapFrame() {
        DataFrame<Integer, LocalDate> frame1 = createFrame(12000, 200);

        long start = System.currentTimeMillis();
        DataFrame<String, String> frame2 = frame1.remapKeys(Object::toString, LocalDate::toString);
        DataFrame<Integer, LocalDate> frame3 = frame2.remapKeys(Integer::valueOf, LocalDate::parse);
        System.out.println(System.currentTimeMillis() - start);

        assertEquals(frame3.listRowKeys(), frame1.listRowKeys());
        assertEquals(frame3.listColumnKeys(), frame1.listColumnKeys());

        for (int row = 0; row < frame1.nrow(); ++row)
            for (int col = 0; col < frame1.ncol(); ++col)
                assertEquals(frame3.getIntAt(row, col), frame1.getIntAt(row, col));
    }

    private static DataFrame<Integer, LocalDate> createFrame(int nrow, int ncol) {
        Random random = new Random(20210512);
        List<Integer> rowKeys = new ArrayList<>();
        List<LocalDate> colKeys = new ArrayList<>();

        for (int row = 0; row < nrow; ++row)
            rowKeys.add(row);

        for (int col = 0; col < ncol; ++col)
            colKeys.add(LocalDate.ofYearDay(2021, 1 + col));

        DataFrame<Integer, LocalDate> frame = DataFrame.ofInts(rowKeys, colKeys);

        for (int row = 0; row < nrow; ++row)
            for (int col = 0; col < ncol; ++col)
                frame.setIntAt(row, col, random.nextInt(1000));

        return frame;
    }
}
