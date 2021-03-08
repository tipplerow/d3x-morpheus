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
import java.util.function.DoublePredicate;

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
        return equalsView(of(values));
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
}
