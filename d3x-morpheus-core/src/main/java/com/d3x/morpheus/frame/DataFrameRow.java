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

import java.util.stream.Stream;

/**
 * A convenience marker interface used to represent a row vector on a DataFrame
 *
 * The <code>DataFrameVector</code> interface is parameterized in 5 types, which makes a it
 * rather cumbersome to pass around directly. The <code>DataFrameRow</code> and <code>DataFrameColumn</code>
 * interfaces exist to address this, and also provide a strongly typed interface to distinguish
 * row vectors from column vectors.
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public interface DataFrameRow<R,C> extends DataFrameVector<R,C,R,C,DataFrameRow<R,C>> {

    @Override
    default boolean containsElement(C colKey) {
        return frame().containsColumn(colKey);
    }

    @Override
    default Stream<C> streamKeys() {
        return frame().cols().keys();
    }

    /**
     * An interface to a movable DataFrameRow
     * @param <R>   the row key type
     * @param <C>   the column key type
     */
    interface Cursor<R,C> extends DataFrameRow<R,C> {

        /**
         * Adds a row for key if it does not exist, and moves cursor to row key
         * @param rowKey    the row key to add
         * @return          this row cursor positioned at row key
         */
        Cursor<R,C> add(R rowKey);

        /**
         * Moves the row cursor to the row key specified if it exists
         * @param rowKey    the row key
         * @return          true if key exists, false otherwise
         */
        boolean tryKey(R rowKey);

        /**
         * Moves the row cursor to the row ordinal if it is not out of bounds
         * @param rowOrdinal    the row ordinal
         * @return              true if ordinal is in bounds, false otherwise
         */
        boolean tryOrdinal(int rowOrdinal);

        /**
         * Moves the row cursor to the row key specified
         * @param rowKey    the row key
         * @return          this row cursor
         * @throws DataFrameException   if row key does not exist
         */
        Cursor<R,C> atKey(R rowKey);

        /**
         * Moves the row cursor to the row ordinal
         * @param rowOrdinal    the row ordinal
         * @return              this row cursor
         * @throws DataFrameException   if row ordinal is out of bounds
         */
        Cursor<R,C> atOrdinal(int rowOrdinal);

    }


}
