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
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.d3x.core.util.Generic;
import com.d3x.core.util.Option;
import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.util.Resource;

/**
 * A class that represents an ordered series of data items keyed by a unique key of some type.
 *
 * @param <K>   the series type
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public class DataSeries<K,V> {

    protected Index<K> index;
    protected Array<V> values;

    /**
     * Constructor
     * @param keys      the keys for series
     * @param values    the values for series
     */
    DataSeries(
        @lombok.NonNull Index<K> keys,
        @lombok.NonNull Array<V> values) {
        if (keys.size() != values.length()) {
            throw new IllegalArgumentException("Key and value array must have the same length");
        } else {
            this.index = Index.of(keys);
            this.values = values;
        }
    }

    /**
     * Copy constructor
     * @param source    the source to copy
     */
    DataSeries(DataSeries<K,V> source) {
        this.index = source.index;
        this.values = source.values;
    }


    /**
     * Returns a newly created data series builder
     * @param <K>   the keyx type for series
     * @param <V>   the data type for series
     * @return      the newly created builder
     */
    public static <K,V> Builder<K,V> builder() {
        return new Builder<>();
    }


    /**
     * Returns a parameterized type of this class with key type
     * @param keyType   the key type for series
     * @param valueType the value type
     * @param <K>       key type
     * @param <V>       value type
     * @return          newly created parameterized type
     */
    public static <K,V> ParameterizedType typeOf(Class<K> keyType, Class<V> valueType) {
        return Generic.of(DataSeries.class, keyType, valueType);
    }


    /**
     * Returns the CSV adapter for this series
     * @param file  the csv resource
     * @return  the CSV adapter
     */
    public static <K,V> DataSeriesCsv<K,V> csv(File file) {
        return new DataSeriesCsv<>(Resource.of(file));
    }


    /**
     * Returns the CSV adapter for this series
     * @param url   the csv resource
     * @return  the CSV adapter
     */
    public static <K,V> DataSeriesCsv<K,V> csv(URL url) {
        return new DataSeriesCsv<>(Resource.of(url));
    }


    /**
     * Returns the CSV adapter for this series
     * @param is    the csv resource
     * @return  the CSV adapter
     */
    public static <K,V> DataSeriesCsv<K,V> csv(InputStream is) {
        return new DataSeriesCsv<>(Resource.of(is));
    }


    /**
     * Returns the CSV adapter for this series
     * @param path    the csv resource
     * @return  the CSV adapter
     */
    public static <K,V> DataSeriesCsv<K,V> csv(String path) {
        return new DataSeriesCsv<>(Resource.of(path));
    }


    /**
     * Returns the size of this series
     * @return      the size of series
     */
    public int size() {
        return values.length();
    }


    /**
     * Returns the key class for series
     * @return  the key class for series
     */
    public Class<K> keyClass() {
        return index.type();
    }


    /**
     * Returns the key data type for series
     * @return  the key data type
     */
    public ArrayType keyType() {
        return ArrayType.of(index.type());
    }


    /**
     * Returns the class for the data in this series
     * @return  the class for data
     */
    public Class<?> dataClass() {
        return values.type();
    }


    /**
     * Returns the data type for this series
     * @return      the data type for series
     */
    public ArrayType dataType() {
        return values.typeCode();
    }


    /**
     * Returns the stream of keys for series
     * @return   the stream of keys
     */
    public Stream<K> getKeys() {
        return index.keys();
    }


    /**
     * Returns the key for the ordinal location
     * @param ordinal   the ordinal location
     * @return          the key at location
     */
    public K getKey(int ordinal) {
        return index.getKey(ordinal);
    }


    /**
     * Returns the value for key
     * @param key   the key item
     * @return      the value or null
     */
    public V getValue(K key) {
        var coord = index.getCoordinate(key);
        return coord >= 0 ? values.getValue(coord) : null;
    }


    /**
     * Returns the value for key
     * @param key   the key item
     * @param fallback  the fallback value if no match for key
     * @return      the value or null
     */
    public V getValueOrElse(K key, V fallback) {
        var coord = index.getCoordinate(key);
        return coord >= 0 ? values.getValue(coord) : fallback;
    }


    /**
     * Returns the value at the index
     * @param index the index location
     * @return      the value or null
     */
    public V getValueAt(int index) {
        var coord = this.index.getCoordinateAt(index);
        return values.getValue(coord);
    }


    /**
     * Returns the value for key
     * @param index     the index location
     * @param fallback  the fallback value if no match for key
     * @return          the value or null
     */
    public V getValueAtOrElse(int index, V fallback) {
        var coord = this.index.getCoordinateAt(index);
        var value = values.getValue(coord);
        return value != null ? value : fallback;
    }


    /**
     * Returns the first key if size > 0
     * @return  the first key
     */
    public Option<K> firstKey() {
        return Option.of(index.first().orElse(null));
    }


    /**
     * Returns the last key if size > 0
     * @return  the last key
     */
    public Option<K> lastKey() {
        return Option.of(index.last().orElse(null));
    }


    /**
     * Returns a filtered copy of this series
     * @param predicate the predicate to select keys
     * @return          the filtered series
     */
    public DataSeries<K,V> filterKeys(Predicate<K> predicate) {
        var newIndex = index.filter(predicate);
        var indexes = newIndex.keys().mapToInt(index::getCoordinate).toArray();
        var newValues = values.copy(indexes);
        return new DataSeries<>(newIndex, newValues);
    }


    /**
     * Casts this series to a double series
     * @return      this series as a double series
     */
    @SuppressWarnings("unchecked")
    public DoubleSeries<K> toDoubles() {
        return new DoubleSeries<>((DataSeries<K,Double>)this);
    }


    /**
     * An incremental builder for DataSeries
     * @param <K>   the key type
     * @param <V>   the value type
     */
    public static class Builder<K,V> {

        protected ArrayBuilder<K> keys;
        protected ArrayBuilder<V> values;

        /**
         * Sets the initial capacity for this builder
         * @param capacity      the initial capacity
         * @return  true if capacity assigned, false if already assigned
         */
        public boolean capacity(int capacity) {
            if (keys != null) {
                return false;
            } else {
                this.keys = ArrayBuilder.of(capacity);
                this.values = ArrayBuilder.of(capacity);
                return true;
            }
        }


        /**
         * Returns a new series created from the state of this builder
         * @return      the newly created series
         */
        public DataSeries<K,V> build() {
            this.capacity(10);
            return new DataSeries<>(Index.of(keys.toArray()), values.toArray());
        }


        /**
         * Adds a boolean to this series builder
         * @param key       the key for entry
         * @param value     the value for entry
         * @return          this builder
         */
        public Builder addBoolean(@lombok.NonNull K key, boolean value) {
            this.keys.add(key);
            this.values.addBoolean(value);
            return this;
        }


        /**
         * Adds a int to this series builder
         * @param key       the key for entry
         * @param value     the value for entry
         * @return          this builder
         */
        public Builder addInt(@lombok.NonNull K key, int value) {
            this.keys.add(key);
            this.values.addInt(value);
            return this;
        }


        /**
         * Adds a long to this series builder
         * @param key       the key for entry
         * @param value     the value for entry
         * @return          this builder
         */
        public Builder addLong(@lombok.NonNull K key, long value) {
            this.keys.add(key);
            this.values.addLong(value);
            return this;
        }


        /**
         * Adds a double to this series builder
         * @param key       the key for entry
         * @param value     the value for entry
         * @return          this builder
         */
        public Builder addDouble(@lombok.NonNull K key, double value) {
            this.keys.add(key);
            this.values.addDouble(value);
            return this;
        }


        /**
         * Adds a value to this series builder
         * @param key       the key for entry
         * @param value     the value for entry
         * @return          this builder
         */
        public Builder addValue(K key, V value) {
            this.capacity(100);
            this.keys.add(key);
            this.values.add(value);
            return this;
        }
    }
}
