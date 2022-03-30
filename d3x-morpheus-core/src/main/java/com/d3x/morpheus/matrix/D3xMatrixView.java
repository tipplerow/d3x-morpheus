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

import java.util.function.DoublePredicate;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVectorView;

/**
 * Provides a read-only view of {@code double} values that are accessed
 * by ordinal row and column indexes (location).
 *
 * @author Scott Shaffer
 */
public interface D3xMatrixView {
    /**
     * Returns the number of rows in this matrix view.
     * @return the number of rows in this matrix view.
     */
    int nrow();

    /**
     * Returns the number of columns in this matrix view.
     * @return the number of columns in this matrix view.
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
     * Returns a matrix view over a bare two-dimensional array.
     *
     * @param array the two-dimensional array to wrap in a view.
     *
     * @return a matrix view over the given array.
     */
    static D3xMatrixView of(double[][] array) {
        return new ArrayView(array);
    }

    /**
     * Returns a matrix view over a DataFrame.
     *
     * @param frame the DataFrame to wrap in a view.
     *
     * @return a matrix view over the given frame.
     */
    static D3xMatrixView of(DataFrame<?,?> frame) {
        return new FrameView(frame);
    }

    /**
     * Tests whether all elements in this matrix satisfy a predicate.
     *
     * @param predicate the predicate to evaluate.
     *
     * @return {@code true} iff every element in this matrix satisfies
     * the specified predicate.
     */
    default boolean all(DoublePredicate predicate) {
        for (int row = 0; row < nrow(); ++row)
            for (int col = 0; col < ncol(); ++col)
                if (!predicate.test(get(row, col)))
                    return false;

        return true;
    }

    /**
     * Tests whether any element in this matrix satisfies a predicate.
     *
     * @param predicate the predicate to evaluate.
     *
     * @return {@code true} iff at least one element in this matrix
     * satisfies the specified predicate.
     */
    default boolean any(DoublePredicate predicate) {
        for (int row = 0; row < nrow(); ++row)
            for (int col = 0; col < ncol(); ++col)
                if (predicate.test(get(row, col)))
                    return true;

        return false;
    }

    /**
     * Returns a vector view of a column in this matrix.
     *
     * @param colIndex the index of the column to view.
     *
     * @return a vector view of the specified column.
     *
     * @throws RuntimeException unless the column index is valid.
     */
    default D3xVectorView column(int colIndex) {
        return new ColumnView(this, colIndex);
    }

    /**
     * Returns a subvector view of a portion of a column in this matrix.
     *
     * @param colIndex  the index of the column to view.
     * @param rowOffset the row index of the first element in the
     *                  subvector view.
     * @param colLength the total length of the subvector view.
     *
     * @return a subvector view of the specified column portion.
     *
     * @throws RuntimeException unless the subvector view defined by the
     * input parameters lies completely within this matrix.
     */
    default D3xVectorView column(int colIndex, int rowOffset, int colLength) {
        return new ColumnView(this, colIndex, rowOffset, colLength);
    }

