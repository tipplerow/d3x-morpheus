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

/**
 * An interface to a movable cursor on a DataFrame which enables random access to DataFrame values via various typed methods.
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public interface DataFrameCursor<R,C> extends DataFrameValue<R,C> {

    /**
     * Moves cursor to row key if it exists, otherwise retains current position
     * @param rowKey    the row key to move to
     * @return          true if cursor moved, false if row key does not exist
     */
    boolean tryRow(R rowKey);

    /**
     * Moves cursor to column key if it exists, otherwise retains current position
     * @param colKey    the column key to move to
     * @return          true if cursor moved, false if column key does not exist
     */
    boolean tryColumn(C colKey);

    /**
     * Moves cursor to row and column key if they both exist, otherwise retains existing position
     * @param rowKey    the row key
     * @param colKey    the column key
     * @return          true if cursor moved, false if one or both keys do not exist
     */
    boolean tryKeys(R rowKey, C colKey);

    /**
     * Moves cursor to row and column ordinal if not out of bounds, otherwise retains existing position
     * @param row       the row ordinal
     * @param column    the column ordinal
     * @return          true if cursor moved, false if one or both ordinals out of bounds
     */
    boolean tryOrdinals(int row, int column);

    /**
     * Returns a copy of this cursor
     * @return  a copy of this cursor
     */
    DataFrameCursor<R,C> copy();

    /**
     * Moves this cursor to the row key specified, leaving column location unchanged
     * @param rowKey   the row key
     * @return      this cursor
     * @throws DataFrameException   if key does not exist
     */
    DataFrameCursor<R,C> row(R rowKey);

    /**
     * Moves this cursor to the column key specified, leaving row location unchanged
     * @param colKey    the column key
     * @return          this cursor
     * @throws DataFrameException   if key does not exist
     */
    DataFrameCursor<R,C> col(C colKey);

    /**
     * Moves this cursor to the row ordinal specified, leaving column location unchanged
     * @param row   the row ordinal
     * @return          this cursor
     * @throws DataFrameException   if ordinal out of bounds
     */
    DataFrameCursor<R,C> rowAt(int row);

    /**
     * Moves this cursor to the column ordinal specified, leaving row location unchanged
     * @param column    the column ordinal
     * @return              this cursor
     * @throws DataFrameException   if ordinal out of bounds
     */
    DataFrameCursor<R,C> colAt(int column);

    /**
     * Moves this cursor to the row and column key specified
     * @param rowKey    the row key
     * @param colKey    the column key
     * @return          this cursor
     * @throws DataFrameException   if either key does not exist
     */
    DataFrameCursor<R,C> atKeys(R rowKey, C colKey);

    /**
     * Moves this cursor to the row and column ordinals specified
     * @param row       the row ordinal
     * @param column    the column ordinal
     * @return          this cursor
     * @throws DataFrameException   if either ordinal out of bounds
     */
    DataFrameCursor<R,C> atOrdinals(int row, int column);

    /**
     * Adds a row if it does not exist, and moves this cursor to row
     * @param rowKey    the row key to add, if does not already exist
     * @return          this cursor, positioned at row key
     */
    DataFrameCursor<R,C> addRow(R rowKey);

    /**
     * Adds a column if it does not exist, and moves this cursor to column key
     * @param colKey    the column key to add, if does not already exist
     * @param dataTye   the data type for column
     * @return          this cursor, positioned at column key
     */
    DataFrameCursor<R,C> addColumn(C colKey, Class<?> dataTye);

    /**
     * Adds a row and/or column if they do not exist, and moves this cursor to that location
     * @param rowKey    the row key to add, if does not already exist
     * @param colKey    the column key to add, if does not already exist
     * @param dataTye   the data type for column
     * @return          this cursor, positioned at column key
     */
    DataFrameCursor<R,C> add(R rowKey, C colKey, Class<?> dataTye);


}
