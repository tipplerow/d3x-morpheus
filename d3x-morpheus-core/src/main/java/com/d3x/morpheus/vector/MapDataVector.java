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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import lombok.NonNull;

/**
 * Creates a DataVector backed by a map.
 *
 * @author Scott Shaffer
 */
final class MapDataVector<K> implements DataVector<K> {
    @NonNull private final Map<K, Double> map;

    MapDataVector() {
        this(new HashMap<>());
    }

    MapDataVector(Map<K, Double> map) {
        this.map = map;
    }

    @Override
    public Set<K> collectKeys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public boolean containsElement(K elementKey) {
        return map.containsKey(elementKey);
    }

    @Override
    public double getElement(K elementKey, double defaultValue) {
        return map.getOrDefault(elementKey, defaultValue);
    }

    @Override
    public int length() {
        return map.size();
    }

    @Override
    public void setElement(K key, double value) {
        map.put(key, value);
    }

    @Override
    public Stream<K> streamKeys() {
        return map.keySet().stream();
    }

    @Override
    public DoubleStream streamValues() {
        return map.keySet().stream().mapToDouble(map::get);
    }
}
