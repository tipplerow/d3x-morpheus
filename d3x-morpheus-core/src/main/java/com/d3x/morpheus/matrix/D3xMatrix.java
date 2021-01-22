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
package com.d3x.morpheus.matrix;

import java.util.List;

import com.d3x.core.lang.D3xException;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.util.DoubleComparator;

/**
 * Represents a matrix of {@code double} values with fixed row and column
 * dimensions.
 */
public interface D3xMatrix {
    /**
     * Returns the number of rows in this matrix.
     * @return the number of rows in this matrix.
     */
    int nrow();

    /**
     * Returns the number of columns in this matrix.
     * @return the number of columns in this matrix.
     */
    int ncol();

    /**
     * Returns the value of an element at a given location.
     *
     * @param row the row index of the element to return.
     * @param col the column index of the element to return.
     *
     * @return the value of the element at the specified location.
     *
     * @throws RuntimeException if either index is out of bounds.
     */
    double get(int row, int col);

    /**
     * Assigns the value of an element at a given location.
     *
     * @param row the row index of the element to assign.
     * @param col the column index of the element to assign.
     * @param value the value to assign.
     *
     * @throws RuntimeException if either index is out of bounds.
     */
    void set(int row, int col, double value);

    /**
     * Returns a new matrix with the same concrete type as this matrix.
     *
     * @param nrow the number of rows in the new matrix.
     * @param ncol the number of columns in the new matrix.
     *
     * @return a new matrix with the specified shape having the same
     * concrete type as this matrix.
     *
     * @throws RuntimeException if either dimension is negative.
     */
    D3xMatrix like(int nrow, int ncol);

    /**
     * Returns the transpose of this matrix; this matrix is unchanged.
     *
     * @return the transpose of this matrix.
     */
    D3xMatrix transpose();

    /**
     * Creates a new matrix and populates its values in row-major order.
     *
     * @param nrow the number of rows in the matrix.
     * @param ncol the number of columns in the matrix.
     * @param values the matrix values, given in row-major order.
     *
     * @return a new matrix with the specified shape and data.
     *
     * @throws RuntimeException if either dimension is negative or if
     * the number of data values does not equal the number of elements
     * ({@code nrow * ncol}).
     */
    static D3xMatrix byrow(int nrow, int ncol, double... values) {
        validateShape(nrow, ncol);
        D3xMatrix matrix = dense(nrow, ncol);

        if (values.length != matrix.size())
            throw new D3xException("Number of values does not match the number of elements.");

        for (int i = 0; i < nrow; ++i)
            for (int j = 0; j < ncol; ++j)
                matrix.set(i, j, values[ncol * i + j]);

        return matrix;
    }

    /**
     * Creates a new matrix by copying values from a Morpheus DataFrame.
     *
     * @param frame the source DataFrame.
     *
     * @return a new matrix containing a copy of the data in the specified
     * frame (in the same row and column order).
     *
     * @throws DataFrameException unless the frame contains entirely numeric
     * data.
     */
    static <R,C> D3xMatrix copyFrame(DataFrame<R,C> frame) {
        return copyFrame(frame, frame.listRowKeys(), frame.listColumnKeys());
    }

    /**
     * Creates a new matrix by copying selected rows and columns from a
     * Morpheus DataFrame.
     *
     * @param frame   the source DataFrame.
     * @param rowKeys the keys of the rows to copy.
     * @param colKeys the keys of the columns to copy.
     *
     * @return a new matrix containing a copy of the specified rows and
     * columns in the specified frame.
     *
     * @throws DataFrameException unless the frame contains the specified
     * rows and columns with numeric data.
     */
    static <R,C> D3xMatrix copyFrame(DataFrame<R,C> frame, List<R> rowKeys, List<C> colKeys) {
        frame.requireRows(rowKeys);
        frame.requireDoubleColumns(colKeys);

        D3xMatrix matrix = dense(rowKeys.size(), colKeys.size());

        for (int i = 0; i < rowKeys.size(); ++i)
            for (int j = 0; j < colKeys.size(); ++j)
                matrix.set(i, j, frame.getDouble(rowKeys.get(i), colKeys.get(j)));

        return matrix;
    }

