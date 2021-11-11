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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.d3x.morpheus.concurrent.ConcurrentObject;

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
public class WormCache<K, V> extends ConcurrentObject {
    /**
     * The underlying map storage.
     */
    @NonNull
    protected final Map<K, V> map;

    /**
     * Creates an empty cache.
     */
    public WormCache() {
        this.map = new HashMap<>();
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
     * Identifies keys contained in this cache.
     *
     * @param key the key of interest.
     *
     * @return {@code true} iff this cache contains the given key.
     */
    public boolean containsKey(@NonNull K key) {
        return read(map::containsKey, key);
    }

    /**
     * Retrieves a value from this cache.
     *
     * @param key the key associated with the value.
     *
     * @return the value associated with the specified key, or
     * {@code null} if there is no match.
     */
    public V get(@NonNull K key) {
        return read(map::get, key);
    }

    /**
     * Retrieves an existing value from this cache or assigns a default.
     *
     * @param targetKey    the key associated with the value.
     * @param defaultValue the default value to assign and return if
     *                     there is no match for the key.
     *
     * @return the value associated with the specified key, or the
     * default value if there is no match.
     */
    public V getOrAssign(@NonNull K targetKey, @NonNull V defaultValue) {
        V result = get(targetKey);

        if (result != null) {
            return result;
        }
        else {
            write(this::putIfAbsent, targetKey, defaultValue);
            // Another thread might have acquired the write lock and assigned
            // the same key during the interval when this thread did not hold
            // the write lock, so we must retrieve the value associated with
            // the key again...
            return get(targetKey);
        }
    }

    private void putIfAbsent(@NonNull K key, @NonNull V value) {
        // Another thread might have acquired the write lock and assigned
        // the same key during the interval when this thread did not hold
        // the write lock.  If so, we cannot overwrite...
        if (!map.containsKey(key))
            map.put(key, value);
    }

    /**
     * Retrieves an existing value from this cache or computes and
     * assigns a value.
     *
     * @param key     the key associated with the value.
     * @param compute the function to compute missing values.
     *
     * @return the value associated with the specified keys, or the
     * computed value if there is no match.
     */
    public V getOrCompute(@NonNull K key, @NonNull Function<K, V> compute) {
        V result = get(key);

        if (result != null) {
            return result;
        }
        else {
            var value = compute.apply(key);
            write(this::putIfAbsent, key, value);
            // Another thread might have acquired the write lock and assigned
            // the same key during the interval when this thread did not hold
            // the write lock, so we must retrieve the value associated with
            // the key again...
            return get(key);
        }
    }

    /**
     * Retrieves a value from this cache or throws an exception.
     *
     * @param key the row key associated with the value.
     *
     * @return the value associated with the specified key.
     *
     * @throws NoSuchElementException unless a value has been
     * associated with the specified key.
     */
    public V getOrThrow(@NonNull K key) {
        V result = get(key);

        if (result != null)
            return result;
        else
            throw new NoSuchElementException(String.format("No value for key [%s].", key));
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
     * @return {@code null}, because there cannot be a previous value in
     * the map.
     *
     * @throws IllegalStateException if this cache already contains the key.
     */
    public V put(@NonNull K key, @NonNull V value) {
        write(this::putUnique, key, value);
        return null;
    }

    private void putUnique(@NonNull K key, @NonNull V value) {
        if (map.containsKey(key))
            throw new IllegalStateException(String.format("Key [%s] has already been assigned.", key));
        else
            map.put(key, value);
    }
}
