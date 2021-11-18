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
import java.util.PrimitiveIterator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;

import com.d3x.morpheus.stats.Statistic1;
import com.d3x.morpheus.stats.Sum;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;

/**
 * Provides a read-only view of {@code double} values that are accessed
 * by ordinal index (location).
 *
 * @author Scott Shaffer
 */
public interface D3xVectorView extends Iterable<Double> {
    /**
     * Returns the length of the vector.
     *
     * @return the length of the vector.
     */
    int length();

    /**
     * Returns the value of an element at a given location.
     *
     * @param index the index of the element to return.
     * @return the value of the element at the specified location.
     * @throws RuntimeException if the index is out of bounds.
     */
    double get(int index);

    /**
     * Returns a read-only iterator over this vector view.
     * @return a read-only iterator over this vector view.
     */
    @Override default PrimitiveIterator.OfDouble iterator() {
        return new VectorIterator(this);
    }

    /**
     * Returns a vector view over a bare array.
     *
     * @param array the array to wrap in a view.
     *
     * @return a vector view over the given array.
     */
    static D3xVectorView of(double... array) {
        return new ArrayView(array);
    }

    /**
     * Returns a vector view over a Double list.
     *
     * @param list the list to wrap in a view.
     *
     * @return a vector view over the given list.
     */
    static D3xVectorView of(List<Double> list) {
        return new ListView(list);
    }

    /**
     * Tests whether all elements in this vector satisfy a predicate.
     *
     * @param predicate the predicate to evaluate.
     *
     * @return {@code true} iff every element in this vector satisfies
     * the specified predicate.
     */
    default boolean all(DoublePredicate predicate) {
        for (int index = 0; index < length(); ++index)
            if (!predicate.test(get(index)))
                return false;

        return true;
    }

    /**
     * Tests whether any element in this vector satisfies a predicate.
     *
     * @param predicate the predicate to evaluate.
     *
     * @return {@code true} iff at least one element in this vector
     * satisfies the specified predicate.
     */
    default boolean any(DoublePredicate predicate) {
        for (int index = 0; index < length(); ++index)
            if (predicate.test(get(index)))
                return true;

        return false;
    }

    /**
     * Applies a unary operator to every element in this vector and
     * returns the result in a new vector.
     *
     * @param operator the operator to apply.
     *
     * @return a new vector with element {@code k} equal to the result
     * of applying the operator to element {@code k} of this vector.
     */
    default D3xVector apply(DoubleUnaryOperator operator) {
        var result = D3xVector.dense(length());

        for (int index = 0; index < length(); ++index)
            result.set(index, operator.applyAsDouble(get(index)));

        return result;
    }

    /**
     * Computes a statistic over the values in this view.
     *
     * @param statistic the statistic to compute.
     *
     * @return the specified statistic evaluated over the elements in this view.
     */
    default double compute(Statistic1 statistic) {
        return Statistic1.compute(statistic, this);
    }

    /**
     * Computes the cumulative product for this view.
     *
     * @return a new vector containing the cumulative product of this view:
     * element {@code k} of the result is the product of the elements {@code
     * 0, 1, ..., k}.
     */
    default D3xVector cumprod() {
        var result = D3xVector.dense(length());

        if (length() > 0)
            result.set(0, get(0));

        for (int index = 1; index < length(); ++index)
            result.set(index, result.get(index - 1) * get(index));

        return result;
    }

    /**
     * Computes the cumulative sum for this view.
     *
     * @return a new vector containing the cumulative sum of this view:
     * element {@code k} of the result is the sum of the elements {@code
     * 0, 1, ..., k}.
     */
    default D3xVector cumsum() {
        var result = D3xVector.dense(length());

        if (length() > 0)
            result.set(0, get(0));

        for (int index = 1; index < length(); ++index)
            result.set(index, result.get(index - 1) + get(index));

        return result;
    }

    /**
     * Computes lagged differences for this view.
     *
     * @return a new vector with the same length as this vector where the
     * first element is NaN and all other elements {@code k} are equal to
     * {@code this.get(k) - this.get(k - 1)}.
     */
    default D3xVector diff() {
        return diff(1);
    }

    /**
     * Computes lagged differences for this view.
     *
     * @param lag the lag index, in the range {@code [1, length() - 1].}
     *
     * @return a new vector with the same length as this vector where the
     * first {@code lag} elements are NaN and all other elements {@code k}
     * are equal to {@code this.get(k) - this.get(k - lag)}.
     */
    default D3xVector diff(int lag) {
        if (lag < 1)
            throw new IllegalArgumentException("Lag must be positive.");

        if (lag >= length())
            throw new IllegalArgumentException("Lag must be smaller than the view length.");

        var result = D3xVector.dense(length());

        for (int index = 0; index < lag; ++index)
            result.set(index, Double.NaN);

        for (int index = lag; index < length(); ++index)
            result.set(index, get(index) - get(index - lag));

        return result;
    }

