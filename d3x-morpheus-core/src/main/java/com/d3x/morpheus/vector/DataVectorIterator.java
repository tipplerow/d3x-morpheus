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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

import lombok.NonNull;

/**
 * Provides read-only iteration over a data vector view.
 *
 * @param <K> the runtime key type.
 *
 * @author Scott Shaffer
 */
final class DataVectorIterator<K> implements PrimitiveIterator.OfDouble {
    @NonNull private final Iterator<K> keyIterator;
    @NonNull private final DataVectorView<K> vectorView;

    DataVectorIterator(DataVectorView<K> vectorView) {
        this.vectorView = vectorView;
        this.keyIterator = vectorView.collectKeys().iterator();
    }

    @Override
    public boolean hasNext() {
        return keyIterator.hasNext();
    }

    @Override
    public double nextDouble() {
        if (hasNext())
            return vectorView.getElement(keyIterator.next());
        else
            throw new NoSuchElementException();
    }
}
