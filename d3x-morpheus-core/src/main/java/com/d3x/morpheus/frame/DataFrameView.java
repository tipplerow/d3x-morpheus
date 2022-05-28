/*
 * Copyright (C) 2014-2022 D3X Systems - All Rights Reserved
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

import com.d3x.morpheus.vector.DataVectorView;

import java.util.List;

/**
 * Provides a read-only view of a Morpheus DataFrame.
 * 
 * @author Scott Shaffer
 */
public interface DataFrameView<R,C> {
    /**
     * Returns the number of rows in this frame.
     * @return the number of rows in this frame.
     */
    int rowCount();

    /**
     * Returns the number of columns in this frame.
     * @return the number of columns in this frame.
     */
    int colCount();

    /**
     * Identifies empty frames, according to the number of rows.
     * @return true if this frame is empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Identifies row keys in the frame.
     *
     * @param rowKey the row key in question.
     *
     * @return {@code true} iff this view contains the specified row key.
     */
    boolean containsRow(R rowKey);

    /**
     * Determines whether this frame contains particular rows.
     *
     * @param rowKeys the keys of the rows in question.
     *
     * @return {@code true} iff this frame contains a row for every key.
     */
    default boolean containsRows(Iterable<R> rowKeys) {
        for (R rowKey : rowKeys) {
            if (!containsRow(rowKey))
                return false;
        }

        return true;
    }

    /**
     * Identifies column keys in the frame.
     *
     * @param colKey the column key in question.
     *
     * @return {@code true} iff this view contains the specified column key.
     */
    boolean containsColumn(C colKey);

    /**
     * Determines whether this frame contains particular columns.
     *
     * @param colKeys the keys of the columns in question.
     *
     * @return {@code true} iff this frame contains a column for every key.
     */
    default boolean containsColumns(Iterable<C> colKeys) {
        for (C colKey : colKeys) {
            if (!containsColumn(colKey))
                return false;
        }

        return true;
    }

    /**
     * Returns the value for the row and column key coordinates provided.
     * 
     * @param rowKey the row key coordinate
     * @param colKey the column key coordinate
     *               
     * @return the value for coordinates, false if no match for keys.
     */
    boolean getBoolean(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided.
     * 
     * @param rowOrdinal the row ordinal coordinate
     * @param colOrdinal the column ordinal coordinate
     *                   
     * @return the value for coordinates
     */
    boolean getBooleanAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns the value for the row and column key coordinates provided.
     *
     * @param rowKey the row key coordinate
     * @param colKey the column key coordinate
     *
     * @return the value for coordinates, 0 if no match for keys.
     */
    int getInt(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided.
     *
     * @param rowOrdinal the row ordinal coordinate
     * @param colOrdinal the column ordinal coordinate
     *
     * @return the value for coordinates.
     */
    int getIntAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns the value for the row and column key coordinates provided.
     *
     * @param rowKey the row key coordinate
     * @param colKey the column key coordinate
     *
     * @return  the value for coordinates, 0L if no match for keys.
     */
    long getLong(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided.
     *
     * @param rowOrdinal the row ordinal coordinate
     * @param colOrdinal the column ordinal coordinate
     *
     * @return the value for coordinates.
     */
    long getLongAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns the value for the row and column key coordinates provided.
     *
     * @param rowKey the row key coordinate
     * @param colKey the column key coordinate
     *
     * @return the value for coordinates, NaN if no match for keys.
     */
    double getDouble(R rowKey, C colKey);

    /**
     * Returns the value for the row and column key coordinates provided.
     *
     * @param rowKey       the row key coordinate
     * @param colKey       the column key coordinate
     * @param defaultValue the default value, if value is missing
     *
     * @return the value for the specified keys, or the default value if
     * there is no match or the value is NaN.
     */
    default double getDouble(R rowKey, C colKey, double defaultValue) {
        var result = getDouble(rowKey, colKey);

        if (Double.isNaN(result))
            return defaultValue;
        else
            return result;
    }

    /**
     * Returns the value for the row and column ordinal coordinates provided.
     *
     * @param rowOrdinal the row ordinal coordinate
     * @param colOrdinal the column ordinal coordinate
     *
     * @return the value for coordinates.
     */
    double getDoubleAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns a DataVectorView of a numeric column.
     *
     * @param colKey the key of the desired column.
     *
     * @return a DataVectorView of the specified column.
     *
     * @throws RuntimeException unless the column exists.
     */
    DataVectorView<R> getDoubleColumn(C colKey);

    /**
     * Returns a DataVectorView of a numeric column.
     *
     * @param colOrdinal the index of the desired column.
     *
     * @return a DataVectorView of the specified column.
     *
     * @throws RuntimeException unless the column index is in bounds.
     */
    DataVectorView<R> getDoubleColumnAt(int colOrdinal);

    /**
     * Returns the value for the row and column key coordinates provided.
     *
     * @param rowKey the row key coordinate
     * @param colKey the column key coordinate
     *
     * @return the value for coordinates, null if no match for keys.
     */
    <T> T getValue(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided.
     *
     * @param rowOrdinal the row ordinal coordinate
     * @param colOrdinal the column ordinal coordinate
     *
     * @return the value for coordinates.
     */
    <T> T getValueAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns a list of the row keys (in order).
     * @return a list of the row keys (in order).
     */
    List<R> listRowKeys();

    /**
     * Returns a list of the column keys (in order).
     * @return a list of the column keys (in order).
     */
    List<C> listColumnKeys();

    /**
     * Ensures that this frame contains a particular row.
     *
     * @param rowKey the required row key.
     *
     * @throws DataFrameException unless this frame contains a row with the specified key.
     */
    default void requireRow(R rowKey) {
        if (!containsRow(rowKey))
            throw new DataFrameException("Missing row [%s].", rowKey);
    }

    /**
     * Ensures that this frame contains particular rows.
     *
     * @param rowKeys the required row keys.
     *
     * @throws DataFrameException unless this frame contains a row for each specified key.
     */
    default void requireRows(Iterable<R> rowKeys) {
        for (R rowKey : rowKeys)
            requireRow(rowKey);
    }

    /**
     * Ensures that this frame contains a certain number of rows.
     *
     * @param expected the required number of rows.
     *
     * @throws DataFrameException unless this frame contains the specified number of rows.
     */
    default void requireRowCount(int expected) {
        int actual = rowCount();

        if (actual != expected)
            throw new DataFrameException("Expected [%d] rows but found [%d].", expected, actual);
    }

    /**
     * Ensures that this frame contains a particular column.
     *
     * @param colKey the required column key.
     *
     * @throws DataFrameException unless this frame contains a column with the specified key.
     */
    default void requireColumn(C colKey) {
        if (!containsColumn(colKey))
            throw new DataFrameException("Missing column [%s].", colKey);
    }

    /**
     * Ensures that this frame contains particular columns.
     *
     * @param colKeys the required column keys.
     *
     * @throws DataFrameException unless this frame contains a column for each specified key.
     */
    default void requireColumns(Iterable<C> colKeys) {
        for (C colKey : colKeys)
            requireColumn(colKey);
    }

    /**
     * Ensures that this frame contains a certain number of columns.
     *
     * @param expected the required number of columns.
     *
     * @throws DataFrameException unless this frame contains the specified number of columns.
     */
    default void requireColumnCount(int expected) {
        int actual = colCount();

        if (actual != expected)
            throw new DataFrameException("Expected [%d] columns but found [%d].", expected, actual);
    }
}
