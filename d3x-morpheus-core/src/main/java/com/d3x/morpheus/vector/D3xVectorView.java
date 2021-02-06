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

import com.d3x.morpheus.util.DoubleComparator;

/**
 * Provides a read-only view of {@code double} values that are accessed
 * by ordinal index (location).
 *
 * @author Scott Shaffer
 */
public interface D3xVectorView {
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
}
