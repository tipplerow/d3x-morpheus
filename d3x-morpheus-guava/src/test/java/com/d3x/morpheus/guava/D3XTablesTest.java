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
package com.d3x.morpheus.guava;

import java.util.List;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class D3XTablesTest {
    static final RowKey row1 = RowKey.of("row1");
    static final RowKey row2 = RowKey.of("row2");
    static final RowKey row3 = RowKey.of("row3");

    static final ColKey col1 = ColKey.of("col1");
    static final ColKey col3 = ColKey.of("col3");
    static final ColKey col4 = ColKey.of("col4");

    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testFromStringTable() {
        Table<RowKey, ColKey, String> table = HashBasedTable.create();

        table.put(row1, col1, "11");
        table.put(row2, col3, "23");

        var frame1 = D3XTables.toDataFrame(String.class, table);

        assertEquals(frame1.listRowKeys(), List.of(row1, row2));
        assertEquals(frame1.listColumnKeys(), List.of(col1, col3));
        assertEquals(frame1.getValue(row1, col1), "11");
        assertEquals(frame1.getValue(row2, col3), "23");
        assertNull(frame1.getValue(row1, col3));
        assertNull(frame1.getValue(row2, col1));

        table.put(row3, col4, "34");

        var frame2 = D3XTables.toDataFrame(String.class, table);

        table.remove(row3, col4);

        assertEquals(frame2.getValue(row1, col1), "11");
        assertEquals(frame2.getValue(row2, col3), "23");
        assertEquals(frame2.getValue(row3, col4), "34");
        assertNull(frame1.getValue(row3, col4));
        assertNull(frame2.getValue(row1, col3));
        assertNull(frame2.getValue(row1, col4));
        assertNull(frame2.getValue(row2, col1));
        assertNull(frame2.getValue(row2, col4));
        assertNull(frame2.getValue(row3, col1));
        assertNull(frame2.getValue(row3, col3));
    }

    @Test
    public void testFromDoubleTable() {
        Table<RowKey, ColKey, Double> table = HashBasedTable.create();

        table.put(row1, col1, 11.0);
        table.put(row2, col3, 23.0);

        var frame1 = D3XTables.toDataFrame(Double.class, table);

        assertEquals(frame1.listRowKeys(), List.of(row1, row2));
        assertEquals(frame1.listColumnKeys(), List.of(col1, col3));
        assertEquals(frame1.getDouble(row1, col1), 11.0, TOLERANCE);
        assertEquals(frame1.getDouble(row2, col3), 23.0, TOLERANCE);
        assertTrue(Double.isNaN(frame1.getDouble(row1, col3)));
        assertTrue(Double.isNaN(frame1.getValue(row2, col1)));
    }
}
