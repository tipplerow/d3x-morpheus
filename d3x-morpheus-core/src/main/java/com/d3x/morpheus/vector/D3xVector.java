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

import java.util.Arrays;
import java.util.List;

import com.d3x.core.lang.D3xException;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameRow;
import com.d3x.morpheus.util.DoubleComparator;

/**
 * Represents a fixed-length vector of {@code double} values, provides static
 * factory methods with explicit control over data ownership, and provides
 * reference implementations of standard operations from linear algebra.
 */
public interface D3xVector {
    /**
     * Returns the length of this vector.
     * @return the length of this vector.
     */
    int length();

    /**
     * Returns the value of an element at a given location.
     *
     * @param index the index of the element to return.
     *
     * @return the value of the element at the specified location.
     *
     * @throws RuntimeException if the index is out of bounds.
     */
    double get(int index);

    /**
     * Assigns the value of an element at a given location.
     *
     * @param index the index of the element to assign.
     * @param value the value to assign.
     *
     * @throws RuntimeException if the index is out of bounds.
     */
    void set(int index, double value);

    /**
     * Returns a deep copy of this vector.
     *
     * <p>Note that this method cannot be named simply {@code copy()}, because
     * that would duplicate the same method from the Apache RealVector class.</p>
     *
     * @return a deep copy of this vector.
     */
    D3xVector copyThis();

    /**
     * Returns a new vector with the same concrete type as this vector.
     *
     * @param length the desired length of the new vector.
     *
     * @return a new vector with the specified length having the same
     * concrete type as this vector.
     *
     * @throws RuntimeException if the length is negative.
     */
    D3xVector like(int length);

    /**
     * Forms a linear combination of this vector and another vector and
     * returns the result in a new vector; this vector is unchanged.
     *
     * <p>Implementations should override this default method when a more
     * efficient algorithm is available for the underlying storage.</p>
     *
     * @param a the coefficient to multiply this vector.
     * @param b the coefficient to multiply the input vector.
     * @param v the vector to combine with this vector.
     *
     * @return the linear combination {@code a * this + b * v} in a new
     * vector object.
     *
     * @throws RuntimeException unless the input vector has the same length
     * as this vector.
     */
    default D3xVector combine(double a, double b, D3xVector v) {
        return copyThis().combineInPlace(a, b, v);
    }

    /**
     * Forms a linear combination of this vector and another vector and
     * stores the result in this vector.
     *
     * <p>Implementations should override this default method when a more
     * efficient algorithm is available for the underlying storage.</p>
     *
     * @param a the coefficient to multiply this vector.
     * @param b the coefficient to multiply the input vector.
     * @param v the vector to combine with this vector.
     *
     * @return this vector, for operator chaining, with elements updated
     * as {@code a * this + b * v}.
     *
     * @throws RuntimeException unless the input vector has the same length
     * as this vector.
     */
    default D3xVector combineInPlace(double a, double b, D3xVector v) {
        validateCongruent(v);

        for (int index = 0; index < length(); ++index)
            this.set(index, a * this.get(index) + b * v.get(index));

        return this;
    }

    /**
     * Returns the sum of this vector and another in a new vector; this
     * vector is unchanged.
     *
     * @param addend the vector to add to this vector.
     *
     * @return a new vector containing the sum of this vector and the
     * specified addend.
     *
     * @throws RuntimeException unless the addend has the same length
     * as this vector.
     */
    default D3xVector plus(D3xVector addend) {
        return combine(1.0, 1.0, addend);
    }

    /**
     * Adds another vector to this vector and modifies the elements of
     * this vector in place.
     *
     * @param addend the vector to add to this vector.
     *
     * @return this vector, for operator chaining.
     *
     * @throws RuntimeException unless the addend has the same length
     * as this vector.
     */
    default D3xVector addInPlace(D3xVector addend) {
        return combineInPlace(1.0, 1.0, addend);
    }

    /**
     * Returns the difference of this vector and another in a new vector;
     * this vector is unchanged.
     *
     * @param subtrahend the vector to subtract from this vector.
     *
     * @return a new vector containing the difference of this vector and
     * the specified subtrahend.
     *
     * @throws RuntimeException unless the subtrahend has the same length
     * as this vector.
     */
    default D3xVector minus(D3xVector subtrahend) {
        return combine(1.0, -1.0, subtrahend);
    }

