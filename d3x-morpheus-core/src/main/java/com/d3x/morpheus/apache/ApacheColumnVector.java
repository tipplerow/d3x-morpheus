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
package com.d3x.morpheus.apache;

import java.util.List;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameException;

/**
 * Provides an adapter to present columns in Morpheus DataFrames as RealVectors in the
 * Apache Commons Math library.
 *
 * <p>Note that the ApacheColumnVector maintains a reference to the DataFrame from
 * which it was created, so changes to the ApacheColumnVector (via {@code setEntry})
 * are reflected in the DataFrame, and changes in the DataFrame are reflected
 * in the ApacheColumnVector.</p>
 *
 *
 * @param <R> the runtime type for the DataFrame row keys.
 * @param <C> the runtime type for the DataFrame column keys.
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public final class ApacheColumnVector<R, C> extends AbstractRealVector {
    /** The key of the DataFrame column mapped to this vector. */
    @lombok.NonNull @lombok.Getter
    private final C colKey;

    /** The keys of the DataFrame rows mapped to this vector. */
    @lombok.NonNull @lombok.Getter
    private final List<R> rowKeys;

    /** The DataFrame mapped to this vector. */
    @lombok.NonNull @lombok.Getter
    private final DataFrame<R, C> frame;

    // The DataFrame column, fetched once and cached...
    private final DataFrameColumn<R, C> column;

    private ApacheColumnVector(DataFrame<R, C> frame, C colKey, List<R> rowKeys) {
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
    public static <R, C> ApacheColumnVector<R, C> wrap(DataFrame<R, C> frame, C colKey) {
        return wrap(frame, colKey, frame.listRowKeys());
    }

    /**
     * Presents a RealVector view for a subset of a column in a DataFrame.
     *
     * @param <R> the runtime type for the DataFrame row keys.
     * @param <C> the runtime type for the DataFrame column keys.
     * @param frame the DataFrame to view.
     * @param colKey the key of the column to view.
     * @param rowKeys the keys of the rows to view.
     *
     * @return a RealVector view of the specified column and rows.
     *
     * @throws DataFrameException unless the data frame contains the specified column
     * and rows and the column contains double precision values.
     */
    public static <R, C> ApacheColumnVector<R, C> wrap(DataFrame<R, C> frame, C colKey, List<R> rowKeys) {
        return new ApacheColumnVector<>(frame, colKey, rowKeys);
    }

    @Override
    public RealVector like(int length) {
        //
        // Could possibly test for sparsity in the underlying DataFrame
        // column and return a SparseRealVector in that case...
        //
        return new ArrayRealVector(length);
    }

    @Override
    public int getDimension() {
        return rowKeys.size();
    }

    @Override
    public double getEntry(int index) throws OutOfRangeException {
        return column.getDouble(rowKeys.get(index));
    }

    @Override
    public void setEntry(int index, double value) throws OutOfRangeException {
        column.setDouble(rowKeys.get(index), value);
    }
}
