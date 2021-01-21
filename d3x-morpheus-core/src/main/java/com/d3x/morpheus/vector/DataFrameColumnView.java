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
package com.d3x.morpheus.vector;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameException;

import java.util.List;

import lombok.Getter;
import lombok.NonNull;

/**
 * Provides an adapter to present columns in Morpheus DataFrames as RealVectors
 * in the Apache Commons Math library.
 *
 * <p>Note that the view maintains a reference to the DataFrame from which it
 * was created, so changes to the view (via {@code set} and {@code setEntry})
 * will be reflected in the DataFrame, and changes in the DataFrame will be
 * reflected in the view.</p>
 *
 * @param <R> the runtime type for the DataFrame row keys.
 * @param <C> the runtime type for the DataFrame column keys.
 *
 * @author  Scott Shaffer
 */
public final class DataFrameColumnView<R, C> extends ApacheVector {
    /** The key of the DataFrame column mapped to this vector. */
    @Getter @NonNull
    private final C colKey;

    /** The keys of the DataFrame rows mapped to this vector. */
    @Getter @NonNull
    private final List<R> rowKeys;

    /** The DataFrame mapped to this vector. */
    @Getter @NonNull
    private final DataFrame<R, C> frame;

    // The DataFrame column, fetched once and cached...
    private final DataFrameColumn<R, C> column;

    private DataFrameColumnView(DataFrame<R, C> frame, List<R> rowKeys, C colKey) {
        this.frame = frame;
        this.colKey = colKey;
        this.rowKeys = List.copyOf(rowKeys);

        validateKeys();

        this.column = frame.col(colKey);
    }

    private void validateKeys() {
        frame.requireRows(rowKeys);
        frame.requireDoubleColumn(colKey);
    }

    /**
     * Presents a RealVector view for an entire column in a DataFrame.
     *
     * @param <R> the runtime type for the DataFrame row keys.
     * @param <C> the runtime type for the DataFrame column keys.
     * @param frame the DataFrame to view.
     * @param colKey the key of the column to view.
     *
     * @return a RealVector view of the specified column.
     *
     * @throws DataFrameException unless the data frame contains the specified column
     * and the column contains double precision values.
     */
    public static <R, C> DataFrameColumnView<R, C> of(DataFrame<R, C> frame, C colKey) {
        return of(frame, frame.listRowKeys(), colKey);
    }

    /**
     * Presents a RealVector view for a subset of a column in a DataFrame.
     *
     * @param <R> the runtime type for the DataFrame row keys.
     * @param <C> the runtime type for the DataFrame column keys.
     * @param frame the DataFrame to view.
     * @param rowKeys the keys of the rows to view.
     * @param colKey the key of the column to view.
     *
     * @return a RealVector view of the specified column and rows.
     *
     * @throws DataFrameException unless the data frame contains the specified column
     * and rows and the column contains double precision values.
     */
    public static <R, C> DataFrameColumnView<R, C> of(DataFrame<R, C> frame, List<R> rowKeys, C colKey) {
        return new DataFrameColumnView<>(frame, rowKeys, colKey);
    }

    @Override
    public ApacheDenseVector like(int length) {
        //
        // Could possibly test for sparsity in the underlying DataFrame
        // column and return a sparse vector in that case...
        //
        return ApacheDenseVector.ofLength(length);
    }

    @Override
    public int getDimension() {
        return rowKeys.size();
    }

    @Override
    public double getEntry(int index) {
        return column.getDouble(rowKeys.get(index));
    }

    @Override
    public void setEntry(int index, double value) {
        column.setDouble(rowKeys.get(index), value);
    }
}