    /**
     * Subtracts another vector from this vector and modifies the elements
     * of this vector in place.
     *
     * @param subtrahend the vector to subtract from this vector.
     *
     * @return this vector, for operator chaining.
     *
     * @throws RuntimeException unless the subtrahend has the same length
     * as this vector.
     */
    default D3xVector subtractInPlace(D3xVector subtrahend) {
        return combineInPlace(1.0, -1.0, subtrahend);
    }

    /**
     * Creates a new vector by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new vector containing a copy of the specified array.
     */
    static D3xVector copyOf(double... values) {
        return ApacheDenseVector.copyOf(values);
    }

    /**
     * Creates a new vector by copying values from a column in a Morpheus
     * DataFrame.
     *
     * @param frame  the source DataFrame.
     * @param colKey the key of the column to copy.
     *
     * @return a new vector containing a copy of the specified column in
     * the given frame (in row order).
     *
     * @throws DataFrameException unless the frame contains the specified
     * column.
     */
    static <R,C> D3xVector copyColumn(DataFrame<R,C> frame, C colKey) {
        return copyColumn(frame, frame.listRowKeys(), colKey);
    }

    /**
     * Creates a new vector by copying selected rows from a column in a
     * Morpheus DataFrame.
     *
     * @param frame   the source DataFrame.
     * @param rowKeys the keys of the rows to copy.
     * @param colKey  the key of the column to copy.
     *
     * @return a new vector containing a copy of the specified values in
     * the given frame.
     *
     * @throws DataFrameException unless the frame contains the specified
     * column and rows.
     */
    static <R,C> D3xVector copyColumn(DataFrame<R,C> frame, List<R> rowKeys, C colKey) {
        frame.requireRows(rowKeys);
        frame.requireDoubleColumn(colKey);

        D3xVector vector = dense(rowKeys.size());
        DataFrameColumn<R,C> column = frame.col(colKey);

        for (int i = 0; i < rowKeys.size(); ++i)
            vector.set(i, column.getDouble(rowKeys.get(i)));

        return vector;
    }

    /**
     * Creates a new vector by copying values from a row in a Morpheus
     * DataFrame.
     *
     * @param frame  the source DataFrame.
     * @param rowKey the key of the row to copy.
     *
     * @return a new vector containing a copy of the specified row in
     * the given frame (in column order).
     *
     * @throws DataFrameException unless the frame contains the specified
     * row.
     */
    static <R,C> D3xVector copyRow(DataFrame<R,C> frame, R rowKey) {
        return copyRow(frame, rowKey, frame.listColumnKeys());
    }

    /**
     * Creates a new vector by copying selected columns from a row in a
     * Morpheus DataFrame.
     *
     * @param frame   the source DataFrame.
     * @param rowKey  the key of the row to copy.
     * @param colKeys the keys of the columns to copy.
     *
     * @return a new vector containing a copy of the specified values in
     * the given frame.
     *
     * @throws DataFrameException unless the frame contains the specified
     * row and columns.
     */
    static <R,C> D3xVector copyRow(DataFrame<R,C> frame, R rowKey, List<C> colKeys) {
        frame.requireRow(rowKey);
        frame.requireDoubleColumns(colKeys);

        D3xVector vector = dense(colKeys.size());
        DataFrameRow<R,C> row = frame.row(rowKey);

        for (int j = 0; j < colKeys.size(); ++j)
            vector.set(j, row.getDouble(colKeys.get(j)));

        return vector;
    }

    /**
     * Creates a new vector with dense physical storage and all elements
     * initialized to zero.
     *
     * @param length the length of the vector.
     *
     * @return a new vector of the specified length with dense physical
     * storage and all elements initialized to zero.
     *
     * @throws RuntimeException if the length is negative.
     */
    static D3xVector dense(int length) {
        return ApacheDenseVector.ofLength(length);
    }

    /**
     * Like the {@code R} function {@code rep(x, n)}, creates a new vector
     * containing the value {@code x} replicated {@code n} times.
     *
     * @param x the value to replicate.
     * @param n the number of times to replicate.
     *
     * @return a new vector of length {@code n} with each element assigned
     * the value {@code x}.
     *
     * @throws RuntimeException if {@code n < 0}.
     */
    static D3xVector rep(double x, int n) {
        return ApacheDenseVector.rep(x, n);
    }

