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
     * Returns a copy of this cursor
     * @return  a copy of this cursor
     */
    DataFrameCursor<R,C> copy();

    /**
     * Moves this cursor to the row key specified, leaving column location unchanged
     * @param key   the row key
     * @return      this cursor
     */
    DataFrameCursor<R,C> toRow(R key);

    /**
     * Moves this cursor to the column key specified, leaving row location unchanged
     * @param colKey    the column key
     * @return          this cursor
     */
    DataFrameCursor<R,C> toCol(C colKey);

    /**
     * Moves this cursor to the row ordinal specified, leaving column location unchanged
     * @param ordinal   the row ordinal
     * @return          this cursor
     */
    DataFrameCursor<R,C> toRowAt(int ordinal);

    /**
     * Moves this cursor to the column ordinal specified, leaving row location unchanged
     * @param colOrdinal    the column ordinal
     * @return              this cursor
     */
    DataFrameCursor<R,C> toColAt(int colOrdinal);

    /**
     * Moves this cursor to the row and column key specified
     * @param rowKey    the row key
     * @param colKey    the column key
     * @return          this cursor
     */
    DataFrameCursor<R,C> atKeys(R rowKey, C colKey);

    /**
     * Moves this cursor to the row and column ordinals specified
     * @param rowOrdinal    the row ordinal
     * @param colOrdinal    the column ordinal
     * @return              this cursor
     */
    DataFrameCursor<R,C> at(int rowOrdinal, int colOrdinal);

}
