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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import com.d3x.morpheus.util.MorpheusException;

import lombok.NonNull;

/**
 * A map that implements a write-once, read-many (WORM) protocol for
 * assigning cell values. The value associated with a row/column key
 * pair may be assigned at most once. A second attempt to assign the
 * the same cell will trigger a runtime exception.
 *
 * @param <K> the runtime key type.
 * @param <V> the runtime value type.
 *
 * @author Scott Shaffer
 */
public class WormMap<K, V> extends AbstractMap<K, V> {
    /**
     * The underlying map storage.
     */
    @NonNull
    protected Map<K, V> map;

    /**
     * Creates an empty WORM map using a HashMap for the underlying storage.
     */
    public WormMap() {
        this(new HashMap<>());
    }

    /**
     * Creates a new WORM map using another map for the underlying storage;
     * the contents of the input map become the contents of the WORM map.
     *
     * @param map the underlying map.
     */
    public WormMap(@NonNull Map<K, V> map) {
        this.map = map;
    }

    /**
     * Creates an empty WORM map using a HashMap for the underlying storage.
     *
     * @param <K> the runtime key type.
     * @param <V> the runtime value type.
     *
     * @return a new empty WORM map using a HashMap for the underlying storage.
     */
    public static <K, V> WormMap<K, V> hash() {
        return new WormMap<>(new HashMap<>());
    }

    /**
     * Creates an empty WORM map using a TreeMap for the underlying storage.
     *
     * @param <K> the runtime key type.
     * @param <V> the runtime value type.
     *
     * @return a new empty WORM map using a TreeMap for the underlying storage.
     */
    public static <K extends Comparable<?>, V> WormMap<K, V> tree() {
        return new WormMap<>(new TreeMap<>());
    }

    /**
     * Retrieves an existing value from this map or assigns a default.
     *
     * @param targetKey    the key associated with the value.
     * @param defaultValue the default value to assign and return if
     *                     there is no match for the key.
     *
     * @return the value associated with the specified key, or the
     * default value if there is no match.
     */
    public V getOrAssign(@NonNull K targetKey, @NonNull V defaultValue) {
        V result = map.get(targetKey);

        if (result != null) {
            return result;
        }
        else {
            map.put(targetKey, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Retrieves an existing value from this map or computes and
     * assigns a default.
     *
     * @param key     the key associated with the value.
     * @param compute the function to compute missing values.
     *
     * @return the value associated with the specified keys, or the
     * computed value if there is no match.
     */
    public V getOrCompute(@NonNull K key, @NonNull Function<K, V> compute) {
        V result = map.get(key);

        if (result != null) {
            return result;
        }
        else {
            var value = compute.apply(key);
            map.put(key, value);
            return value;
        }
    }

    /**
     * Retrieves a value from this map or throws an exception.
     *
     * @param key the row key associated with the value.
     *
     * @return the value associated with the specified key.
     *
     * @throws RuntimeException unless a value has been associated with
     * the specified key.
     */
    public V getOrThrow(@NonNull K key) {
        V result = map.get(key);

        if (result != null)
            return result;
        else
            throw new MorpheusException("No value for key [%s].", key);
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
     * @throws RuntimeException if this map already contains the key.
     */
    @Override
    public V put(@NonNull K key, @NonNull V value) {
        if (map.containsKey(key))
            throw new MorpheusException("Key [%s] has already been assigned.", key);
        else
            return map.put(key, value);
    }

    /**
     * Guaranteed to throw an exception and leave the map unmodified.
     * @throws RuntimeException always: entry removal is forbidden.
     */
    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    /**
     * Guaranteed to throw an exception and leave the map unmodified.
     * @throws RuntimeException always: entry removal is forbidden.
     */
    @Override
    @Deprecated
    public V remove(@NonNull Object key) {
        throw new UnsupportedOperationException();
    }
}