    /**
     * Determines whether the entries in this view are equal to those in a bare
     * array <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param values the values to test for equality.
     *
     * @return {@code true} iff the input array has the same length as this view
     * and each value matches the corresponding entry in this view within the
     * tolerance of the default DoubleComparator.
     */
    default boolean equalsArray(double... values) {
        return equalsArray(values, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this view are equal to those in a bare
     * array within the tolerance of a given DoubleComparator.
     *
     * @param values     the values to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input array has the same length as this view
     * and each value matches the corresponding entry in this view within the
     * tolerance of the specified comparator.
     */
    default boolean equalsArray(double[] values, DoubleComparator comparator) {
        return equalsView(of(values), comparator);
    }

    /**
     * Determines whether the entries in this view are equal to those in another
     * view <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param that the view to test for equality.
     *
     * @return {@code true} iff the input view has the same length as this view and
     * each value matches the corresponding entry in this view within the tolerance
     * of the default DoubleComparator.
     */
    default boolean equalsView(D3xVectorView that) {
        return equalsView(that, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the entries in this view are equal to those in another
     * view within the tolerance of a given DoubleComparator.
     *
     * @param that       the vector to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input view has the same length as this view and
     * each value matches the corresponding entry in this view within the tolerance
     * of the specified comparator.
     */
    default boolean equalsView(D3xVectorView that, DoubleComparator comparator) {
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
     * Determines if this view has the same length as another view.
     *
     * @param that the view to compare to this.
     *
     * @return {@code true} iff the input view has the same length as this view.
     */
    default boolean isCongruent(D3xVectorView that) {
        return this.length() == that.length();
    }

    /**
     * Returns a view of this vector in reverse order.
     * @return a view of this vector in reverse order.
     */
    default D3xVectorView reverse() {
        return new ReverseView(this);
    }

    /**
     * Returns a view of a slice of this vector.
     *
     * @param start  the index of the first element in the subvector.
     * @param length the length of the subvector.
     *
     * @return a view of the elements {@code [start, start + length)}
     * of this vector.
     *
     * @throws RuntimeException unless the input arguments specify a
     * valid subvector of this vector.
     */
    default D3xVectorView subVectorView(int start, int length) {
        return new SubVectorView(this, start, length);
    }

    /**
     * Computes the sum of all elements in this vector.
     *
     * @return the sum of all elements in this vector.
     */
    default double sum() {
        return compute(new Sum());
    }

    /**
     * Copies the elements of this view into a new array, which does not contain
     * a reference to the underlying data in this view.  Changes to the returned
     * array will not be reflected in this view.
     *
     * @return the elements of this view in a new array.
     */
    default double[] toArray() {
        double[] array = new double[length()];

        for (int index = 0; index < array.length; ++index)
            array[index] = get(index);

        return array;
    }

    /**
     * Ensures that this view has the same length as another view.
     *
     * @param that the view to validate.
     *
     * @throws RuntimeException unless the specified view has the same length as
     * this view.
     */
    default void validateCongruent(D3xVectorView that) {
        if (!isCongruent(that))
            throw new MorpheusException("Vector length mismatch: [%d != %d].", this.length(), that.length());
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
            throw new MorpheusException("Index [%d] is out of bounds [0, %d).", index, length());
    }

    /**
     * Applies a binary operator to two congruent vectors element by element.
     *
     * @param vector1  the first vector.
     * @param vector2  the second vector.
     * @param operator the operator to apply.
     *
     * @return a new vector with element {@code k} equal to the result of
     * applying the operator to elements {@code v1[k]} and {@code v2[k]}.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    static D3xVector applyEBE(D3xVectorView vector1, D3xVectorView vector2, DoubleBinaryOperator operator) {
        validateCongruent(vector1, vector2);

        var length = vector1.length();
        var result = D3xVector.dense(length);

        for (int index = 0; index < length; ++index)
            result.set(index, operator.applyAsDouble(vector1.get(index), vector2.get(index)));

        return result;
    }

    /**
     * Divides two congruent vectors element by element.
     *
     * @param v1 the first vector.
     * @param v2 the second vector.
     *
     * @return a new vector {@code u} with {@code u[k] = v1[k] * v2[k]}.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    static D3xVector divideEBE(D3xVectorView v1, D3xVectorView v2) {
        return applyEBE(v1, v2, (x, y) -> x / y);
    }

    /**
     * Multiplies two congruent vectors element by element.
     *
     * @param v1 the first vector.
     * @param v2 the second vector.
     *
     * @return a new vector {@code u} with {@code u[k] = v1[k] * v2[k]}.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    static D3xVector multiplyEBE(D3xVectorView v1, D3xVectorView v2) {
        return applyEBE(v1, v2, (x, y) -> x * y);
    }

    /**
     * Ensures that two vector views have the same length.
     *
     * @param v1 the first view to validate.
     * @param v2 the second view to validate.
     *
     * @throws RuntimeException unless the specified views have the
     * same length.
     */
    static void validateCongruent(D3xVectorView v1, D3xVectorView v2) {
        v1.validateCongruent(v2);
    }
}
