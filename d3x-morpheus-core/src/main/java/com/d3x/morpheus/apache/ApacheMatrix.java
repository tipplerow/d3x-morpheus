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
import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.DoubleComparator;

/**
 * Provides an adapter to present Morpheus DataFrames as RealMatrix objects
 * in the Apache Commons Math library.
 *
 * <p>Note that the ApacheMatrix maintains a reference to the DataFrame from
 * which it was created, so changes to the ApacheMatrix (via {@code setEntry})
 * are reflected in the DataFrame, and changes in the DataFrame are reflected
 * in the ApacheMatrix.</p>
 *
 * @param <R> the runtime type for the DataFrame row keys.
 * @param <C> the runtime type for the DataFrame column keys.
 *
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public final class ApacheMatrix<R, C> extends AbstractRealMatrix {
    /** The keys of the DataFrame rows mapped to this matrix. */
    @lombok.NonNull @lombok.Getter
    private final List<R> rowKeys;

    /** The keys of the DataFrame columns mapped to this matrix. */
    @lombok.NonNull @lombok.Getter
    private final List<C> colKeys;

    /** The DataFrame mapped to this matrix. */
    @lombok.NonNull @lombok.Getter
    private final DataFrame<R, C> frame;

    private ApacheMatrix(DataFrame<R, C> frame, List<R> rowKeys, List<C> colKeys) {
        this.frame = frame;
        this.rowKeys = List.copyOf(rowKeys);
        this.colKeys = List.copyOf(colKeys);
        validateKeys();
    }

    private void validateKeys() {
        frame.requireRows(rowKeys);
        frame.requireDoubleColumns(colKeys);
    }

    /**
     * Presents a RealMatrix view for an entire DataFrame.
     *
     * @param <R> the runtime type for the DataFrame row keys.
     * @param <C> the runtime type for the DataFrame column keys.
     * @param frame the DataFrame to view.
     *
     * @return a RealMatrix view of the specified frame.
     *
     * @throws DataFrameException unless all data frame columns contain
     * double precision values.
     */
    public static <R, C> ApacheMatrix<R, C> wrap(DataFrame<R, C> frame) {
        return wrap(frame, frame.listRowKeys(), frame.listColumnKeys());
    }

    /**
     * Presents a RealMatrix view for a slice of a DataFrame.
     *
     * @param <R> the runtime type for the DataFrame row keys.
     * @param <C> the runtime type for the DataFrame column keys.
     * @param frame the DataFrame to view.
     * @param rowKeys the keys of the rows to view.
     * @param colKeys the keys of the columns to view.
     *
     * @return a RealMatrix view of the specified rows and columns.
     *
     * @throws DataFrameException unless the data frame contains the specified
     * rows and columns and each column contains double precision values.
     */
    public static <R, C> ApacheMatrix<R, C> wrap(DataFrame<R, C> frame, List<R> rowKeys, List<C> colKeys) {
        return new ApacheMatrix<>(frame, rowKeys, colKeys);
    }

    /**
     * Determines whether the entries in this matrix are equal to those in a bare array
     * of arrays <em>within the fixed tolerance of the default DoubleComparator</em>.
     *
     * @param values the values to test for equality.
     *
     * @return {@code true} iff the input arrays have the same dimensions as this matrix and
     * each value matches the corresponding entry in this matrix within the tolerance of the
     * default DoubleComparator.
     */
    public boolean equalsData(double[][] values) {
        return equalsData(values, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this matrix are equal to those in a bare array
     * of arrays within the tolerance of a DoubleComparator.
     *
     * @param values     the values to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input arrays have the same dimensions as this matrix and
     * each value matches the corresponding entry in this matrix within the tolerance of the
     * specified comparator.
     */
    public boolean equalsData(double[][] values, DoubleComparator comparator) {
        return equalsMatrix(new Array2DRowRealMatrix(values), comparator);
    }

    /**
     * Determines whether the entries in this matrix are equal to those in another
     * matrix <em>within the fixed tolerance of the default DoubleComparator</em>.
     *
     * @param that the matrix to test for equality.
     *
     * @return {@code true} iff the input matrix has the same dimensions as this matrix and
     * each value matches the corresponding entry in this matrix within the tolerance of the
     * default DoubleComparator.
     */
    public boolean equalsMatrix(RealMatrix that) {
        return equalsMatrix(that, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this matrix are equal to those in another
     * matrix within the tolerance of a DoubleComparator</em>.
     *
     * @param that       the matrix to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input matrix has the same dimensions as this matrix and
     * each value matches the corresponding entry in this matrix within the tolerance of the
     * specified comparator.
     */
    public boolean equalsMatrix(RealMatrix that, DoubleComparator comparator) {
        if (this.getRowDimension() != that.getRowDimension())
            return false;

        if (this.getColumnDimension() != that.getColumnDimension())
            return false;

        for (int row = 0; row < getRowDimension(); row++) {
            for (int col = 0; col < getColumnDimension(); col++) {
                double thisEntry = this.getEntry(row, col);
                double thatEntry = that.getEntry(row, col);

                if (!comparator.equals(thisEntry, thatEntry))
                    return false;
            }
        }

        return true;
    }

    @Override
    public RealMatrix copy() {
        //
        // Could possibly test for sparsity in the underlying DataFrame
        // column and return a SparseRealMatrix in that case...
        //
        return new BlockRealMatrix(getData());
    }

    @Override
    public RealMatrix createMatrix(int rowDimension, int colDimension) {
        //
        // Could possibly test for sparsity in the underlying DataFrame
        // column and return a SparseRealMatrix in that case...
        //
        return new BlockRealMatrix(rowDimension, colDimension);
    }

    @Override
    public int getColumnDimension() {
        return colKeys.size();
    }

    @Override
    public double getEntry(int rowIndex, int colIndex) throws OutOfRangeException {
        return frame.getDouble(rowKeys.get(rowIndex), colKeys.get(colIndex));
    }

    @Override
    public int getRowDimension() {
        return rowKeys.size();
    }

    @Override
    public void setEntry(int rowIndex, int colIndex, double value) throws OutOfRangeException {
        frame.setDouble(rowKeys.get(rowIndex), colKeys.get(colIndex), value);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RealMatrix) && equalsMatrix((RealMatrix) obj);
    }

    @Override
    public int hashCode() {
        //
        // As containers of floating-point values which are subject to round-off error,
        // RealMatrix objects should not be used as hash keys...
        //
        return System.identityHashCode(this);
    }
}
