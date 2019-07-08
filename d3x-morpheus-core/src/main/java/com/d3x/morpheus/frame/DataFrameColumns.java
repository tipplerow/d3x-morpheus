/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.stats.StatType;

/**
 * An interface that provides functions to operate on the column dimension of a DataFrame
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public interface DataFrameColumns<R,C> extends DataFrameAxis<C,R,R,C,DataFrameColumn<R,C>,DataFrameColumns<R,C>,DataFrameGrouping.Cols<R,C>> {

    /**
     * Adds a column if one does not already exist for the key specified
     * @param key       the column key
     * @param type      the data type for the column
     * @return          a reference to the column for the key specified
     */
    boolean add(C key, Class<?> type);

    /**
     * Adds a column if one does not already exist with the values provided
     * @param key       the column key
     * @param values    the values for column
     * @return          a reference to the column for the key specified
     */
    boolean add(C key, Iterable<?> values);

    /**
     * Adds a column if one does not already exist for the key specified
     * @param key       the column key
     * @param type      the data type for the column
     * @param seed      the function that seeds values for the column
     * @return          a reference to the column for the key specified
     */
    <T> boolean add(C key, Class<T> type, Function<DataFrameValue<R,C>,T> seed);

    /**
     * Adds columns to this frame based on the column key and value mapping
     * @param consumer  the consumer that populates the map with key array mappings
     * @return          the column keys of newly added columns
     */
    Array<C> addAll(Consumer<Map<C,Iterable<?>>> consumer);

    /**
     * Adds multiple columns if they do not already exist
     * @param colKeys   the column keys to add
     * @param type      the data type for columns
     * @return          the column keys of newly added columns
     */
    Array<C> addAll(Iterable<C> colKeys, Class<?> type);

    /**
     * Returns a newly created column cursor
     * @return      the newly created column cursor
     */
    DataFrameColumn.Cursor<R,C> cursor();

    /**
     * Returns a reference to the stats API for the column dimension
     * @return      the stats API to operate in the column dimension
     */
    DataFrameAxisStats<C,R,C,C,StatType> stats();

    /**
     * Returns a DataFrame containing one or more stats for the rows
     * @param stats     the sequence of stats to compute, none implies all stats
     * @return          the DataFrame of row statistics
     */
    DataFrame<C,StatType> describe(StatType... stats);

    /**
     * Returns a new shallow copy of the frame with the mapped column keys
     * @param mapper    the mapper function to map column keys
     * @param <X>       the new column key type
     * @return          a shallow copy of the frame with new keys
     */
    <X> DataFrame<R,X> mapKeys(Function<DataFrameColumn<R,C>, X> mapper);

    /**
     * Returns a DataFrame with evenly distributed frequency counts for the columns specified
     * @param binCount  the number of bins to include in frequency distribution
     * @param columns   the column keys to generate frequency distribution
     * @return          the DataFrame histogram with frequency distributions
     */
    DataFrame<Double,C> hist(int binCount, C... columns);

    /**
     * Returns a DataFrame with evenly distributed frequency counts for the columns specified
     * @param binCount  the number of bins to include in frequency distribution
     * @param columns   the column keys to generate frequenct distribution
     * @return          the DataFrame histogram with frequency distributions
     */
    DataFrame<Double,C> hist(int binCount, Iterable<C> columns);

    /**
     * Returns the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @return              the value for coordinates
     */
    boolean getBoolean(C colKey, int rowOrdinal);

    /**
     * Returns the DataFrame value for the column ordinal and row key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @return              the value for coordinates
     */
    boolean getBooleanAt(int colOrdinal, R rowKey);

    /**
     * Returns the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @return              the value for coordinates
     */
    int getInt(C colKey, int rowOrdinal);

    /**
     * Returns the DataFrame value for the column ordinal and row key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @return              the value for coordinates
     */
    int getIntAt(int colOrdinal, R rowKey);

    /**
     * Returns the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @return              the value for coordinates
     */
    long getLong(C colKey, int rowOrdinal);

    /**
     * Returns the DataFrame value for the column ordinal and row key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @return              the value for coordinates
     */
    long getLongAt(int colOrdinal, R rowKey);

    /**
     * Returns the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @return              the value for coordinates
     */
    double getDouble(C colKey, int rowOrdinal);

    /**
     * Returns the DataFrame value for the column ordinal and row key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @return              the value for coordinates
     */
    double getDoubleAt(int colOrdinal, R rowKey);

    /**
     * Returns the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @return              the value for coordinates
     */
    <V> V getValue(C colKey, int rowOrdinal);

    /**
     * Returns the DataFrame value for the column ordinal and row key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @return              the value for coordinates
     */
    <V> V getValueAt(int colOrdinal, R rowKey);

    /**
     * Sets the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @param value         the value to set
     */
    boolean setBoolean(C colKey, int rowOrdinal, boolean value);

    /**
     * Sets the DataFrame value for the row ordinal and column key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @param value         the value to set
     */
    boolean setBooleanAt(int colOrdinal, R rowKey, boolean value);

    /**
     * Sets the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @param value         the value to set
     */
    int setInt(C colKey, int rowOrdinal, int value);

    /**
     * Sets the DataFrame value for the row ordinal and column key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @param value         the value to set
     */
    int setIntAt(int colOrdinal, R rowKey, int value);

    /**
     * Sets the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @param value         the value to set
     */
    long setLong(C colKey, int rowOrdinal, long value);

    /**
     * Sets the DataFrame value for the row ordinal and column key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @param value         the value to set
     */
    long setLongAt(int colOrdinal, R rowKey, long value);

    /**
     * Sets the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @param value         the value to set
     */
    double setDouble(C colKey, int rowOrdinal, double value);

    /**
     * Sets the DataFrame value for the row ordinal and column key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @param value         the value to set
     */
    double setDoubleAt(int colOrdinal, R rowKey, double value);

    /**
     * Sets the DataFrame value for the column key and row ordinal provided
     * @param colKey        the column key, which must exist
     * @param rowOrdinal    the row ordinal, which must be in bounds
     * @param value         the value to set
     */
    <V> V setValue(C colKey, int rowOrdinal, V value);

    /**
     * Sets the DataFrame value for the row ordinal and column key provided
     * @param colOrdinal    the column ordinal, which must be within bounds
     * @param rowKey        the row key, which must exist
     * @param value         the value to set
     */
    <V> V setValueAt(int colOrdinal, R rowKey, V value);


}
