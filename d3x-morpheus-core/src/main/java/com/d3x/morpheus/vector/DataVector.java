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

import java.util.Map;

public interface DataVector<K> extends DataVectorView<K> {
    /**
     * Assigns a vector element.
     *
     * @param key   the key of the element to assign.
     * @param value the value to assign.
     */
    void setElement(K key, double value);

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
