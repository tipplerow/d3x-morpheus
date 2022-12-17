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
import com.d3x.morpheus.util.IntComparator;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.impl.factory.primitive.IntIntMaps;

/**
 * An Index implementation designed to efficiently store integer values
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
class IndexOfInts extends IndexBase<Integer> {

    private static final long serialVersionUID = 1L;

    private MutableIntIntMap indexMap;

    /**
     * Constructor
     *
     * @param initialSize the initial size for this index
     */
    IndexOfInts(int initialSize) {
        super(Array.of(Integer.class, initialSize));
        this.indexMap = IntIntMaps.mutable.withInitialCapacity(initialSize);
    }

    /**
     * Constructor
     *
     * @param iterable the keys for index
     */
    IndexOfInts(Iterable<Integer> iterable) {
        super(iterable);
        this.indexMap = IntIntMaps.mutable.withInitialCapacity(keyArray().length());
        this.keyArray().sequential().forEachValue(v -> {
            final int index = v.index();
            final int key = v.getInt();
            final int size = indexMap.size();
            indexMap.put(key, index);
            if (indexMap.size() <= size) {
                throw new IndexException("Cannot have duplicate keys in index: " + v.getValue());
            }
        });
    }

    /**
     * Constructor
     *
     * @param iterable the keys for index
     * @param parent   the parent index to initialize from
     */
    private IndexOfInts(Iterable<Integer> iterable, IndexOfInts parent) {
        super(iterable, parent);
        this.indexMap = IntIntMaps.mutable.withInitialCapacity(keyArray().length());
        this.keyArray().sequential().forEachValue(v -> {
            final int key = v.getInt();
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
    public final Index<Integer> filter(Iterable<Integer> keys) {
        return new IndexOfInts(keys, isFilter() ? (IndexOfInts) parent() : this);
    }

    @Override
    public Index<Integer> filter(Predicate<Integer> predicate) {
        final int count = size();
        final ArrayBuilder<Integer> builder = ArrayBuilder.of(count / 2, Integer.class);
        for (int i = 0; i < count; ++i) {
            final int value = keyArray().getInt(i);
            if (predicate.test(value)) {
                builder.appendInt(value);
            }
        }
        final Array<Integer> filter = builder.toArray();
        return new IndexOfInts(filter, isFilter() ? (IndexOfInts) parent() : this);
    }

    @Override
    public final boolean add(Integer key) {
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
    public int addAll(Iterable<Integer> keys, boolean ignoreDuplicates) {
        if (isFilter()) {
            throw new IndexException("Cannot add keys to an filter on another index");
        } else {
            var count = new int[1];
            keys.forEach(key -> {
                final int keyAsInt = key;
                if (!indexMap.containsKey(keyAsInt)) {
                    final int index = indexMap.size();
                    this.ensureCapacity(index + 1);
                    this.keyArray().setValue(index, keyAsInt);
                    final int size = indexMap.size();
                    indexMap.put(keyAsInt, index);
                    if (!ignoreDuplicates && indexMap.size() <= size) {
                        throw new IndexException("Attempt to add duplicate key to index: " + key);
                    }
                    count[0]++;
                }
            });
            return count[0];
        }
    }

    @Override
    public final Index<Integer> copy(boolean deep) {
        try {
            var clone = (IndexOfInts)super.copy(deep);
            if (deep) clone.indexMap = IntIntMaps.mutable.withAll(indexMap);
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
    public final int getCoordinate(Integer key) {
        return indexMap.getIfAbsent(key, -1);
    }

    @Override
    public final boolean contains(Integer key) {
        return indexMap.containsKey(key);
    }

    @Override
    public final int replace(Integer existing, Integer replacement) {
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

    @Override
    public final void forEachEntry(IndexConsumer<Integer> consumer) {
        final int size = size();
        for (int i = 0; i < size; ++i) {
            var key = keyArray().getValue(i);
            final int index = indexMap.getIfAbsent(key, -1);
            consumer.accept(key, index);
        }
    }


    @Override
    public final Index<Integer> resetOrder() {
        final Array<Integer> keys = keyArray();
        this.indexMap.forEachKeyValue((key, index) -> {
            keys.setInt(index, key);
        });
        return this;
    }


    @Override
    public final void sort(boolean parallel, IntComparator comparator) {
        super.sort(parallel, comparator);
        if (comparator == null) {
            final Array<Integer> keys = keyArray();
            this.indexMap.forEachKeyValue((key, index) -> {
                keys.setInt(index, key);
            });
        }
   }
}
