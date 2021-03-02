/*
 * Copyright (C) 2018-2019 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.series;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import com.d3x.morpheus.util.GenericType;
import com.d3x.morpheus.util.IntComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.util.Resource;

/**
 * An interface to an immutable series of values stored against a unique key
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author Xavier Witdouck
 */
public interface DataSeries<K,V> extends Cloneable {

    /**
     * Returns the size of series
     * @return  the series size
     */
    int size();

    /**
     * Returns the stream of keys for this series
     * @return   the stream of keys for series
     */
    Stream<K> keys();

    /**
     * Returns the key class for series
     * @return  the key class
     */
    Class<K> keyClass();

    /**
     * Returns the value class for series
     * @return  the value class
     */
    Class<V> valueClass();

    /**
     * Returns the key at the index specified
     * @param index the index of key
     * @return      the key at index
     */
    K getKey(int index);

    /**
     * Returns true if the series contains key
     * @param key   the key to match
     * @return      true if series contains key
     */
    boolean contains(K key);

    /**
     * Returns true if series value is null for key
     * @param key   the key to match
     * @return      true if value is null
     */
    boolean isNull(K key);

    /**
     * Returns true if series value is null for index
     * @param index the index of key
     * @return      true if value is null
     */
    boolean isNullAt(int index);

    /**
     * Returns the value for the key
     * @param key   the key to match
     * @return      the value or null if no match
     */
    V getValue(K key);

    /**
     * Returns the value at the index location
     * @param index the index location
     * @return      the value, can be null
     */
    V getValueAt(int index);

    /**
     * Sorts this series according to the comparator
     * @param comparator    the comparator to apply
     * @throws UnsupportedOperationException    if not supported
     */
    default void sort(IntComparator comparator) {
        throw new UnsupportedOperationException("Sorting not supported by this series");
    }

    /**
     * Returns true if this is a parallel series
     * @return      true if parallel series
     */
    default boolean isParallel() {
        return false;
    }

    /**
     * Returns true if this series is empty
     * @return  true if series is empty
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns true if this series is not empty
     * @return  true if not empty
     */
    default boolean notEmpty() {
        return size() > 0;
    }

    /**
     * Returns the first key in this series
     * @return  the first key in series
     */
    default Optional<K> firstKey() {
        return isEmpty() ? Optional.empty() : Optional.of(getKey(0));
    }

    /**
     * Returns the last key in this series
     * @return  the last key in series
     */
    default Optional<K> lastKey() {
        return isEmpty() ? Optional.empty() : Optional.of(getKey(size()-1));
    }

    /**
     * Returns a list of the keys in this series.
     * @return a list for which {@code get(k).equals(this.getKey(k))}.
     */
    default List<K> listKeys() {
        List<K> keyList = new ArrayList<>(size());

        for (int index = 0; index < size(); ++index)
            keyList.add(getKey(index));

        return keyList;
    }

    /**
     * Ensures that a DataSeries contains a particular key.
     *
     * @param key the required key.
     *
     * @throws RuntimeException unless this series contains the specified key.
     */
    default void requireKey(K key) {
        if (!contains(key))
            throw new MorpheusException("Missing required key: [%s].", key);
    }

    /**
     * Ensures that a DataSeries contains particular keys.
     *
     * @param keys the required keys.
     *
     * @throws RuntimeException unless this series contains the specified keys.
     */
    default void requireKeys(Iterable<K> keys) {
        for (K key : keys)
            requireKey(key);
    }

    /**
     * Ensures that a DataSeries contains particular keys.
     *
     * @param keys the required keys.
     *
     * @throws RuntimeException unless this series contains the specified keys.
     */
    default void requireKeys(Stream<K> keys) {
        keys.forEach(this::requireKey);
    }

    /**
     * Maps this data series to a double series
     * @param mapper    the mapper function
     * @return          the resulting series
     */
    default DoubleSeries<K> mapToDoubles(ToDoubleFunction<K> mapper) {
        var keyClass = keyClass();
        var mapped = DoubleSeries.builder(keyClass).capacity(this.size());
        this.keys().forEach(v -> mapped.putDouble(v, mapper.applyAsDouble(v)));
        return mapped.build();
    }

    /**
     * Returns a new DataSeries Builder for key and value type
     * @param keyType   the key type
     * @param valueType the value type
     * @return          the builder
     */
    static <K,V> DataSeriesBuilder<K,V> builder(Class<K> keyType, Class<V> valueType) {
        return DataSeriesBuilder.builder(keyType, valueType);
    }

    /**
     * Returns a parameterized type to represent a series with key and value type
     * @param keyType   the key type for series
     * @param valueType the value type for series
     * @return          the parameterized type
     */
    static ParameterizedType ofType(
        @lombok.NonNull Class<?> keyType,
        @lombok.NonNull Class<?> valueType) {
        return GenericType.of(DataSeries.class, keyType, valueType);
    }

    /**
     * Returns a CSV data series read adapter for resource
     * @param file  the resource to read from
     * @param <K>   the key type for series
     * @return      the CSV read adapter
     */
    static <K,V> DataSeriesRead<K,V,DataSeries<K,V>> read(File file) {
        return new DataSeriesRead<>(Resource.of(file));
    }

    /**
     * Returns a CSV data series read adapter for resource
     * @param url   the resource to read from
     * @param <K>   the key type for series
     * @return      the CSV read adapter
     */
    static <K,V> DataSeriesRead<K,V,DataSeries<K,V>> read(URL url) {
        return new DataSeriesRead<>(Resource.of(url));
    }

    /**
     * Returns a CSV data series read adapter for resource
     * @param path  the resource to read from
     * @param <K>   the key type for series
     * @return      the CSV read adapter
     */
    static <K,V> DataSeriesRead<K,V,DataSeries<K,V>> read(String path) {
        return new DataSeriesRead<>(Resource.of(path));
    }

    /**
     * Returns a CSV data series read adapter for resource
     * @param is    the resource to read from
     * @param <K>   the key type for series
     * @return      the CSV read adapter
     */
    static <K,V> DataSeriesRead<K,V,DataSeries<K,V>> read(InputStream is) {
        return new DataSeriesRead<>(Resource.of(is));
    }

}
