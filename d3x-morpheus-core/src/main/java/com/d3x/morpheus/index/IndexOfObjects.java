/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.index;

import java.util.function.Predicate;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.impl.factory.primitive.ObjectIntMaps;

/**
 * An Index implementation designed to store any object type.
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
class IndexOfObjects<K> extends IndexBase<K> {

    private static final long serialVersionUID = 1L;

    private MutableObjectIntMap<K> indexMap;

    /**
     * Constructor
     * @param type      the element type
     * @param initialSize   the initial size for this index
     */
    IndexOfObjects(Class<K> type, int initialSize) {
        super(Array.of(type, initialSize));
        this.indexMap = ObjectIntMaps.mutable.withInitialCapacity(initialSize);
    }

    /**
     * Constructor
     * @param iterable      the keys for index
     */
    IndexOfObjects(Iterable<K> iterable) {
        super(iterable);
        this.indexMap = ObjectIntMaps.mutable.withInitialCapacity(keyArray().length());
        this.keyArray().sequential().forEachValue(v -> {
            final int index = v.index();
            final K key = v.getValue();
            final int size = indexMap.size();
            indexMap.put(key, index);
            if (indexMap.size() <= size) {
                throw new IndexException("Cannot have duplicate keys in index: " + v.getValue());
            }
        });
    }

    /**
     * Constructor
     * @param iterable  the keys for index
     * @param parent    the parent index to initialize from
     */
    private IndexOfObjects(Iterable<K> iterable, IndexOfObjects<K> parent) {
        super(iterable, parent);
        this.indexMap = ObjectIntMaps.mutable.withInitialCapacity(keyArray().length());
        this.keyArray().sequential().forEachValue(v -> {
            final K key = v.getValue();
            final int index = parent.indexMap.getIfAbsent(key, -1);
            if (index < 0) throw new IndexException("No match for key: " + v.getValue());
            final int size = indexMap.size();
            indexMap.put(key, index);
            if (indexMap.size() <= size) {
                throw new IndexException("Cannot have duplicate keys in index: " + v.getValue());
            }
        });
    }

    @Override()
    public final Index<K> filter(Iterable<K> keys) {
        return new IndexOfObjects<>(keys, isFilter() ? (IndexOfObjects<K>)parent() : this);
    }

    @Override
    public Index<K> filter(Predicate<K> predicate) {
        final int count = size();
        final Class<K> type = type();
        final ArrayBuilder<K> builder = ArrayBuilder.of(count / 2, type);
        for (int i=0; i<count; ++i) {
            final K value = keyArray().getValue(i);
            if (predicate.test(value)) {
                builder.append(value);
            }
        }
        final Array<K> filter = builder.toArray();
        return new IndexOfObjects<>(filter, isFilter() ? (IndexOfObjects<K>)parent() : this);
    }

    @Override
    public final boolean add(K key) {
        if (isFilter()) {
            throw new IndexException("Cannot add keys to an filter on another index");
        } else {
            if (indexMap.containsKey(key)) {
                return false;
            } else {
                final int index = indexMap.size();
                this.ensureCapacity(index + 1);
                this.keyArray().setValue(index, key);
                this.indexMap.put(key, index);
                return true;
            }
        }
    }

    @Override
    public int addAll(Iterable<K> keys, boolean ignoreDuplicates) {
        if (isFilter()) {
            throw new IndexException("Cannot add keys to an filter on another index");
        } else {
            var count = new int[1];
            keys.forEach(key -> {
                if (!indexMap.containsKey(key)) {
                    final int index = indexMap.size();
                    this.ensureCapacity(index + 1);
                    this.keyArray().setValue(index, key);
                    final int size = indexMap.size();
                    indexMap.put(key, index);
                    if (!ignoreDuplicates && indexMap.size() <= size) {
                        throw new IndexException("Attempt to add duplicate key to index: " + key);
                    }
                    ++count[0];
                }
            });
            return count[0];
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Index<K> copy(boolean deep) {
        try {
            final IndexOfObjects<K> clone = (IndexOfObjects<K>)super.copy(deep);
            if (deep) clone.indexMap = ObjectIntMaps.mutable.withAll(indexMap);
            return clone;
        } catch (Exception ex) {
            throw new IndexException("Failed to clone index", ex);
        }
    }

    @Override
    public final int size() {
        return indexMap.size();
    }

    @Override
    public final int getCoordinate(K key) {
        return indexMap.getIfAbsent(key, -1);
    }

    @Override
    public final boolean contains(K key) {
        return indexMap.containsKey(key);
    }

    @Override
    public final int replace(K existing, K replacement) {
        final int index = indexMap.removeKeyIfAbsent(existing, -1);
        if (index == -1) {
            throw new IndexException("No match key for " + existing);
        } else {
            if (indexMap.containsKey(replacement)) {
                throw new IndexException("The replacement key already exists in index " + replacement);
            } else {
                final int ordinal = getOrdinalAt(index);
                this.indexMap.put(replacement, index);
                this.keyArray().setValue(ordinal, replacement);
                return index;
            }
        }
    }

    @Override()
    public final void forEachEntry(IndexConsumer<K> consumer) {
        final int size = size();
        for (int i=0; i<size; ++i) {
            final K key = keyArray().getValue(i);
            final int index = indexMap.getIfAbsent(key, -1);
            consumer.accept(key, index);
        }
    }

}