    /**
     * Determines whether the entries in this view are equal to those in a bare
     * array <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param values the values to test for equality.
     *
     * @return {@code true} iff the input array has the same shape as this view
     * and each value matches the corresponding entry in this matrix within the
     * tolerance of the default DoubleComparator.
     */
    default boolean equalsArray(double[][] values) {
        return equalsArray(values, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this view are equal to those in a bare
     * array within the tolerance of a given DoubleComparator.
     *
     * @param values     the values to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input array has the same shape as this view
     * and each value matches the corresponding entry in this matrix within the
     * tolerance of the specified comparator.
     */
    default boolean equalsArray(double[][] values, DoubleComparator comparator) {
        return equalsView(of(values), comparator);
    }

    /**
     * Determines whether the entries in this view are equal to those in another
     * view <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param that the matrix view to test for equality.
     *
     * @return {@code true} iff the input view has the same shape as this view
     * and each value matches the corresponding entry in this view within the
     * tolerance of the default DoubleComparator.
     */
    default boolean equalsView(D3xMatrixView that) {
        return equalsView(that, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this view are equal to those in another
     * view within the tolerance of a given DoubleComparator.
     *
     * @param that       the matrix to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input view has the same shape as this view
     * and each value matches the corresponding entry in this view within the
     * tolerance of the specified comparator.
     */
    default boolean equalsView(D3xMatrixView that, DoubleComparator comparator) {
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
    default boolean isCongruent(D3xMatrixView that) {
        return this.nrow() == that.nrow() && this.ncol() == that.ncol();
    }

    /**
     * Identifies diagonal matrices.
     *
     * @return {@code true} iff this matrix is diagonal.
     */
    default boolean isDiagonal() {
        if (!isSquare())
            return false;

        for (int irow = 0; irow < nrow(); ++irow)
            for (int jcol = 0; jcol < ncol(); ++jcol)
                if (jcol != irow && DoubleComparator.DEFAULT.isNonZero(get(irow, jcol)))
                    return false;

        return true;
    }

    /**
     * Identifies empty matrices.
     *
     * @return {@code true} iff this matrix contains no elements.
     */
    boolean isEmpty();

    /**
     * Identifies square matrices.
     *
     * @return {@code true} iff this matrix is square.
     */
    default boolean isSquare() {
        return nrow() == ncol();
    }

    /**
     * Identifies symmetric matrices.
     *
     * @return {@code true} iff this matrix is symmetric.
     */
    default boolean isSymmetric() {
        if (!isSquare())
            return false;

        for (int irow = 0; irow < nrow(); ++irow)
            for (int jcol = irow + 1; jcol < ncol(); ++jcol)
                if (!DoubleComparator.DEFAULT.equals(get(irow, jcol), get(jcol, irow)))
                    return false;

        return true;
    }

    /**
     * Returns a vector view of a row in this matrix.
     *
     * @param rowIndex the index of the row to view.
     *
     * @return a vector view of the specified row.
     *
     * @throws RuntimeException unless the row index is valid.
     */
    default D3xVectorView row(int rowIndex) {
        return new RowView(this, rowIndex);
    }

    /**
     * Returns a subvector view of a portion of a row in this matrix.
     *
     * @param rowIndex  the index of the row to view.
     * @param colOffset the column index of the first element in the
     *                  subvector view.
     * @param rowLength the total length of the subvector view.
     *
     * @return a subvector view of the specified row portion.
     *
     * @throws RuntimeException unless the subvector view defined by the
     * input parameters lies completely within this matrix.
     */
    default D3xVectorView row(int rowIndex, int colOffset, int rowLength) {
        return new RowView(this, rowIndex, colOffset, rowLength);
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
     * Copies the elements of this view into a new array, which does not contain
     * a reference to the underlying data in this view.  Changes to the returned
     * array will not be reflected in this view.
     *
     * @return the elements of this view in a new array.
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
    default void validateColumnVector(int col, D3xVectorView vec) {
        validateColumnIndex(col);

        if (vec.length() != nrow())
            throw new MorpheusException("Invalid column vector length.");
    }

    /**
     * Ensures that this matrix has the same shape as another matrix.
     *
     * @param that the matrix to validate.
     *
     * @throws RuntimeException unless the specified matrix has the same
     * shape as this matrix.
     */
    default void validateCongruent(D3xMatrixView that) {
        if (!isCongruent(that))
            throw new MorpheusException("Matrix shape mismatch: [(%d, %d) != (%d, %d)].",
                    this.nrow(), this.ncol(), that.nrow(), that.ncol());
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
            throw new MorpheusException("Invalid index: [%d] not in range [0, %d).", index, dimension);
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
    default void validateRowVector(int row, D3xVectorView vec) {
        validateRowIndex(row);

        if (vec.length() != ncol())
            throw new MorpheusException("Invalid row vector length.");
    }
}
