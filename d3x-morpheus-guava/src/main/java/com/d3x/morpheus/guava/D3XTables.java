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

import com.google.common.collect.Table;

import com.d3x.morpheus.frame.DataFrame;

/**
 * Provides static utility methods operating on Guava Tables.
 *
 * @author Scott Shaffer
 */
public final class D3XTables {
    /**
     * Creates a new DataFrame with the contents of a Guava Table.
     *
     * @param valueType the value class type.
     * @param dataTable the Guava Table to copy.
     *
     * @return a new DataFrame with the contents of the specified table.
     */
    public static <R,C,V> DataFrame<R,C> toDataFrame(Class<V> valueType, Table<R,C,V> dataTable) {
        var cells = dataTable.cellSet();
        var rowKeys = dataTable.rowKeySet();
        var colKeys = dataTable.columnKeySet();
        var dataFrame = DataFrame.of(rowKeys, colKeys, valueType);

        for (var cell : cells)
            dataFrame.setValue(cell.getRowKey(), cell.getColumnKey(), cell.getValue());

        return dataFrame;
    }
}
