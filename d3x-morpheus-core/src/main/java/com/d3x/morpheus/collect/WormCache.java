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

import java.util.HashMap;

import lombok.NonNull;

/**
 * An in-memory cache that implements a write-once, read-many (WORM)
 * protocol for assigning values.  The value associated with a key
 * may be assigned at most once.  A second attempt to assign a value
 * to the same key will trigger a runtime exception.
 *
 * <p>This implementation is synchronized and thread-safe.</p>
 *
 * @param <K> the runtime key type.
 * @param <V> the runtime value type.
 *
 * @author Scott Shaffer
 */
public class WormCache<K, V> extends CacheBase<K, V> {
    /**
     * Creates an empty cache.
     */
    public WormCache() {
        super(new HashMap<>());
    }

    /**
     * Creates an empty cache.
     *
     * @param <K> the runtime key type.
     * @param <V> the runtime value type.
     *
     * @return a new empty cache using a HashMap for the underlying storage.
     */
    public static <K, V> WormCache<K, V> create() {
        return new WormCache<>();
    }

    /**
     * Permanently assigns a key/value pair.
     *
     * <p>This method may be called at most once with a given key; calling
     * it a second time with the same key will trigger an exception.</p>
     *
     * @param key   the key to associate with the value.
     * @param value the value to associate with the key.
     *
     * @throws IllegalStateException if this cache already contains the key.
     */
    @Override
    public void put(@NonNull K key, @NonNull V value) {
        write(this::putUnique, key, value);
    }

    private void putUnique(@NonNull K key, @NonNull V value) {
        if (map.containsKey(key))
            throw new IllegalStateException(String.format("Key [%s] has already been assigned.", key));
        else
            map.put(key, value);
    }
}
