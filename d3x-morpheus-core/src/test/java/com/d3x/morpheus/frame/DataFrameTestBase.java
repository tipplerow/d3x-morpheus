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

import com.d3x.morpheus.util.DoubleComparator;

import static org.testng.Assert.*;

/**
 * Provides common data for tests in the {@code com.d3x.morpheus.frame} package.
 */
public abstract class DataFrameTestBase {
    protected static final RowKey row1 = RowKey.of("row1");
    protected static final RowKey row2 = RowKey.of("row2");
    protected static final RowKey row3 = RowKey.of("row3");
    protected static final RowKey row4 = RowKey.of("row4");

    protected static final ColKey col1 = ColKey.of("col1");
    protected static final ColKey col2 = ColKey.of("col2");
    protected static final ColKey col3 = ColKey.of("col3");
    protected static final ColKey col4 = ColKey.of("col4");

    protected final DataFrame<RowKey, ColKey> intFrame = newIntFrame();
    protected final DataFrame<RowKey, ColKey> doubleFrame = newDoubleFrame();

    protected static final List<RowKey> rowKeys = List.of(row1, row2);
    protected static final List<ColKey> colKeys = List.of(col1, col2, col3);

    protected static final DoubleComparator comparator = DoubleComparator.DEFAULT;

    protected static DataFrame<RowKey, ColKey> newIntFrame() {
        DataFrame<RowKey, ColKey> frame = DataFrame.ofInts(rowKeys, colKeys);

        frame.setInt(row1, col1, 11);
        frame.setInt(row1, col2, 12);
        frame.setInt(row1, col3, 13);
        frame.setInt(row2, col1, 21);
        frame.setInt(row2, col2, 22);
        frame.setInt(row2, col3, 23);

        return frame;
    }

    protected static DataFrame<RowKey, ColKey> newDoubleFrame() {
        DataFrame<RowKey, ColKey> frame = DataFrame.ofDoubles(rowKeys, colKeys);

        frame.setDouble(row1, col1, 11.0);
        frame.setDouble(row1, col2, 12.0);
        frame.setDouble(row1, col3, 13.0);
        frame.setDouble(row2, col1, 21.0);
        frame.setDouble(row2, col2, 22.0);
        frame.setDouble(row2, col3, 23.0);

        return frame;
    }

    protected static <R,C> void assertDoubleFrame(DataFrame<R,C> actual,
                                                  List<R> expectedRows,
                                                  List<C> expectedCols,
                                                  double[][] expectedValues) {
        assertEquals(actual.rowCount(), expectedRows.size());
        assertEquals(actual.colCount(), expectedCols.size());

        assertEquals(actual.rows().keyList(), expectedRows);
        assertEquals(actual.cols().keyList(), expectedCols);

        assertTrue(comparator.equals(actual.getDoubleMatrix(), expectedValues));

        for (int i = 0; i < actual.rowCount(); i++) {
            for (int j = 0; j < actual.colCount(); j++) {
                R rowKey = expectedRows.get(i);
                C colKey = expectedCols.get(j);

                assertTrue(comparator.equals(actual.getDoubleAt(i, j), expectedValues[i][j]));
                assertTrue(comparator.equals(actual.getDouble(rowKey, colKey), expectedValues[i][j]));
            }
        }
    }
}
