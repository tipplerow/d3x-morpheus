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

import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameRow;
import com.d3x.morpheus.frame.DataFrameVector;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.stats.SumSquares;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import lombok.NonNull;

/**
 * Represents a fixed-length vector of {@code double} values, provides static
 * factory methods with explicit control over data ownership, and provides
 * reference implementations of standard operations from linear algebra.
 *
 * @author Scott Shaffer
 */
public interface D3xVector extends D3xVectorView {
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
     * Applies a unary operator in place: Replaces each element with the result
     * of applying a unary operator to that element.
     *
     * @param operator the operator to apply.
     *
     * @return this vector, for operator chaining.
     */
    default D3xVector apply(DoubleUnaryOperator operator) {
        for (int index = 0; index < length(); ++index)
            set(index, operator.applyAsDouble(get(index)));

        return this;
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
     * Creates a deep copy of this vector.
     * @return a deep copy of this vector.
     */
    default D3xVector copy() {
        var result = like();

        for (int index = 0; index < length(); ++index)
            result.set(index, get(index));

        return result;
    }

    /**
     * Computes the dot product of this vector and another.
     *
     * @param that the second vector in the dot product.
     *
     * @return the dot product of this vector and the input vector.
     *
     * @throws RuntimeException unless the input vector has the same
     * length as this vector.
     */
    default double dot(D3xVector that) {
        validateCongruent(that);
        double result = 0.0;

        for (int index= 0; index < length(); ++index)
            result += this.get(index) * that.get(index);

        return result;
    }

    /**
     * Computes the 1-norm of this vector: the sum of the absolute values of each element.
     *
     * @return the 1-norm of this data vector.
     */
    default double norm1() {
        return stream().filter(x -> !Double.isNaN(x)).map(Math::abs).sum();
    }

    /**
     * Computes the 2-norm (Euclidean norm) of this vector.
     *
     * @return the 2-norm (Euclidean norm) of this vector.
     */
    default double norm2() {
        return Math.sqrt(new SumSquares().compute(this));
    }

    /**
     * Rescales this vector (in place) so that the elements sum to one.
     *
     * @return this vector, modified, for operator chaining.
     *
     * @throws RuntimeException if the elements sum to zero within a
     * small floating-point tolerance.
     */
    default D3xVector normalize() {
        double sum = sum();

        if (DoubleComparator.DEFAULT.isZero(sum))
            throw new MorpheusException("Cannot normalize a vector with zero element sum.");

        return divideInPlace(sum);
    }

    /**
     * Replaces missing values with a default value.
     *
     * @param value the replacement value.
     *
     * @return this vector, for operator chaining.
     */
    default D3xVector replaceNaN(double value) {
        for (int index = 0; index < length(); ++index) {
            if (Double.isNaN(get(index)))
                set(index, value);
        }

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
    default D3xVector times(double scalar) {
        return copy().multiplyInPlace(scalar);
    }

    /**
     * Multiplies each element of this vector by a scalar factor and modifies
     * the elements of this vector in place.
     *
     * @param scalar the scalar factor.
     *
     * @return this vector, for operator chaining.
     */
    default D3xVector multiplyInPlace(double scalar) {
        for (int index = 0; index < length(); ++index)
            set(index, scalar * get(index));

        return this;
    }

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
     * Concatenates a sequence of vectors and returns the result in a new vector.
     *
     * @param vectors the vectors to concatenate.
     *
     * @return a new vector containing the concatenation of the input vectors.
     */
    static D3xVector concat(D3xVector... vectors) {
        int length = 0;

        for (D3xVector vector : vectors)
            length += vector.length();

        D3xVector result = D3xVector.dense(length);

        int vectorIndex = 0;
        int concatIndex = 0;

        while (vectorIndex < vectors.length) {
            result.setSubVector(concatIndex, vectors[vectorIndex]);
            concatIndex += vectors[vectorIndex].length();
            vectorIndex += 1;
        }

        return result;
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
     * Creates a new vector by copying values from a vector view.
     *
     * @param view the view to be copied.
     *
     * @return a new vector containing a copy of the specified vector.
     */
    static D3xVector copyOf(D3xVectorView view) {
        return ApacheVector.copyOf(view.toArray());
    }

    /**
     * Creates a new vector by copying values from a list.
     *
     * @param values the list to be copied.
     *
     * @return a new vector containing a copy of the values in the list.
     */
    static D3xVector copyOf(@NonNull List<Double> values) {
        D3xVector vector = dense(values.size());

        for (int index = 0; index < values.size(); ++index)
            vector.set(index, values.get(index));

        return vector;
    }

    /**
     * Creates a new vector by copying values from a DoubleSeries.
     *
     * @param series       the series to be copied.
     * @param keys         the keys of the values to be copied.
     * @param defaultValue the default value to use for missing keys.
     *
     * @return a new vector containing a copy of the values in the
     * specified series.
     */
    static <K> D3xVector copyOf(DoubleSeries<K> series, List<K> keys, double defaultValue) {
        D3xVector vector = dense(keys.size());

        for (int index = 0; index < keys.size(); ++index) {
            K key = keys.get(index);

            if (series.contains(key))
                vector.set(index, series.getDouble(key));
            else
                vector.set(index, defaultValue);
        }

        return vector;
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
     * Creates a vector with every element equal to {@code 1.0}.
     *
     * @param length the length of the vector.
     *
     * @return a ones-vector with the specified length.
     */
    static D3xVector ones(int length) {
        return rep(1.0, length);
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
            throw new MorpheusException("Length [%d] is negative.", length);
    }

    /**
     * Creates a mutable vector view over a bare array (a shallow copy).
     * Changes to the returned vector will be reflected in the original
     * array, and changes to the array will be reflected in the vector.
     *
     * @param values the values to be viewed.
     *
     * @return a mutable vector view over the specified array.
     */
    static D3xVector wrap(double... values) {
        return ApacheVector.wrap(values);
    }

    /**
     * Presents a Double list as a D3xVector. Changes to the returned
     * vector will be reflected in the original list, and changes to
     * the list will be reflected in the returned vector.
     *
     * @param list the list to present.
     *
     * @return a vector adapter for the specified list.
     */
    static D3xVector wrap(List<Double> list) {
        return new ListVector(list);
    }

    /**
     * Presents a row or column of a numeric data frame as a D3xVector.
     * Changes to the returned vector will be reflected in the original
     * data frame, and changes to the data frame will be reflected in
     * the returned vector.
     *
     * @param vector the data frame row or column to present.
     *
     * @return a vector adapter for the specified data frame vector.
     *
     * @throws RuntimeException unless the row index refers to a
     * numeric row in the data frame.
     */
    static D3xVector wrap(DataFrameVector<?,?,?,?,?> vector) {
        return FrameVector.wrap(vector);
    }

    /**
     * Creates a vector with every element equal to {@code 0.0}.
     *
     * @param length the length of the vector.
     *
     * @return a zeros-vector with the specified length.
     */
    static D3xVector zeros(int length) {
        return rep(0.0, length);
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
        return equalsView(that, comparator);
    }

    /**
     * Identifies empty vectors.
     *
     * @return {@code true} iff this vector has zero length.
     */
    default boolean isEmpty() {
        return length() == 0;
    }
}
