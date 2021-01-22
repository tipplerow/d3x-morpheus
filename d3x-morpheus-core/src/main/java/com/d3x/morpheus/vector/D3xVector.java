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

import com.d3x.core.lang.D3xException;
import com.d3x.morpheus.util.DoubleComparator;

/**
 * Represents a fixed-length vector of {@code double} values.
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
     * Creates a new mutable vector by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new mutable vector containing a copy of the specified array.
     */
    static D3xVector copyOf(double... values) {
        return ApacheDenseVector.copyOf(values);
    }

    /**
     * Creates an <em>immutable</em> vector view over a bare array.
     *
     * @param values the values to be viewed.
     *
     * @return an <em>immutable</em> vector view over the specified array.
     */
    static D3xVector of(double... values) {
        return ApacheDenseVector.of(values);
    }

    /**
     * Creates a new mutable vector of a fixed length with all elements
     * initialized to zero.
     *
     * @param length the fixed length of the vector.
     *
     * @return a new mutable vector of the specified length with all elements
     * initialized to zero.
     *
     * @throws RuntimeException if the length is negative.
     */
    static D3xVector ofLength(int length) {
        return ApacheDenseVector.ofLength(length);
    }

    /**
     * Like the {@code R} function {@code rep(x, n)}, creates a new mutable vector
     * containing the value {@code x} replicated {@code n} times.
     *
     * @param x the value to replicate.
     * @param n the number of times to replicate.
     *
     * @return a new mutable vector of length {@code n} with each element assigned
     * the value {@code x}.
     *
     * @throws RuntimeException if {@code n < 0}.
     */
    static D3xVector rep(double x, int n) {
        D3xVector result = ofLength(n);

        for (int index = 0; index < n; ++index)
            result.set(index, x);

        return result;
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
     * Creates a mutable vector view over a bare array.
     *
     * @param values the values to be viewed.
     *
     * @return a mutable vector view over the specified array.
     */
    static D3xVector wrap(double[] values) {
        return ApacheDenseVector.wrap(values);
    }

    /**
     * Returns a deep copy of this vector.
     *
     * <p>Note that this method cannot be named simply {@code copy()}, because
     * that would duplicate the same method from the Apache RealVector class.</p>
     *
     * @return a deep copy of this vector.
     */
    default D3xVector copyOf() {
        D3xVector copy = like();

        for (int index = 0; index < length(); ++index)
            copy.set(index, this.get(index));

        return copy;
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
