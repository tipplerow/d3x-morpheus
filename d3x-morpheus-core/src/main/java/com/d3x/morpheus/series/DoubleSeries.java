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
import java.util.function.Function;

import com.d3x.core.util.Generic;
import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.stats.Sample;
import com.d3x.morpheus.stats.Stats;

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
    public double getDouble(K key) {
        var coord = index.getCoordinate(key);
        return coord >= 0 ? values.getDouble(coord) : Double.NaN;
    }


    /**
     * Returns the value at the index
     * @param index the index location
     * @return      the value or null
     */
    public double getDoubleAt(int index) {
        var coord = this.index.getCoordinateAt(index);
        return coord >= 0 ? values.getDouble(coord) : Double.NaN;
    }


    /**
     * Returns the value for key
     * @param key   the key item
     * @param fallback  the fallback value if no match for key
     * @return      the value or null
     */
    public double getDoubleOrElse(K key, double fallback) {
        var coord = index.getCoordinate(key);
        return coord >= 0 ? values.getDouble(coord) : fallback;
    }


    /**
     * Returns the value for key
     * @param index     the index location
     * @param fallback  the fallback value if no match for key
     * @return          the value or null
     */
    public double getDoubleAtOrElse(int index, double fallback) {
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
        var result = index.map((key, index) -> mapper.apply(key));
        return new DoubleSeries<>(result, values);
    }


    /**
     * Returns a filter of this series by applying the predicate
     * @param predicate     the predicate to filter on
     * @return              the filtered series
     */
    public DoubleSeries<K> filterDoubles(Predicate<K> predicate) {
        var builder = DataSeries.<K,Double>builder();
        builder.capacity(this.size() / 2);
        this.forEach((key, ordinal, value) -> {
            if (predicate.test(key, ordinal, value)) {
                builder.addDouble(key, value);
            }
        });
        return builder.build().toDoubles();
    }


    /**
     * Iterates over entries in this series and applies them to consumer
     * @param consumer  the consumer to receive entries from this series
     */
    public void forEach(Consumer<K> consumer) {
        var size = this.size();
        for (int i=0; i<size; ++i) {
            var key = getKey(i);
            var value = getDoubleAt(i);
            consumer.apply(key, i, value);
        }
    }



    interface Predicate<K> {

        boolean test(K key, int ordinal, double value);
    }


    interface Consumer<K> {

        void apply(K key, int ordinal, double value);
    }

}