    /**
     * Creates a new mutable matrix by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new mutable matrix containing a copy of the specified array.
     */
    static D3xMatrix copyOf(double[][] values) {
        return ApacheMatrix.copyOf(values);
    }

    /**
     * Creates a new matrix with dense physical storage and all elements
     * initialized to zero.
     *
     * @param nrow the number of matrix rows.
     * @param ncol the number of matrix columns.
     *
     * @return a new matrix with dense physical storage.
     *
     * @throws RuntimeException if either dimension is negative.
     */
    static D3xMatrix dense(int nrow, int ncol) {
        return ApacheMatrix.dense(nrow, ncol);
    }

    /**
     * Creates a new matrix with storage only for diagonal elements (all
     * diagonal elements initialized to zero).
     *
     * @param N the length of the diagonal.
     *
     * @return a new matrix with storage for only diagonal elements.
     *
     * @throws RuntimeException if the diagonal length is negative.
     */
    static D3xMatrix diagonal(int N) {
        return ApacheMatrix.diagonal(N);
    }

    /**
     * Similar to the {@code R} function {@code rep(x, n)}, creates a new mutable
     * matrix containing the value {@code x} replicated {@code n} times.
     *
     * @param x the value to replicate.
     * @param m the number of rows in the replicate matrix.
     * @param n the number of columns in the replicate matrix.
     *
     * @return a new mutable matrix with shape {@code m x n} with each element
     * assigned the value {@code x}.
     *
     * @throws RuntimeException if {@code n < 0}.
     */
    static D3xMatrix rep(double x, int m, int n) {
        D3xMatrix result = dense(m, n);

        for (int i = 0; i < m; ++i)
            for (int j = 0; j < n; ++j)
                result.set(i, j, x);

        return result;
    }

    /**
     * Creates a new matrix with sparse physical storage and all elements
     * initialized to zero.
     *
     * @param nrow the number of matrix rows.
     * @param ncol the number of matrix columns.
     *
     * @return a new matrix with sparse physical storage.
     *
     * @throws RuntimeException if either dimension is negative.
     */
    static D3xMatrix sparse(int nrow, int ncol) {
        return ApacheMatrix.sparse(nrow, ncol);
    }

    /**
     * Creates a mutable matrix view over a bare array.
     *
     * @param values the values to be wrapped.
     *
     * @return a mutable matrix view using the specified array as
     * its physical storage.
     */
    static D3xMatrix wrap(double[][] values) {
        return ApacheMatrix.wrap(values);
    }


    /**
     * Ensures that a matrix dimension is non-negative.
     *
     * @param dim the matrix dimension to validate.
     *
     * @throws RuntimeException if the dimension is negative.
     */
    static void validateDimension(int dim) {
        if (dim < 0)
            throw new D3xException("Matrix dimension [%d] is negative.", dim);
    }

    /**
     * Ensures that a matrix shape is valid.
     *
     * @param nrow the number of rows to validate.
     * @param ncol the number of columns to validate.
     *
     * @throws RuntimeException if either dimension is negative.
     */
    static void validateShape(int nrow, int ncol) {
        validateDimension(nrow);
        validateDimension(ncol);
    }

    /**
     * Returns a deep copy of this matrix.
     *
     * <p>Note that this method cannot be named simply {@code copy()}, because
     * that would duplicate the same method from the Apache RealMatrix class.</p>
     *
     * @return a deep copy of this matrix.
     */
    default D3xMatrix copyOf() {
        D3xMatrix copy = like();

        for (int i = 0; i < nrow(); ++i)
            for (int j = 0; j < ncol(); ++j)
                copy.set(i, j, this.get(i, j));

        return copy;
    }

