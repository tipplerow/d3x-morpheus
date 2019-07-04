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
package com.d3x.morpheus.reference;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameRow;

/**
 * A class that is designed to sort a DataFrame in either the row or column dimension
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @see <a href="http://mechanical-sympathy.blogspot.com/2012/08/memory-access-patterns-are-important.html">Mechanical Sympathy</a>
 *
 * @author  Xavier Witdouck
 */
class XDataFrameSorter {


    /**
     * Sorts the rows of a DataFrame according to data in the specified column
     * @param frame         the frame to sort
     * @param colKey        the column key to sort by
     * @param ascending     true for ascending, false for descending
     * @param parallel      true for parallel sort
     * @return              the sorted DataFrame
     */
    static <R,C> XDataFrame<R,C> sortRows(XDataFrame<R,C> frame, C colKey, boolean ascending, boolean parallel) {
        return sortRows(frame, Collections.singletonList(colKey), ascending, parallel);
    }


    /**
     * Sorts the column of a DataFrame according to data in the specified rows
     * @param frame         the frame to sort
     * @param rowKey        the row key to sort by
     * @param ascending     true for ascending, false for descending
     * @param parallel      true for parallel sort
     * @return              the sorted DataFrame
     */
    static <R,C> XDataFrame<R,C> sortCols(XDataFrame<R,C> frame, R rowKey, boolean ascending, boolean parallel) {
        return sortCols(frame, Collections.singletonList(rowKey), ascending, parallel);
    }


    /**
     * Sorts the rows of a DataFrame according to the row keys
     * @param frame         the frame to sort
     * @param ascending     true for ascending, false for descending
     * @param parallel      true for parallel sort
     * @return              the sorted DataFrame
     */
    static <R,C> XDataFrame<R,C> sortRows(XDataFrame<R,C> frame, boolean ascending, boolean parallel) {
        frame.rowKeys().sort(parallel, ascending);
        return frame;
    }


    /**
     * Sorts the column of a DataFrame according to the column keys
     * @param frame         the frame to sort
     * @param ascending     true for ascending, false for descending
     * @param parallel      true for parallel sort
     * @return              the sorted DataFrame
     */
    static <R,C> XDataFrame<R,C> sortCols(XDataFrame<R,C> frame, boolean ascending, boolean parallel) {
        frame.colKeys().sort(parallel, ascending);
        return frame;
    }


    /**
     * Sorts the rows of a DataFrame according to data in the specified columns
     * @param frame         the frame to sort
     * @param colKeys       the column keys to sort by, in order of precedence
     * @param ascending     true for ascending, false for descending
     * @param parallel      true for parallel sort
     * @return              the sorted DataFrame
     */
    static <R,C> XDataFrame<R,C> sortRows(XDataFrame<R,C> frame, List<C> colKeys, boolean ascending, boolean parallel) {
        var multiplier = ascending ? 1 : -1;
        var result = frame.withRowKeys(frame.rowKeys().copy(false));
        var comparator = result.content().createRowComparator(colKeys, multiplier);
        result.rowKeys().sort(parallel, comparator);
        return result;
    }


    /**
     * Sorts the column of a DataFrame according to data in the specified rows
     * @param frame         the frame to sort
     * @param rowKeys       the row keys to sort by, in order of precedence
     * @param ascending     true for ascending, false for descending
     * @param parallel      true for parallel sort
     * @return              the sorted DataFrame
     */
    static <R,C> XDataFrame<R,C> sortCols(XDataFrame<R,C> frame, List<R> rowKeys, boolean ascending, boolean parallel) {
        var multiplier = ascending ? 1 : -1;
        var result = frame.withColKeys(frame.colKeys().copy(false));
        var comparator = result.content().createColComparator(rowKeys, multiplier);
        result.colKeys().sort(parallel, comparator);
        return result;
    }


    /**
     * Sorts rows of the DataFrame based on the user provided comparator
     * @param frame         the frame reference
     * @param parallel      true for parallel sort
     * @param comparator    the user provided comparator
     * @return              the sorted DataFrame
     */
    static <R,C> XDataFrame<R,C> sortRows(XDataFrame<R,C> frame, boolean parallel, Comparator<DataFrameRow<R,C>> comparator) {
        var result = frame.withRowKeys(frame.rowKeys().copy(false));
        if (comparator == null) {
            result.rowKeys().resetOrder();
            return result;
        } else {
            var rowComparator = XDataFrameComparator.createRowComparator(result, comparator);
            result.rowKeys().sort(parallel, rowComparator);
            return result;
        }
    }


    /**
     * Sorts rows of the DataFrame based on the user provided comparator
     * @param frame         the frame reference
     * @param parallel      true for parallel sort
     * @param comparator    the user provided comparator
     * @return              the sorted DataFrame
     */
    static <R,C> XDataFrame<R,C> sortCols(XDataFrame<R,C> frame, boolean parallel, Comparator<DataFrameColumn<R,C>> comparator) {
        var result = frame.withColKeys(frame.colKeys().copy(false));
        if (comparator == null) {
            result.colKeys().resetOrder();
            return result;
        } else {
            var colComparator = XDataFrameComparator.createColComparator(result, comparator);
            result.colKeys().sort(parallel, colComparator);
            return result;
        }
    }
}
