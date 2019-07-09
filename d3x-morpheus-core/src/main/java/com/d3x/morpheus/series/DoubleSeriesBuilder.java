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

import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.d3x.core.util.StopWatch;
import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.coding.IntCoding;
import com.d3x.morpheus.array.coding.LongCoding;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.IntComparator;
import com.d3x.morpheus.util.SortAlgorithm;
import com.d3x.morpheus.util.Swapper;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TLongDoubleMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * An interface to a Builder for DoubleSeries
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @param <K>   the series key
 */
public interface DoubleSeriesBuilder<K> extends DataSeriesBuilder<K,Double> {

    float DEFAULT_LOAD_FACTOR = 0.85f;

    /**
     * Returns a newly created DoubleSeries from the state of this builder
     * @return      the newly created DoubleSeries
     */
    DoubleSeries<K> build();

    /**
     * Sets the initial capacity for builder
     * @param capacity  the initial capacity
     * @return          this builder
     */
    DoubleSeriesBuilder<K> capacity(int capacity);

    /**
     * Puts an entry in this builder against the key
     * @param key       the entry key
     * @param value     the entry value
     * @return          this builder
     */
    DoubleSeriesBuilder<K> putDouble(@lombok.NonNull K key, double value);


    @Override
    default DataSeriesBuilder<K, Double> putValue(K key, Double value) {
        return putDouble(key, value != null ? value : Double.NaN);
    }

    /**
     * Returns a new DoubleSeries Builder for key type
     * @param keyType   the key type
     * @return          the builder
     */
    static <K> DoubleSeriesBuilder<K> builder(Class<K> keyType) {
        if (LongCoding.Support.includes(keyType)) {
            var coding = LongCoding.Support.getCoding(keyType).orNull();
            return new LongCodingDoubleSeriesBuilder<>(keyType, coding);
        } else if (IntCoding.Support.includes(keyType)) {
            var coding = IntCoding.Support.getCoding(keyType).orNull();
            return new IntCodingDoubleSeriesBuilder<>(keyType, coding);
        } else {
            return new DefaultDoubleSeriesBuilder<>(keyType);
        }
    }


    /**
     * A Builder for DefaultSeries
     */
    class DefaultDoubleSeriesBuilder<K> implements DoubleSeriesBuilder<K> {

        private Class<K> keyType;
        private ArrayBuilder<K> keys;
        private TObjectDoubleMap<K> values;

        /**
         * Constructor
         * @param keyType   the key type
         */
        DefaultDoubleSeriesBuilder(Class<K> keyType) {
            this.keyType = keyType;
        }

        @Override()
        public DoubleSeries<K> build() {
            this.capacity(100);
            return new DefaultDoubleSeries<>(keys.toArray(), values);
        }

        @Override()
        public DoubleSeriesBuilder<K> capacity(int capacity) {
            if (values != null) {
                return this;
            } else {
                this.keys = ArrayBuilder.of(capacity, keyType);
                this.values = new TObjectDoubleHashMap<>(capacity, DEFAULT_LOAD_FACTOR, Double.NaN);
                return this;
            }
        }

        @Override()
        public DoubleSeriesBuilder<K> putDouble(@lombok.NonNull K key, double value) {
            this.capacity(100);
            this.keys.append(key);
            this.values.put(key, value);
            return this;
        }
    }


    /**
     * A Builder for IntCodingSeries
     */
    @lombok.AllArgsConstructor()
    class IntCodingDoubleSeriesBuilder<K> implements DoubleSeriesBuilder<K> {

        private Class<K> keyType;
        private IntCoding<K> coding;
        private ArrayBuilder<K> keys;
        private TIntDoubleMap values;

        /**
         * Constructor
         * @param keyType   the key type
         * @param coding    the coding instance
         */
        IntCodingDoubleSeriesBuilder(
            @lombok.NonNull Class<K> keyType,
            @lombok.NonNull IntCoding<K> coding) {
            this.keyType = keyType;
            this.coding = coding;
        }

        @Override
        public DoubleSeries<K> build() {
            this.capacity(100);
            return new IntCodingDoubleSeries<>(coding, keys.toArray(), values);
        }

        @Override
        public DoubleSeriesBuilder<K> capacity(int capacity) {
            if (values != null) {
                return this;
            } else {
                this.keys = ArrayBuilder.of(capacity, keyType);
                this.values = new TIntDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, Integer.MIN_VALUE, Double.NaN);
                return this;
            }
        }

