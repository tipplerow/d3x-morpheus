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
package com.d3x.morpheus.series;

import java.util.stream.Stream;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.index.Index;

/**
 * An interface to an orderd series of values
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
        @lombok.NonNull Array<K> keys,
        @lombok.NonNull Array<V> values) {
        if (keys.length() != values.length()) {
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
     * Returns the length of this series
     * @return      the length of series
     */
    public int length() {
        return values.length();
    }

    /**
     * Returns the stream of keys for series
     * @return   the stream of keys
     */
    public Stream<K> getKeys() {
        return index.keys();
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
     * @param <D>   the series type
     */
    public static abstract class Builder<K,V,D extends DataSeries<K,V>> {

        protected ArrayBuilder<K> keys;
        protected ArrayBuilder<V> values;


        /**
         * Constructor
         * @param capacity  the initial capacity for builder
         */
        Builder(int capacity) {
            this.keys = ArrayBuilder.of(capacity);
            this.values = ArrayBuilder.of(capacity);
        }


        /**
         * Returns a new series created from the state of this builder
         * @return      the newly created series
         */
        public abstract D build();


        /**
         * Adds a value to this series builder
         * @param key       the key for entry
         * @param value     the value for entry
         * @return          this builder
         */
        public Builder addValue(K key, V value) {
            this.keys.add(key);
            this.values.add(value);
            return this;
        }
    }

}