    /**
     * Creates a new vector with sparse physical storage and all elements
     * initialized to zero.
     *
     * @param length the length of the vector.
     *
     * @return a new vector of the specified length with sparse physical
     * storage and all elements initialized to zero.
     *
     * @throws RuntimeException if the length is negative.
     */
    static D3xVector sparse(int length) {
        return ApacheSparseVector.ofLength(length);
    }

    /**
     * Ensures that a vector length is non-negative.
     *
     * @param length the length to validate.
     *
     * @throws RuntimeException if the length is negative.
     */
    static void validateLength(int length) {
        if (length < 0)
            throw new D3xException("Length [%d] is negative.", length);
    }

    /**
     * Creates a mutable vector view over a bare array (a shallow copy).
     * Changes to this vector will be reflected in the input array, and
     * changes to the array will be reflected in this vector.
     *
     * @param values the values to be viewed.
     *
     * @return a mutable vector view over the specified array.
     */
    static D3xVector wrap(double[] values) {
        return ApacheDenseVector.wrap(values);
    }

    /**
     * Determines whether the entries in this vector are equal to those in a bare array
     * <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param values the values to test for equality.
     *
     * @return {@code true} iff the input array has the same length as this vector and
     * each value matches the corresponding entry in this vector within the tolerance
     * of the default DoubleComparator.
     */
    default boolean equalsArray(double... values) {
        return equalsArray(values, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this vector are equal to those in a bare array
     * within the tolerance of a given DoubleComparator.
     *
     * @param values     the values to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input array has the same length as this vector and
     * each value matches the corresponding entry in this vector within the tolerance
     * of the specified comparator.
     */
    default boolean equalsArray(double[] values, DoubleComparator comparator) {
        if (values.length != this.length())
            return false;

        for (int index = 0; index < values.length; ++index)
            if (!comparator.equals(values[index], get(index)))
                return false;

        return true;
    }

    /**
     * Determines whether the entries in this vector are equal to those in another vector
     * <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param that the vector to test for equality.
     *
     * @return {@code true} iff the input vector has the same length as this vector and
     * each value matches the corresponding entry in this vector within the tolerance
     * of the default DoubleComparator.
     */
    default boolean equalsVector(D3xVector that) {
        return equalsVector(that, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this vector are equal to those in another vector
     * within the tolerance of a given DoubleComparator.
     *
     * @param that       the vector to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input vector has the same length as this vector and
     * each value matches the corresponding entry in this vector within the tolerance
     * of the specified comparator.
     */
    default boolean equalsVector(D3xVector that, DoubleComparator comparator) {
        if (this.length() != that.length())
            return false;

        for (int index = 0; index < length(); ++index)
            if (!comparator.equals(this.get(index), that.get(index)))
                return false;

        return true;
    }

    /**
     * Determines if this vector has the same length as another vector.
     *
     * @param that the vector to compare to this.
     *
     * @return {@code true} iff the input vector has the same length as this vector.
     */
    default boolean isCongruent(D3xVector that) {
        return this.length() == that.length();
    }

    /**
     * Returns a string representation of this vector.
     *
     * @return a string representation of this vector.
     */
    default String format() {
        return Arrays.toString(toArray());
    }

    /**
     * Returns a new vector with the same length and concrete type as this vector.
     *
     * @return a new vector with the same length and concrete type as this vector.
     */
    default D3xVector like() {
        return like(length());
    }

    /**
     * Returns the elements of this vector in a new array, which must not contain
     * a reference to the underlying data in this vector.  Subsequent changes to
     * the returned array must not change the contents of this vector.
     *
     * @return the elements of this vector in a new array.
     */
    default double[] toArray() {
        double[] array = new double[length()];

        for (int index = 0; index < array.length; ++index)
            array[index] = get(index);

        return array;
    }

    /**
     * Ensures that this vector has the same length as another vector.
     *
     * @param that the vector to validate.
     *
     * @throws RuntimeException unless the specified vector has the same length as
     * this vector.
     */
    default void validateCongruent(D3xVector that) {
        if (!isCongruent(that))
            throw new D3xException("Vector length mismatch: [%d != %d].", this.length(), that.length());
    }
}
