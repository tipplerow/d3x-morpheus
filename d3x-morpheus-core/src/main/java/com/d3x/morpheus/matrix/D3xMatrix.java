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
import com.d3x.morpheus.vector.D3xVector;

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
     * Creates a deep copy of this matrix.
     * @return a deep copy of this matrix.
     */
    D3xMatrix copy();

    /**
     * Creates a new matrix with the same concrete type as this matrix.
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
     * Computes the product {@code Ax} of this matrix {@code A} and a
     * vector {@code x} and returns the result in a new vector.
     *
     * @param x the vector factor.
     *
     * @return the product {@code Ax} of this matrix {@code A} and the
     * input vector {@code x}.
     *
     * @throws RuntimeException unless the length of the input vector
     * matches the column dimension of this matrix.
     */
    D3xVector times(D3xVector x);

    /**
     * Computes the product {@code AB} of this matrix {@code A} and another
     * matrix {@code B} and returns the result in a new matrix.
     *
     * @param B the right matrix factor.
     *
     * @return the product {@code AB} of this matrix {@code A} and the
     * input matrix {@code B}.
     *
     * @throws RuntimeException unless the row dimension of the input
     * matrix matches the column dimension of this matrix.
     */
    D3xMatrix times(D3xMatrix B);

    /**
     * Creates the transpose of this matrix; this matrix is unchanged.
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
        frame.requireNumericColumns(colKeys);

        D3xMatrix matrix = dense(rowKeys.size(), colKeys.size());

        for (int i = 0; i < rowKeys.size(); ++i)
            for (int j = 0; j < colKeys.size(); ++j)
                matrix.set(i, j, frame.getDouble(rowKeys.get(i), colKeys.get(j)));

        return matrix;
    }

    /**
     * Creates a new matrix by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new matrix containing a copy of the specified array.
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
     * @return a new matrix with dense physical storage and all elements
     * initialized to zero.
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
     * Creates a new matrix with storage only for diagonal elements and
     * initializes those elements with a vector of diagonal values.
     *
     * @param diagonals the diagonal values to assign.
     *
     * @return a new matrix with storage for only diagonal elements with
     * those elements assigned by the input vector.
     */
    static D3xMatrix diagonal(D3xVector diagonals) {
        return ApacheMatrix.diagonal(diagonals);
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
     * Creates a mutable matrix view over a bare array (a shallow copy).
     * Changes to the returned matrix will be reflected in the input
     * array, and changes to the array will be reflected in the matrix.
     *
     * @param values the values to be wrapped.
     *
     * @return a mutable matrix view using the specified array for its
     * physical storage.
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
     * Ensures that a row or column index is valid.
     *
     * @param index the row or column index to validate.
     * @param dimension the row or column dimension in the matrix.
     *
     * @throws RuntimeException unless the index is valid.
     */
    static void validateIndex(int index, int dimension) {
        if (index < 0 || index >= dimension)
            throw new D3xException("Invalid index: [%d] not in range [0, %d).", index, dimension);
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
     * Returns the elements of this matrix in a vector having row-major order.
     * @return the elements of this matrix in a vector having row-major order.
     */
    default D3xVector byrow() {
        int vectorIndex = 0;
        D3xVector elementVector = D3xVector.dense(size());

        for (int i = 0; i < nrow(); ++i) {
            for (int j = 0; j < ncol(); ++j) {
                elementVector.set(vectorIndex, get(i, j));
                ++vectorIndex;
            }
        }

        return elementVector;
    }

    /**
     * Returns the elements of this matrix in a vector having column-major order.
     * @return the elements of this matrix in a vector having column-major order.
     */
    default D3xVector bycol() {
        int vectorIndex = 0;
        D3xVector elementVector = D3xVector.dense(size());

        for (int j = 0; j < ncol(); ++j) {
            for (int i = 0; i < nrow(); ++i) {
                elementVector.set(vectorIndex, get(i, j));
                ++vectorIndex;
            }
        }

        return elementVector;
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
     * Returns a string representation of this matrix that can be cut-and-pasted
     * into an {@code R} session.
     *
     * @return a string representation of this matrix that can be cut-and-pasted
     * into an {@code R} session.
     */
    default String formatR() {
        D3xVector elements = byrow();
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("matrix(nrow = %d, ncol = %d, byrow = TRUE, data = c(", nrow(), ncol()));

        if (elements.length() > 0)
            builder.append(elements.get(0));

        for (int index = 1; index < elements.length(); ++index) {
            builder.append(", ");
            builder.append(elements.get(index));
        }

        builder.append("))");
        return builder.toString();
    }

    /**
     * Creates a copy of the diagonal elements in this matrix.
     * @return a copy of the diagonal elements in this matrix.
     * @throws RuntimeException unless this is a square matrix.
     */
    default D3xVector getDiagonal() {
        if (!isSquare())
            throw new D3xException("Non-square matrix.");

        D3xVector diagonal = D3xVector.dense(nrow());

        for (int i = 0; i < nrow(); i++)
            diagonal.set(i, get(i, i));

        return diagonal;
    }

    /**
     * Copies the elements from a column of this matrix into a new vector.
     *
     * @param col the index of the column to copy.

     * @return a new vector with a copy of the contents in the specified column.
     *
     * @throws RuntimeException unless the column index is valid.
     */
    default D3xVector getColumn(int col) {
        validateColumnIndex(col);
        D3xVector vec = D3xVector.dense(nrow());

        for (int row = 0; row < nrow(); ++row)
            vec.set(row, get(row, col));

        return vec;
    }

    /**
     * Copies the elements from a row of this matrix into a new vector.
     *
     * @param row the index of the row to copy.
     *
     * @return a new vector with a copy of the contents in the specified row.
     *
     * @throws RuntimeException unless the row index is valid.
     */
    default D3xVector getRow(int row) {
        validateRowIndex(row);
        D3xVector vec = D3xVector.dense(ncol());

        for (int col = 0; col < ncol(); ++col)
            vec.set(col, get(row, col));

        return vec;
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
     * Identifies square matrices.
     *
     * @return {@code true} iff this matrix is square.
     */
    default boolean isSquare() {
        return nrow() == ncol();
    }

    /**
     * Creates a new matrix with the same shape and concrete type as this matrix.
     * @return a new matrix with the same shape and concrete type as this matrix.
     */
    default D3xMatrix like() {
        return like(nrow(), ncol());
    }

    /**
     * Assigns the elements in a column of this matrix to the values of a column vector.
     *
     * @param col the index of the column to assign.
     * @param vec the column vector to assign.
     *
     * @throws RuntimeException unless the column index is valid and the length of the
     * column vector matches the number of rows in this matrix.
     */
    default void setColumn(int col, D3xVector vec) {
        validateColumnVector(col, vec);

        for (int row = 0; row < nrow(); ++row)
            set(row, col, vec.get(row));
    }

    /**
     * Assigns the elements in a row of this matrix to the values of a row vector.
     *
     * @param row the index of the row to assign.
     * @param vec the row vector to assign.
     *
     * @throws RuntimeException unless the row index is valid and the length of the
     * row vector matches the number of columns in this matrix.
     */
    default void setRow(int row, D3xVector vec) {
        validateRowVector(row, vec);

        for (int col = 0; col < ncol(); ++col)
            set(row, col, vec.get(col));
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
     * Copies the elements of this matrix into a new array, which does not contain
     * a reference to the underlying data in this matrix.
     *
     * <p>Changes to the returned array will not be reflected in this matrix, and
     * changes to this matrix will not be reflected in the returned array.</p>
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
     * Ensures that a column index is valid.
     *
     * @param col the index of the column to validate.
     *
     * @throws RuntimeException unless the column index is valid.
     */
    default void validateColumnIndex(int col) {
        validateIndex(col, ncol());
    }

    /**
     * Ensures that a column vector is valid.
     *
     * @param col the index of the column to validate.'
     * @param vec the column vector to validate.
     *
     * @throws RuntimeException unless the column index is valid and the
     * length of the vector matches the number of rows in this matrix.
     */
    default void validateColumnVector(int col, D3xVector vec) {
        validateColumnIndex(col);

        if (vec.length() != nrow())
            throw new D3xException("Invalid column vector length.");
    }

    /**
     * Ensures that a row index is valid.
     *
     * @param row the index of the row to validate.
     *
     * @throws RuntimeException unless the row index is valid.
     */
    default void validateRowIndex(int row) {
        validateIndex(row, nrow());
    }

    /**
     * Ensures that a row vector is valid.
     *
     * @param row the index of the row to validate.'
     * @param vec the row vector to validate.
     *
     * @throws RuntimeException unless the row index is valid and the
     * length of the vector matches the number of columns in this matrix.
     */
    default void validateRowVector(int row, D3xVector vec) {
        validateRowIndex(row);

        if (vec.length() != ncol())
            throw new D3xException("Invalid row vector length.");
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
