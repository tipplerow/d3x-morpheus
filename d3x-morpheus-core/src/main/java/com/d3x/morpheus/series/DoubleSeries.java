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

import java.lang.reflect.ParameterizedType;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import com.d3x.core.util.Generic;
import com.d3x.core.util.StopWatch;
import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.stats.Sample;
import com.d3x.morpheus.stats.Stats;
import com.d3x.morpheus.util.IO;

/**
 * A type specific series for optimized to hold primitive double values
 *
 * @param <K> the key type
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public class DoubleSeries<K> extends DataSeries<K,Double> implements Sample {

    /**
     * Constructor
     * @param keys      the array of keys
     * @param values    the array of values
     */
    private DoubleSeries(
        @lombok.NonNull Index<K> keys,
        @lombok.NonNull Array<Double> values) {
        super(keys, values);
    }


    /**
     * Copy constructor
     * @param source    the source to copy
     */
    DoubleSeries(DataSeries<K,Double> source) {
        super(source);
    }


    /**
     * Returns a newly created builder for this class
     * @param <K>   the key type
     * @return      the new builder
     */
    public static <K> Builder<K> builder() {
        return new Builder<>();
    }


    /**
     * Returns a parameterized type of this class with key type
     * @param keyType   the key type for series
     * @param <K>       key type
     * @return          newly created parameterized type
     */
    public static <K> ParameterizedType typeOf(Class<K> keyType) {
        return Generic.of(DoubleSeries.class, keyType, Double.class);
    }


    /**
     * Returns the value for key
     * @param key   the key item
     * @return      the value or null
     */
    public final double getDouble(K key) {
        var coord = index.getCoordinate(key);
        return coord >= 0 ? values.getDouble(coord) : Double.NaN;
    }


    /**
     * Returns the value at the index
     * @param index the index location
     * @return      the value or null
     */
    public final double getDoubleAt(int index) {
        var coord = this.index.getCoordinateAt(index);
        return coord >= 0 ? values.getDouble(coord) : Double.NaN;
    }


    /**
     * Returns the value for key
     * @param key   the key item
     * @param fallback  the fallback value if no match for key
     * @return      the value or null
     */
    public final double getDoubleOrElse(K key, double fallback) {
        var coord = index.getCoordinate(key);
        return coord >= 0 ? values.getDouble(coord) : fallback;
    }


    /**
     * Returns the value for key
     * @param index     the index location
     * @param fallback  the fallback value if no match for key
     * @return          the value or null
     */
    public final double getDoubleAtOrElse(int index, double fallback) {
        var coord = this.index.getCoordinateAt(index);
        return coord >= 0 ? values.getDouble(coord) : fallback;
    }


    /**
     * Returns the stats interface for this series
     * @return  the stats interface for series
     */
    public Stats<Double> stats() {
        return Stats.of(this);
    }


    /**
     * Returns a mapping of this series with the keys mapped
     * @param mapper    the key mapper
     * @param <T>       the type for mapper
     * @return          the mapped series
     */
    public <T> DoubleSeries<T> mapKeys(Function<K,T> mapper) {
        return new DoubleSeries<>(super.mapKeys(mapper));
    }


    /**
     * Returns a filtered copy of this series
     * @param predicate the predicate to select items
     * @return          the filtered series
     */
    public DoubleSeries<K> filter(Predicate<Entry<K,Double>> predicate) {
        return new DoubleSeries<>(super.filter(predicate));
    }


    /**
     * Returns a shallow copy of this series sorted according to comparator
     * @param comparator    the comparator to apply sorting to entries
     * @return              the shallow copy sorted series
     */
    public DoubleSeries<K> sort(Comparator<Entry<K,Double>> comparator) {
        return new DoubleSeries<>(super.sort(false, comparator));
    }


    /**
     * Returns a shallow copy of this series sorted according to comparator
     * @param parallel      true to apply parallel sorting algo
     * @param comparator    the comparator to apply sorting to entries
     * @return              the shallow copy sorted series
     */
    public DoubleSeries<K> sort(boolean parallel, Comparator<Entry<K,Double>> comparator) {
        return new DoubleSeries<>(super.sort(parallel, comparator));
    }


    @Override
    Entry<K,Double> createEntry() {
        return new Entry<>(this) {
            @Override
            public final double getDouble() {
                return getDoubleAt(ordinal());
            }
        };
    }


    @Override
    public String toString() {
        return "DoubleSeries type: " + valueType() + ", size: " + size() + ", first: " + firstKey().orNull() + ", last: " + lastKey().orNull();
    }



    /**
     * An incremental builder for DataSeries
     * @param <K>   the key type
     */
    public static class Builder<K> {

        protected ArrayBuilder<K> keys;
        protected ArrayBuilder<Double> values;

        /**
         * Sets the initial capacity for this builder
         * @param capacity  the initial capacity
         * @return          this builder
         */
        public Builder<K> capacity(int capacity) {
            if (keys != null) {
                return this;
            } else {
                this.keys = ArrayBuilder.of(capacity);
                this.values = ArrayBuilder.of(capacity, Double.class);
                return this;
            }
        }

        /**
         * Returns a new series created from the state of this builder
         * @return      the newly created series
         */
        public DoubleSeries<K> build() {
            this.capacity(10);
            return new DoubleSeries<>(Index.of(keys.toArray()), values.toArray());
        }

        /**
         * Adds a double to this series builder
         * @param key       the key for entry
         * @param value     the value for entry
         * @return          this builder
         */
        public Builder<K> addDouble(@lombok.NonNull K key, double value) {
            this.capacity(100);
            this.keys.add(key);
            this.values.addDouble(value);
            return this;
        }
    }


    public static void main(String[] args) {
        var size = 1000000;
        var builder = DoubleSeries.<Integer>builder().capacity(size);
        IntStream.range(0, size).forEach(i -> builder.addDouble(i, Math.random() * 100d));
        var series = builder.build();
        for (int i=0; i<10; ++i) {
            var time = StopWatch.time(() -> series.sort(true, Comparator.comparingDouble(Entry::getDouble)));
            //var time = StopWatch.time(() -> series.values.sort(true));
            IO.println("Sorted " + size + " entries in " + time.getMillis() + " millis");
        }
    }


}
