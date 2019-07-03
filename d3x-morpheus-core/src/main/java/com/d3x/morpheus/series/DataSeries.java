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
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
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
     * @param index     the index with keys for series
     * @param values    the array with values for series
     */
    DataSeries(
        @lombok.NonNull Index<K> index,
        @lombok.NonNull Array<V> values) {
        if (index.size() != values.length()) {
            throw new IllegalArgumentException("Key and value array must have the same length");
        } else {
            this.index = index;
            this.values = values;
        }
    }

    /**
     * Copy constructor
     * @param source    the source to copy
     */
    DataSeries(@lombok.NonNull DataSeries<K,V> source) {
        this.index = source.index;
        this.values = source.values;
    }


    /**
     * Returns a parameterized type of this class with key and value type
     * @param keyType   the key type for series
     * @param valueType the value type for series
     * @param <K>       key type
     * @return          newly created parameterized type
     */
    public static <K,V> ParameterizedType typeOf(Class<K> keyType, Class<V> valueType) {
        return Generic.of(DataSeries.class, keyType, valueType);
    }


    /**
     * Returns a newly created builder for the data type provided
     * @param dataType      the data type class
     * @param <K>           key type
     * @param <V>           data type
     * @return              the new builder
     */
    public static <K,V> Builder<K,V> builder(Class<V> dataType) {
        return new Builder<>(dataType);
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
    public final int size() {
        return values.length();
    }


    /**
     * Returns the key class for series
     * @return  the key class for series
     */
    public final Class<K> keyClass() {
        return index.type();
    }


    /**
     * Returns the key data type for series
     * @return  the key data type
     */
    public final ArrayType keyType() {
        return ArrayType.of(index.type());
    }


    /**
     * Returns the class for the value in this series
     * @return  the class for value
     */
    public final Class<?> valueClass() {
        return values.type();
    }


    /**
     * Returns the value type for this series
     * @return      the value type for series
     */
    public final ArrayType valueType() {
        return values.typeCode();
    }


    /**
     * Returns the parameterized type for this class
     * @return      the parameterized type
     */
    public ParameterizedType type() {
        return Generic.of(getClass(), keyClass(), valueClass());
    }


    /**
     * Returns the stream of keys for series
     * @return   the stream of keys
     */
    public final Stream<K> getKeys() {
        return index.keys();
    }


    /**
     * Returns the key for the ordinal location
     * @param ordinal   the ordinal location
     * @return          the key at location
     */
    public final K getKey(int ordinal) {
        return index.getKey(ordinal);
    }


    /**
     * Returns the first key if size > 0
     * @return  the first key
     */
    public final Option<K> firstKey() {
        return Option.of(index.first().orElse(null));
    }


    /**
     * Returns the last key if size > 0
     * @return  the last key
     */
    public final Option<K> lastKey() {
        return Option.of(index.last().orElse(null));
    }


    /**
     * Returns the value for key
     * @param key   the key item
     * @return      the value or null
     */
    public final V getValue(K key) {
        var coord = index.getCoordinate(key);
        return coord >= 0 ? values.getValue(coord) : null;
    }


    /**
     * Returns the value for key
     * @param key   the key item
     * @param fallback  the fallback value if no match for key
     * @return      the value or null
     */
    public final V getValueOrElse(K key, V fallback) {
        var coord = index.getCoordinate(key);
        return coord >= 0 ? values.getValue(coord) : fallback;
    }


    /**
     * Returns the value at the index
     * @param index the index location
     * @return      the value or null
     */
    public final V getValueAt(int index) {
        var coord = this.index.getCoordinateAt(index);
        return values.getValue(coord);
    }


    /**
     * Returns the value for key
     * @param index     the index location
     * @param fallback  the fallback value if null at index
     * @return          the value or null
     */
    public final V getValueAtOrElse(int index, V fallback) {
        var coord = this.index.getCoordinateAt(index);
        var value = values.getValue(coord);
        return value != null ? value : fallback;
    }


    /**
     * Iterates over all entries in this series
     * @param consumer  the consumer to receive key, oridinal and value
     */
    public void forEach(Consumer<Entry<K,V>> consumer) {
        var size = this.size();
        var value = createEntry();
        for (int i=0; i<size; ++i) {
            consumer.accept(value.locate(i));
        }
    }


    /**
     * Returns a shallow copy of this series sorted according to comparator
     * @param comparator    the comparator to apply sorting to entries
     * @return              the shallow copy sorted series
     */
    public DataSeries<K,V> sort(Comparator<Entry<K,V>> comparator) {
        return sort(false, comparator);
    }


    /**
     * Returns a shallow copy of this series sorted according to comparator
     * @param parallel      true to apply parallel sorting algo
     * @param comparator    the comparator to apply sorting to entries
     * @return              the shallow copy sorted series
     */
    public DataSeries<K,V> sort(boolean parallel, Comparator<Entry<K,V>> comparator) {
        var clone = (DataSeries<K,V>)new DataSeries<>(index.copy(false), values);
        var entry1 = clone.createEntry();
        var entry2 = clone.createEntry();
        clone.index.sort(parallel, (i1, i2) -> {
            entry1.locate(i1);
            entry2.locate(i2);
            return comparator.compare(entry1, entry2);
        });
        return clone;
    }


    /**
     * Returns a mapping of this series with the keys mapped
     * @param mapper    the key mapper
     * @param <T>       the type for mapper
     * @return          the mapped series
     */
    public <T> DataSeries<T,V> mapKeys(Function<K,T> mapper) {
        var result = index.map((key, index) -> mapper.apply(key));
        return new DataSeries<>(result, values);
    }


    /**
     * Returns a filtered copy of this series
     * @param predicate the predicate to select items
     * @return          the filtered series
     */
    public DataSeries<K,V> filter(Predicate<Entry<K,V>> predicate) {
        var size = this.size();
        var keys = ArrayBuilder.of(size, index.type());
        var indexes = ArrayBuilder.of(size, Integer.class);
        this.forEach(entry -> {
            if (predicate.test(entry)) {
                var key = entry.key();
                indexes.addInt(index.getCoordinate(key));
                keys.add(key);
            }
        });
        var newIndex = Index.of(keys.toArray());
        var newValues = values.copy(indexes.toArray());
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
     * Returns a newly created entry
     * @return  the newly created entry
     */
    Entry<K,V> createEntry() {
        return new Entry<>(this);
    }


    @Override
    public String toString() {
        return "DataSeries type: " + valueType() + ", size: " + size() + ", first: " + firstKey().orNull() + ", last: " + lastKey().orNull();
    }

    /**
     * A class used to represent an entry in a data series by exposing the key, ordinal and value
     */
    static class Entry<K,V> {

        private int ordinal;
        private DataSeries<K,V> series;

        /**
         * Constructor
         * @param series    the series to wrap
         */
        Entry(DataSeries<K,V> series) {
            this.series = series;
        }

        /**
         * Changes the location pointer for this entry
         * @param ordinal   the new ordinal location
         * @return          this entry
         */
        private Entry<K,V> locate(int ordinal) {
            this.ordinal = ordinal;
            return this;
        }

        /**
         * Returns the key for this entry
         * @return      the key for entry
         */
        public final K key() {
            return series.getKey(ordinal);
        }

        /**
         * Returns the ordinal location for entry
         * @return      the ordinal location
         */
        public final int ordinal() {
            return ordinal;
        }

        /**
         * Returns the value for this entry as a boolean
         * @return  the value as a boolean
         */
        public boolean getBoolean() {
            var value = series.getValueAt(ordinal);
            return value != null ? (Boolean)value : false;
        }

        /**
         * Returns the value for this entry as an int
         * @return  the value as an int
         */
        public int getInt() {
            var value = series.getValueAt(ordinal);
            return value != null ? ((Number)value).intValue() : 0;
        }

        /**
         * Returns the value for this entry as a long
         * @return  the value as a long
         */
        public long getLong() {
            var value = series.getValueAt(ordinal);
            return value != null ? ((Number)value).longValue() : 0L;
        }

        /**
         * Returns the value for this entry as a double
         * @return      the value as a double
         */
        public double getDouble() {
            var value = series.getValueAt(ordinal);
            return value != null ? ((Number)value).doubleValue() : Double.NaN;
        }

        /**
         * Returns the value for this entry as an object
         * @return  the value as an object
         */
        public final V getValue() {
            return series.getValueAt(ordinal);
        }
    }



    /**
     * An incremental builder for DataSeries
     * @param <K>   the key type
     * @param <V>   the value type
     */
    public static class Builder<K,V> {

        private Class<V> dataType;
        private ArrayBuilder<K> keys;
        private ArrayBuilder<V> values;


        /**
         * Constructor
         */
        public Builder() {
            this(null);
        }

        /**
         * Constructor
         * @param dataType  the data type
         */
        private Builder(Class<V> dataType) {
            this.dataType = dataType;
        }

        /**
         * Sets the initial capacity for this builder
         * @param capacity  the initial capacity
         * @return          this builder
         */
        public Builder<K,V> capacity(int capacity) {
            if (keys != null) {
                return this;
            } else {
                this.keys = ArrayBuilder.of(capacity);
                this.values = ArrayBuilder.of(capacity, dataType);
                return this;
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
        public Builder<K,V> addBoolean(@lombok.NonNull K key, boolean value) {
            this.capacity(100);
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
        public Builder<K,V> addInt(@lombok.NonNull K key, int value) {
            this.capacity(100);
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
        public Builder<K,V> addLong(@lombok.NonNull K key, long value) {
            this.capacity(100);
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
        public Builder<K,V> addDouble(@lombok.NonNull K key, double value) {
            this.capacity(100);
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
        public Builder<K,V> addValue(K key, V value) {
            this.capacity(100);
            this.keys.add(key);
            this.values.add(value);
            return this;
        }
    }
}