    /**
     * Determines whether the entries in this matrix are equal to those in a bare array
     * <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param values the values to test for equality.
     *
     * @return {@code true} iff the input array has the same shape as this matrix and
     * each value matches the corresponding entry in this matrix within the tolerance
     * of the default DoubleComparator.
     */
    default boolean equalsArray(double[][] values) {
        return equalsArray(values, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this matrix are equal to those in a bare array
     * within the tolerance of a given DoubleComparator.
     *
     * @param values     the values to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input array has the same shape as this matrix and
     * each value matches the corresponding entry in this matrix within the tolerance
     * of the specified comparator.
     */
    default boolean equalsArray(double[][] values, DoubleComparator comparator) {
        if (values.length != nrow())
            return false;

        for (int i = 0; i < nrow(); ++i) {
            double[] rowi = values[i];

            if (rowi.length != ncol())
                return false;

            for (int j = 0; j < ncol(); ++j)
                if (!comparator.equals(rowi[j], get(i, j)))
                    return false;
        }

        return true;
    }

    /**
     * Determines whether the entries in this matrix are equal to those in another matrix
     * <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param that the matrix to test for equality.
     *
     * @return {@code true} iff the input matrix has the same shape as this matrix
     * and each value matches the corresponding entry in this matrix within the tolerance
     * of the default DoubleComparator.
     */
    default boolean equalsMatrix(D3xMatrix that) {
        return equalsMatrix(that, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this matrix are equal to those in another matrix
     * within the tolerance of a given DoubleComparator.
     *
     * @param that       the matrix to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input matrix has the same shape as this matrix
     * and each value matches the corresponding entry in this matrix within the tolerance
     * of the specified comparator.
     */
    default boolean equalsMatrix(D3xMatrix that, DoubleComparator comparator) {
        if (this.nrow() != that.nrow())
            return false;

        if (this.ncol() != that.ncol())
            return false;

        for (int i = 0; i < nrow(); ++i)
            for (int j = 0; j < ncol(); ++j)
                if (!comparator.equals(this.get(i, j), that.get(i, j)))
                    return false;

        return true;
    }

    /**
     * Determines if this matrix has the same shape as another matrix.
     *
     * @param that the matrix to compare to this.
     *
     * @return {@code true} iff the input matrix has the same shape as this matrix.
     */
    default boolean isCongruent(D3xMatrix that) {
        return this.nrow() == that.nrow() && this.ncol() == that.ncol();
    }

    /**
     * Returns a new matrix with the same shape and concrete type as this matrix.
     *
     * @return a new matrix with the same shape and concrete type as this matrix.
     */
    default D3xMatrix like() {
        return like(nrow(), ncol());
    }

    /**
     * Returns the number of elements in this matrix (the product of the number
     * of rows and the number of columns).
     *
     * @return the number of elements in this matrix.
     */
    default int size() {
        return nrow() * ncol();
    }

    /**
     * Returns the elements of this matrix in a new array, which must not contain
     * a reference to the underlying data in this matrix.  Subsequent changes to
     * the returned array must not change the contents of this matrix.
     *
     * @return the elements of this matrix in a new array.
     */
    default double[][] toArray() {
        double[][] array = new double[nrow()][];

        for (int i = 0; i < nrow(); ++i) {
            double[] rowi = new double[ncol()];

            for (int j = 0; j < ncol(); ++j)
                rowi[j] = get(i, j);

            array[i] = rowi;
        }

        return array;
    }

    /**
     * Ensures that this matrix has the same shape as another matrix.
     *
     * @param that the matrix to validate.
     *
     * @throws RuntimeException unless the specified matrix has the same
     * shape as this matrix.
     */
    default void validateCongruent(D3xMatrix that) {
        if (!isCongruent(that))
            throw new D3xException("Matrix shape mismatch: [(%d, %d) != (%d, %d)].",
                    this.nrow(), this.ncol(), that.nrow(), that.ncol());
    }
}