        @Override
        public DoubleSeriesBuilder<K> putDouble(K key, double value) {
            this.capacity(100);
            this.keys.append(key);
            this.values.put(coding.getCode(key), value);
            return this;
        }
    }


    /**
     * A Builder for LongCodingSeries
     */
    @lombok.AllArgsConstructor()
    class LongCodingDoubleSeriesBuilder<K> implements DoubleSeriesBuilder<K> {

        private Class<K> keyType;
        private LongCoding<K> coding;
        private ArrayBuilder<K> keys;
        private TLongDoubleMap values;

        /**
         * Constructor
         * @param keyType   the key type
         * @param coding    the coding instance
         */
        LongCodingDoubleSeriesBuilder(
            @lombok.NonNull Class<K> keyType,
            @lombok.NonNull LongCoding<K> coding) {
            this.keyType = keyType;
            this.coding = coding;
        }

        @Override
        public DoubleSeries<K> build() {
            this.capacity(100);
            return new LongCodingDoubleSeries<>(coding, keys.toArray(), values);
        }

        @Override
        public DoubleSeriesBuilder<K> capacity(int capacity) {
            if (values != null) {
                return this;
            } else {
                this.keys = ArrayBuilder.of(capacity, keyType);
                this.values = new TLongDoubleHashMap(capacity, DEFAULT_LOAD_FACTOR, Long.MIN_VALUE, Double.NaN);
                return this;
            }
        }

        @Override
        public DoubleSeriesBuilder<K> putDouble(K key, double value) {
            this.capacity(100);
            this.keys.append(key);
            this.values.put(coding.getCode(key), value);
            return this;
        }
    }


    /**
     * A convenience base class for DoubleSeries implementations
     * @param <K>   the key type
     */
    abstract class DoubleSeriesBase<K> implements DoubleSeries<K> {

        private Class<K> keyClass;
        private boolean parallel;

        /**
         * Constructor
         * @param keyClass  the key class
         */
        DoubleSeriesBase(Class<K> keyClass) {
            this.keyClass = keyClass;
        }

        @Override
        public boolean isParallel() {
            return parallel;
        }

        @Override
        public Class<K> keyClass() {
            return keyClass;
        }

        @Override
        public String toString() {
            return String.format("DoubleSeries, size: %s, first: %s, last: %s", size(), firstKey().orNull(), lastKey().orNull());
        }

        @Override
        @SuppressWarnings("unchecked")
        public DoubleSeries<K> sequential() {
            try {
                if (!isParallel()) {
                    return this;
                } else {
                    var clone = (DoubleSeriesBase<K>)super.clone();
                    clone.parallel = false;
                    return clone;
                }
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }


        @Override
        @SuppressWarnings("unchecked")
        public DoubleSeries<K> parallel() {
            try {
                if (isParallel()) {
                    return this;
                } else {
                    var clone = (DoubleSeriesBase<K>)super.clone();
                    clone.parallel = true;
                    return clone;
                }
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }


    /**
     * The default implementation of DoubleSeries interface
     * @param <K>   the key type
     */
    class DefaultDoubleSeries<K> extends DoubleSeriesBase<K> implements Swapper {

        private Array<K> keys;
        private TObjectDoubleMap<K> values;
        private double[] temp;

        /**
         * Constructor
         * @param keys      the keys
         * @param values    the values
         */
        DefaultDoubleSeries(
            @lombok.NonNull Array<K> keys,
            @lombok.NonNull TObjectDoubleMap<K> values) {
            super(keys.type());
            this.keys = keys;
            this.values = values;
        }

        @Override
        public final int size() {
            return keys.length();
        }

        @Override
        public boolean contains(K key) {
            return values.containsKey(key);
        }

        @Override
        public final Stream<K> keys() {
            return keys.stream().values();
        }

        @Override
        public final K getKey(int index) {
            return keys.getValue(index);
        }

        @Override
        public final double getDouble(K key) {
            return values.get(key);
        }

        @Override
        public final double getDoubleAt(int index) {
            if (temp != null) {
                return temp[index];
            } else {
                var key = keys.getValue(index);
                return values.get(key);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new double[keys.length()];
                keys.forEachValue(v -> temp[v.index()] = values.get(v.getValue()));
                SortAlgorithm.getDefault(isParallel()).sort(0, size(), comparator, this);
            } finally {
                this.temp = null;
            }
        }

        @Override
        public final void swap(int i, int j) {
            this.keys.swap(i, j);
            if (temp != null) {
                var d1 = temp[i];
                var d2 = temp[j];
                this.temp[i] = d2;
                this.temp[j] = d1;
            }
        }
    }


    /**
     * An IntCoding implementation of the DoubleSeries interface
     * @param <K>   the key type
     */
    class IntCodingDoubleSeries<K> extends DoubleSeriesBase<K> implements Swapper {

        private IntCoding<K> coding;
        private Array<K> keys;
        private TIntDoubleMap values;
        private double[] temp;

        /**
         * Constructor
         * @param coding    the coding instance
         * @param keys      the keys for series
         * @param values    the values for series
         */
        IntCodingDoubleSeries(
            @lombok.NonNull IntCoding<K> coding,
            @lombok.NonNull Array<K> keys,
            @lombok.NonNull TIntDoubleMap values) {
            super(keys.type());
            this.coding = coding;
            this.keys = keys;
            this.values = values;
        }

        @Override
        public final int size() {
            return values.size();
        }

        @Override
        public boolean contains(K key) {
            return values.containsKey(coding.getCode(key));
        }

        @Override
        public final Stream<K> keys() {
            return keys.stream().values();
        }

        @Override
        public final K getKey(int index) {
            return keys.getValue(index);
        }

        @Override
        public final double getDouble(K key) {
            return values.get(coding.getCode(key));
        }

        @Override
        public final double getDoubleAt(int index) {
            if (temp != null) {
                return temp[index];
            } else {
                var key = keys.getInt(index);
                return values.get(key);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new double[keys.length()];
                keys.forEachValue(v -> temp[v.index()] = values.get(v.getInt()));
                SortAlgorithm.getDefault(isParallel()).sort(0, size(), comparator, this);
            } finally {
                this.temp = null;
            }
        }

        @Override
        public final void swap(int i, int j) {
            this.keys.swap(i, j);
            if (temp != null) {
                var d1 = temp[i];
                var d2 = temp[j];
                this.temp[i] = d2;
                this.temp[j] = d1;
            }
        }
    }


    /**
     * An LongCoding implementation of the DoubleSeries interface
     * @param <K>   the key type
     */
    class LongCodingDoubleSeries<K> extends DoubleSeriesBase<K> implements Swapper {

        private Array<K> keys;
        private LongCoding<K> coding;
        private TLongDoubleMap values;
        private double[] temp;

        /**
         * Constructor
         * @param coding    the coding instance
         * @param keys      the keys for series
         * @param values    the values for series
         */
        LongCodingDoubleSeries(
            @lombok.NonNull LongCoding<K> coding,
            @lombok.NonNull Array<K> keys,
            @lombok.NonNull TLongDoubleMap values) {
            super(keys.type());
            this.coding = coding;
            this.keys = keys;
            this.values = values;
        }

        @Override
        public final int size() {
            return values.size();
        }

        @Override
        public boolean contains(K key) {
            return values.containsKey(coding.getCode(key));
        }

        @Override
        public final Stream<K> keys() {
            return keys.stream().values();
        }

        @Override
        public final K getKey(int index) {
            return keys.getValue(index);
        }

        @Override
        public final double getDouble(K key) {
            return values.get(coding.getCode(key));
        }

        @Override
        public final double getDoubleAt(int index) {
            if (temp != null) {
                return temp[index];
            } else {
                var key = keys.getLong(index);
                return values.get(key);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new double[keys.length()];
                keys.forEachValue(v -> temp[v.index()] = values.get(v.getLong()));
                SortAlgorithm.getDefault(isParallel()).sort(0, size(), comparator, this);
            } finally {
                this.temp = null;
            }
        }

        @Override
        public final void swap(int x, int y) {
            this.keys.swap(x, y);
            if (this.temp != null) {
                var d1 = temp[x];
                var d2 = temp[y];
                this.temp[x] = d2;
                this.temp[y] = d1;
            }
        }
    }




    static void main(String[] args) {
        var size = 10000000;
        var builder = DoubleSeries.builder(Integer.class).capacity(size);
        IntStream.range(0, size).forEach(i -> builder.putDouble(i, Math.random() * 100d));
        var series = builder.build().parallel();
        for (int i=0; i<10; ++i) {
            ((IntCodingDoubleSeries)series).keys.shuffle(5);
            var time2 = StopWatch.time(() -> series.sort((i1, i2) -> {
                var d1 = series.getDoubleAt(i1);
                var d2 = series.getDoubleAt(i2);
                return Double.compare(d1, d2);
            }));
            IO.println("Series Sort " + size + " entries in " + time2 + " millis");
        }
    }
}