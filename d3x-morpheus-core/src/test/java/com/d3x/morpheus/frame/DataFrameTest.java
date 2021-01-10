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
public class DataFrameTest {
    private final DataFrame<String, String> intFrame = newIntFrame();
    private final DataFrame<String, String> doubleFrame = newDoubleFrame();

    private static final List<String> rowKeys = List.of("row1", "row2");
    private static final List<String> colKeys = List.of("col1", "col2", "col3");

    private static DataFrame<String, String> newIntFrame() {
        DataFrame<String, String> frame = DataFrame.ofInts(rowKeys, colKeys);

        frame.setInt("row1", "col1", 11);
        frame.setInt("row1", "col2", 12);
        frame.setInt("row1", "col3", 13);
        frame.setInt("row2", "col1", 21);
        frame.setInt("row2", "col2", 22);
        frame.setInt("row2", "col3", 23);

        return frame;
    }

    private static DataFrame<String, String> newDoubleFrame() {
        DataFrame<String, String> frame = DataFrame.ofDoubles(rowKeys, colKeys);

        frame.setDouble("row1", "col1", 11.0);
        frame.setDouble("row1", "col2", 12.0);
        frame.setDouble("row1", "col3", 13.0);
        frame.setDouble("row2", "col1", 21.0);
        frame.setDouble("row2", "col2", 22.0);
        frame.setDouble("row2", "col3", 23.0);

        return frame;
    }

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
}
