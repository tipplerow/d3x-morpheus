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

import java.util.stream.Stream;

import lombok.NonNull;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.coding.IntCoding;
import com.d3x.morpheus.array.coding.LongCoding;
import com.d3x.morpheus.util.IntComparator;
import com.d3x.morpheus.util.SortAlgorithm;
import com.d3x.morpheus.util.Swapper;
import org.eclipse.collections.api.map.primitive.IntDoubleMap;
import org.eclipse.collections.api.map.primitive.LongDoubleMap;
import org.eclipse.collections.api.map.primitive.MutableIntDoubleMap;
import org.eclipse.collections.api.map.primitive.MutableLongDoubleMap;
import org.eclipse.collections.api.map.primitive.MutableObjectDoubleMap;
import org.eclipse.collections.api.map.primitive.ObjectDoubleMap;
import org.eclipse.collections.impl.factory.primitive.IntDoubleMaps;
import org.eclipse.collections.impl.factory.primitive.LongDoubleMaps;
import org.eclipse.collections.impl.factory.primitive.ObjectDoubleMaps;

/**
 * An interface to a Builder for DoubleSeries
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @param <K>   the series key
 */
public interface DoubleSeriesBuilder<K> extends DataSeriesBuilder<K,Double> {

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

    /**
     * Puts all entries from another DoubleSeries into this builder.
     * @param series the series to put into this builder.
     * @return this builder.
     */
    default DoubleSeriesBuilder<K> putSeries(@lombok.NonNull DoubleSeries<K> series) {
        series.forEach(this::putDouble);
        return this;
    }

    /**
     * Adds the value to the existing value for key, or simply puts value if no existing value
     * @param key       the entry key
     * @param value     the entry value
     * @return          this builder
     */
    DoubleSeriesBuilder<K> plusDouble(@lombok.NonNull K key, double value);



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
            var coding = LongCoding.Support.getCoding(keyType).orElse(null);
            return new LongCodingDoubleSeriesBuilder<>(keyType, coding);
        } else if (IntCoding.Support.includes(keyType)) {
            var coding = IntCoding.Support.getCoding(keyType).orElse(null);
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
        private MutableObjectDoubleMap<K> values;

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
                this.values = ObjectDoubleMaps.mutable.withInitialCapacity(capacity);
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

        @Override
        public DoubleSeriesBuilder<K> plusDouble(@NonNull K key, double value) {
            this.capacity(100);
            if (!values.containsKey(key)) {
                return putDouble(key ,value);
            } else if (!Double.isNaN(value)) {
                var existing = values.getIfAbsent(key, Double.NaN);
                var v1 = Double.isNaN(existing) ? 0d : existing;
                this.values.put(key, v1 + value);
                return this;
            } else {
                return this;
            }
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
        private MutableIntDoubleMap values;

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
                this.values = IntDoubleMaps.mutable.withInitialCapacity(capacity);
                return this;
            }
        }

        @Override
        public DoubleSeriesBuilder<K> putDouble(@NonNull K key, double value) {
            this.capacity(100);
            this.keys.append(key);
            this.values.put(coding.getCode(key), value);
            return this;
        }

        @Override
        public DoubleSeriesBuilder<K> plusDouble(@NonNull K key, double value) {
            this.capacity(100);
            var code = coding.getCode(key);
            if (!values.containsKey(code)) {
                return putDouble(key ,value);
            } else if (!Double.isNaN(value)) {
                var existing = values.getIfAbsent(code, Double.NaN);
                var v1 = Double.isNaN(existing) ? 0d : existing;
                this.values.put(code, v1 + value);
                return this;
            } else {
                return this;
            }
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
        private MutableLongDoubleMap values;

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
                this.values = LongDoubleMaps.mutable.withInitialCapacity(capacity);
                return this;
            }
        }

        @Override
        public DoubleSeriesBuilder<K> putDouble(@NonNull K key, double value) {
            this.capacity(100);
            this.keys.append(key);
            this.values.put(coding.getCode(key), value);
            return this;
        }

        @Override
        public DoubleSeriesBuilder<K> plusDouble(@NonNull K key, double value) {
            this.capacity(100);
            var code = coding.getCode(key);
            if (!values.containsKey(code)) {
                return putDouble(key ,value);
            } else if (!Double.isNaN(value)) {
                var existing = values.getIfAbsent(code, Double.NaN);
                var v1 = Double.isNaN(existing) ? 0d : existing;
                this.values.put(code, v1 + value);
                return this;
            } else {
                return this;
            }
        }
    }


    /**
     * A convenience base class for DoubleSeries implementations
     * @param <K>   the key type
     */
    abstract class DoubleSeriesBase<K> implements DoubleSeries<K> {

        private final Class<K> keyClass;
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
            var first = firstKey().orElse(null);
            var last = lastKey().orElse(null);
            return String.format("DoubleSeries, size: %s, first: %s, last: %s", size(), first, last);
        }

        @Override
        public boolean isNull(K key) {
            return Double.isNaN(getDouble(key));
        }

        @Override
        public boolean isNullAt(int index) {
            return Double.isNaN(getDoubleAt(index));
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

        private final Array<K> keys;
        private final ObjectDoubleMap<K> values;
        private double[] temp;

        /**
         * Constructor
         * @param keys      the keys
         * @param values    the values
         */
        DefaultDoubleSeries(
            @lombok.NonNull Array<K> keys,
            @lombok.NonNull ObjectDoubleMap<K> values) {
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
            return values.getIfAbsent(key, Double.NaN);
        }

        @Override
        public final double getDoubleAt(int index) {
            if (temp != null) {
                return temp[index];
            } else {
                var key = keys.getValue(index);
                return values.getIfAbsent(key, Double.NaN);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new double[keys.length()];
                keys.forEachValue(v -> temp[v.index()] = values.getIfAbsent(v.getValue(), Double.NaN));
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
        private IntDoubleMap values;
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
            @lombok.NonNull IntDoubleMap values) {
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
            return values.getIfAbsent(coding.getCode(key), Double.NaN);
        }

        @Override
        public final double getDoubleAt(int index) {
            if (temp != null) {
                return temp[index];
            } else {
                var key = keys.getInt(index);
                return values.getIfAbsent(key, Double.NaN);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new double[keys.length()];
                keys.forEachValue(v -> temp[v.index()] = values.getIfAbsent(v.getInt(), Double.NaN));
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

        private final Array<K> keys;
        private final LongCoding<K> coding;
        private final LongDoubleMap values;
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
            @lombok.NonNull LongDoubleMap values) {
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
            return values.getIfAbsent(coding.getCode(key), Double.NaN);
        }

        @Override
        public final double getDoubleAt(int index) {
            if (temp != null) {
                return temp[index];
            } else {
                var key = keys.getLong(index);
                return values.getIfAbsent(key, Double.NaN);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new double[keys.length()];
                keys.forEachValue(v -> temp[v.index()] = values.getIfAbsent(v.getLong(), Double.NaN));
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
}