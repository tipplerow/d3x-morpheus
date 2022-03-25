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
package com.d3x.morpheus.collect;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An in-memory cache that removes the least recently used (LRU) item
 * after reaching a given size limit.
 *
 * @author Scott Shaffer
 */
public final class LRUCache<K, V> extends CacheBase<K, V> {
    /**
     * Creates an empty LRU cache with a fixed capacity.
     *
     * @param capacity the maximum number of items to be held in the cache.
     *
     * @throws RuntimeException unless the capacity is positive.
     */
    public LRUCache(int capacity) {
        super(new CacheMap<>(capacity));
    }

    /**
     * Creates an empty LRU cache with a fixed capacity.
     *
     * @param <K>      the runtime key type.
     * @param <V>      the runtime value type.
     * @param capacity the maximum number of items to be held in the cache.
     *
     * @return a new empty LRU cache with the specified capacity.
     *
     * @throws RuntimeException unless the capacity is positive.
     */
    public static <K, V> LRUCache<K, V> create(int capacity) {
        return new LRUCache<>(capacity);
    }

    /**
     * Returns the fixed capacity of this cache.
     * @return the fixed capacity of this cache.
     */
    public int getCapacity() {
        return ((CacheMap<K, V>) map).capacity;
    }

    private static final class CacheMap<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        private CacheMap(int capacity) {
            super(capacity, 1.0f, true);

            if (capacity > 0)
                this.capacity = capacity;
            else
                throw new IllegalArgumentException("Capacity must be positive.");
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            // Called by put() and putAll() after inserting a new element...
            return size() > capacity;
        }
    }
}
