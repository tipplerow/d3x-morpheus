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
