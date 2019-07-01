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

import java.util.stream.IntStream;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.util.IO;

/**
 * A type specific series for double values
 *
 * @param <K> the key type
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public class DoubleSeries<K> extends DataSeries<K,Double> {

    /**
     * Constructor
     * @param keys      the array of keys
     * @param values    the array of values
     */
    private DoubleSeries(
        @lombok.NonNull Array<K> keys,
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
     * Returns a newly created builder with initial capacity
     * @param capacity the initial capacity
     * @param <T>       the builder type
     * @return          the new builder
     */
    public static <T> Builder<T> builder(int capacity) {
        return new Builder<>(capacity);
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
     * @param index     the index location
     * @param fallback  the fallback value if no match for key
     * @return          the value or null
     */
    public double getDoubleAtOrElse(int index, double fallback) {
        var coord = this.index.getCoordinateAt(index);
        return coord >= 0 ? values.getDouble(coord) : fallback;
    }


    /**
     * A builder class for DoubleSeries
     * @param <K>   the key type
     */
    public static class Builder<K> extends DataSeries.Builder<K,Double,DoubleSeries<K>> {

        /**
         * Constructor
         * @param capacity  the initial capacity for builder
         */
        Builder(int capacity) {
            super(capacity);
        }


        @Override
        public DoubleSeries<K> build() {
            return new DoubleSeries<>(keys.toArray(), values.toArray());
        }


        /**
         * Adds a double to this series builder
         * @param key       the key for entry
         * @param value     the value for entry
         * @return          this builder
         */
        public Builder addDouble(K key, double value) {
            this.keys.add(key);
            this.values.addDouble(value);
            return this;
        }
    }



    public static void main(String[] args) {
        var builder = DoubleSeries.<String>builder(1000);
        IntStream.range(0, 1200).forEach(i -> builder.addDouble("S" + i, Math.random()));
        var series = builder.build();
        IO.println(series);
    }

}
