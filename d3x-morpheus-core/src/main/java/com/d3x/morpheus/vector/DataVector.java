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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface DataVector<K> extends DataVectorView<K> {
    /**
     * Assigns a vector element.
     *
     * @param key   the key of the element to assign.
     * @param value the value to assign.
     */
    void setElement(K key, double value);

    /**
     * Assigns a vector element.
     *
     * @param element the element to assign.
     */
    default void setElement(DataVectorElement<K> element) {
        setElement(element.getKey(), element.getValue());
    }

    /**
     * Assigns all elements from a collection.
     *
     * @param elements the elements to assign.
     */
    default void setElements(Collection<DataVectorElement<K>> elements) {
        for (DataVectorElement<K> element : elements)
            setElement(element);
    }

    /**
     * Assigns all elements from a stream.
     *
     * @param elements the elements to assign.
     */
    default void setElements(Stream<DataVectorElement<K>> elements) {
        elements.forEach(this::setElement);
    }

    /**
     * Assigns all elements contained in another DataVector.
     *
     * @param vector the vector to assign.
     */
    default void setElements(DataVectorView<K> vector) {
        setElements(vector.streamElements());
    }

    /**
     * Creates a new data vector by copying the elements of an existing view.
     *
     * @param view the data vector view to copy.
     *
     * @return a new data vector with the same elements as the input view.
     */
    static <K> DataVector<K> copy(DataVectorView<K> view) {
        DataVector<K> vector = create();

        for (DataVectorElement<K> element : view.collectElements())
            vector.setElement(element);

        return vector;
    }

    /**
     * Collects the vector elements in a stream into a new DataVector.
     *
     * @param elements the elements to collect.
     *
     * @return a new DataVector containing the elements in the stream.
     */
    static <K> DataVector<K> collect(Stream<DataVectorElement<K>> elements) {
        DataVector<K> vector = create();
        elements.forEach(vector::setElement);
        return vector;
    }

    /**
     * Creates a new, empty data vector.
     *
     * @param <K> the runtime key type.
     *
     * @return a new, empty data vector with the desired key type.
     */
    static <K> DataVector<K> create() {
        return new MapDataVector<>(new HashMap<>());
    }

    /**
     * Returns a DataVector backed by a Double map; changes to the map
     * will be reflected in the returned vector.
     *
     * @param map the underlying Double map.
     *
     * @return a DataVector backed by specified Double map.
     */
    static <K> DataVector<K> of(Map<K, Double> map) {
        return new MapDataVector<>(map);
    }
}
