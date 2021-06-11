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

import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Provides a singleton implementation for empty data vectors.
 * by a fixed set of keys.
 *
 * @param <K> the runtime key type.
 *
 * @author Scott Shaffer
 */
final class EmptyVectorView<K> implements DataVectorView<K> {
    private EmptyVectorView() {
    }

    static final EmptyVectorView<?> INSTANCE = new EmptyVectorView<>();

    @Override
    public boolean containsElement(K key) {
        return false;
    }

    @Override
    public double getElement(K elementKey, double defaultValue) {
        return defaultValue;
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public Stream<K> streamKeys() {
        return Stream.empty();
    }

    @Override
    public DoubleStream streamValues() {
        return DoubleStream.empty();
    }
}