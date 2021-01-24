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
import java.util.Random;

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
 *
 * @author Scott Shaffer
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
     * Creates a deep copy of this vector.
     * @return a deep copy of this vector.
     */
    D3xVector copy();

    /**
     * Creates a new vector with the same concrete type as this vector.
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
     * Creates a new vector with the same length and concrete type as this vector.
     * @return a new vector with the same length and concrete type as this vector.
     */
    default D3xVector like() {
        return like(length());
    }

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
        return copy().combineInPlace(a, b, v);
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
     * Computes the sum of this vector and another and returns the sum
     * in a new vector; this vector is unchanged.
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
     * Computes the difference of this vector and another and returns the
     * difference in a new vector; this vector is unchanged.
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
     * Computes the product of this vector and a scalar factor and returns the
     * product in a new vector; this vector is unchanged.
     *
     * @param scalar the scalar factor.
     *
     * @return a new vector containing the product of this vector and the given
     * factor.
     */
    D3xVector times(double scalar);

    /**
     * Multiplies each element of this vector by a scalar factor and modifies
     * the elements of this vector in place.
     *
     * @param scalar the scalar factor.
     *
     * @return this vector, for operator chaining.
     */
    D3xVector multiplyInPlace(double scalar);

    /**
     * Divides each element of this vector by a scalar factor and modifies
     * the elements of this vector in place.
     *
     * @param scalar the scalar factor.
     *
     * @return this vector, for operator chaining.
     */
    default D3xVector divideInPlace(double scalar) {
        return multiplyInPlace(1.0 / scalar);
    }

    /**
     * Creates a new vector from a continuous range of elements in this vector.
     *
     * @param start the first element to include in the sub-vector.
     * @param length the length of the sub-vector.
     *
     * @return a new vector containing the elements from {@code start} (inclusive)
     * to {@code start + length} (exclusive).
     *
     * @throws RuntimeException unless the starting element and sub-vector
     * length define a valid range in this vector.
     */
    default D3xVector getSubVector(int start, int length) {
        validateIndex(start);
        validateIndex(start + length - 1);
        D3xVector result = like(length);

        for (int subIndex = 0; subIndex < length; ++subIndex)
            result.set(subIndex, this.get(start + subIndex));

        return result;
    }

    /**
     * Assigns a continuous range of elements in this vector from another vector.
     *
     * @param start the first element in the assignment range.
     * @param subVector the sub-vector to assign.
     *
     * @return this vector, for operator chaining.
     *
     * @throws RuntimeException unless the assignment range is valid.
     */
    default D3xVector setSubVector(int start, D3xVector subVector) {
        validateIndex(start);
        validateIndex(start + subVector.length() - 1);

        for (int subIndex = 0; subIndex < subVector.length(); ++subIndex)
            set(start + subIndex, subVector.get(subIndex));

        return this;
    }

    /**
     * Creates a new vector by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new vector containing a copy of the specified array.
     */
    static D3xVector copyOf(double... values) {
        return ApacheVector.copyOf(values);
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
        return rep(0.0, length);
    }

    /**
     * Returns an empty vector.
     * @return an empty vector.
     */
    static D3xVector empty() {
        return ApacheVector.EMPTY;
    }

    /**
     * Creates a new vector and populates it with pseudorandom values
     * distributed uniformly over the interval {@code [0.0, 1.0)}.
     *
     * @param length the length of the vector.
     * @param random the random number source.
     *
     * @return a new vector of the specified length populated with uniform
     * random variables.
     */
    static D3xVector random(int length, Random random) {
        D3xVector result = dense(length);

        for (int index = 0; index < length; ++index)
            result.set(index, random.nextDouble());

        return result;
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
        return ApacheVector.rep(x, n);
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
        return ApacheVector.sparse(length);
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
    static D3xVector wrap(double... values) {
        return ApacheVector.wrap(values);
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
     * Creates a string representation of this vector.
     *
     * @return a string representation of this vector.
     */
    default String format() {
        return Arrays.toString(toArray());
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
     * Creates a new array with the same contents as this vector. Changes to the
     * returned array will not be reflected in this vector, and changes to this
     * vector will not be reflected in the returned array.
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

    /**
     * Ensures that an element index is valid.
     *
     * @param index the index to validate.
     *
     * @throws RuntimeException if the specified index is out of bounds.
     */
    default void validateIndex(int index) {
        if (index < 0 || index >= length())
            throw new D3xException("Index [%d] is out of bounds [0, %d).", index, length());
    }
}
