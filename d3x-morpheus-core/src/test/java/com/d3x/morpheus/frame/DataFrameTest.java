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

import java.util.List;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * Tests default methods implemented in the DataFrame interface.
 */
public class DataFrameTest extends DataFrameTestBase {
    @Test
    public void testContainsColumn() {
        assertTrue(intFrame.containsColumn("col1"));
        assertTrue(intFrame.containsColumn("col2"));
        assertTrue(intFrame.containsColumn("col3"));

        assertFalse(intFrame.containsColumn("row1"));
        assertFalse(intFrame.containsColumn("row2"));
    }

    @Test
    public void testContainsColumns() {
        assertTrue(intFrame.containsColumns(List.of("col1")));
        assertTrue(intFrame.containsColumns(List.of("col1", "col2")));
        assertTrue(intFrame.containsColumns(List.of("col1", "col2", "col3")));
        assertFalse(intFrame.containsColumns(List.of("col1", "col2", "col3", "col4")));
    }

    @Test
    public void testContainsRow() {
        assertTrue(intFrame.containsRow("row1"));
        assertTrue(intFrame.containsRow("row2"));

        assertFalse(intFrame.containsRow("col1"));
        assertFalse(intFrame.containsRow("col2"));
        assertFalse(intFrame.containsRow("col3"));
    }

    @Test
    public void testContainsRows() {
        assertTrue(intFrame.containsRows(List.of("row1")));
        assertTrue(intFrame.containsRows(List.of("row1", "row2")));
        assertFalse(intFrame.containsRows(List.of("row1", "row2", "row3")));
    }

    @Test
    public void testRequireColumnPresent() {
        intFrame.requireColumn("col1");
        intFrame.requireColumn("col2");
        intFrame.requireColumn("col3");
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRequireColumnAbsent() {
        intFrame.requireColumn("row1");
    }

    @Test
    public void testRequireDoubleColumnPresent() {
        doubleFrame.requireDoubleColumn("col1");
        doubleFrame.requireDoubleColumns(List.of("col2", "col3"));
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRequireDoubleColumnAbsent() {
        //
        // Integer columns are "numeric" but not "double"...
        //
        intFrame.requireDoubleColumn("col1");
    }

    @Test
    public void testRequireNumericColumnPresent() {
        intFrame.requireNumericColumn("col1");
        intFrame.requireNumericColumns(List.of("col2", "col3"));

        doubleFrame.requireNumericColumn("col1");
        doubleFrame.requireNumericColumns(List.of("col2", "col3"));
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRequireNumericColumnAbsent() {
        DataFrame<String, String> stringDataFrame = DataFrame.ofStrings(rowKeys, colKeys);
        stringDataFrame.requireNumericColumn("col1");
    }

    @Test
    public void testRequireRowPresent() {
        intFrame.requireRow("row1");
        intFrame.requireRow("row2");
    }

    @Test(expectedExceptions = DataFrameException.class)
    public void testRequiredRowAbsent() {
        intFrame.requireRow("col1");
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
                doubleFrame.getDoubleMatrix(List.of("row2", "row1"), List.of("col3", "col1", "col2"));

        double[][] expected = new double[][] {
                { 23.0, 21.0, 22.0 },
                { 13.0, 11.0, 12.0 }
        };

        assertTrue(comparator.equals(actual, expected));
    }

    @Test
    public void testOnesColumn() {
        var colKey = "col1";
        var colKeys = List.of("col1", "col2", "col3");

        var rowKey = "row1";
        var rowKeys = List.of("row1", "row2", "row3");

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
        DataFrame<String,String> frame = DataFrame.zeros(rowKeys, colKeys);

        double[][] expected = new double[][] {
                { 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0 }
        };

        assertDoubleFrame(frame, rowKeys, colKeys, expected);
    }

    @Test
    public void testPatchyUpdate() {
        DataFrame<String,String> frame = DataFrame.zeros(rowKeys, colKeys);

        DataFrame<String,String> row1 = DataFrame.ofDoubles("row1", List.of("col1", "col2"));
        row1.setDouble("row1", "col1", 11.0);
        row1.setDouble("row1", "col2", 12.0);

        DataFrame<String,String> row2 = DataFrame.ofDoubles("row2", List.of("col2", "col3"));
        row2.setDouble("row2", "col2", 22.0);
        row2.setDouble("row2", "col3", 23.0);

        frame = frame.update(List.of(row1, row2), false, false);

        double[][] expected = new double[][] {
                { 11.0, 12.0,  0.0 },
                {  0.0, 22.0, 23.0 }
        };

        assertDoubleFrame(frame, rowKeys, colKeys, expected);
    }
}
