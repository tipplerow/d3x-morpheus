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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.coding.IntCoding;
import com.d3x.morpheus.array.coding.LongCoding;
import com.d3x.morpheus.util.IntComparator;
import com.d3x.morpheus.util.SortAlgorithm;
import com.d3x.morpheus.util.Swapper;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

/**
 * An interface to a Builder for DataSeries
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @param <K>   the series key
 */
public interface DataSeriesBuilder<K,V> {

    float DEFAULT_LOAD_FACTOR = 0.85f;

    /**
     * Returns a newly created DoubleSeries from the state of this builder
     * @return      the newly created DoubleSeries
     */
    DataSeries<K,V> build();

    /**
     * Sets the initial capacity for builder
     * @param capacity  the initial capacity
     * @return          this builder
     */
    DataSeriesBuilder<K,V> capacity(int capacity);

    /**
     * Puts an entry in this builder against the key
     * @param key       the entry key
     * @param value     the entry value
     * @return          this builder
     */
    @SuppressWarnings("unchecked")
    default DataSeriesBuilder<K,V> putBoolean(@lombok.NonNull K key, boolean value) {
        return this.putValue(key, (V)Boolean.valueOf(value));
    }

    /**
     * Puts an entry in this builder against the key
     * @param key       the entry key
     * @param value     the entry value
     * @return          this builder
     */
    @SuppressWarnings("unchecked")
    default DataSeriesBuilder<K,V> putInt(@lombok.NonNull K key, int value) {
        return this.putValue(key, (V)Integer.valueOf(value));
    }

    /**
     * Puts an entry in this builder against the key
     * @param key       the entry key
     * @param value     the entry value
     * @return          this builder
     */
    @SuppressWarnings("unchecked")
    default DataSeriesBuilder<K,V> putLong(@lombok.NonNull K key, long value) {
        return this.putValue(key, (V)Long.valueOf(value));
    }

    /**
     * Puts an entry in this builder against the key
     * @param key       the entry key
     * @param value     the entry value
     * @return          this builder
     */
    @SuppressWarnings("unchecked")
    default DataSeriesBuilder<K,V> putDouble(@lombok.NonNull K key, double value) {
        return this.putValue(key, (V)Double.valueOf(value));
    }

    /**
     * Puts an entry in this builder against the key
     * @param key       the entry key
     * @param value     the entry value
     * @return          this builder
     */
    default DataSeriesBuilder<K,V> putValue(@lombok.NonNull K key, V value) {
        throw new UnsupportedOperationException("Type not supported by this builder");
    }


    /**
     * Returns a new DoubleSeries Builder for key type
     * @param keyType   the key type
     * @return          the builder
     */
    @SuppressWarnings("unchecked")
    static <K,V> DataSeriesBuilder<K,V> builder(Class<K> keyType, Class<V> valueType) {
        if (valueType.equals(Double.class)) {
            return (DataSeriesBuilder<K,V>)DoubleSeriesBuilder.builder(keyType);
        } else if (LongCoding.Support.includes(keyType)) {
            var coding = LongCoding.Support.getCoding(keyType).orNull();
            return new LongCodingDataSeriesBuilder<>(keyType, valueType, coding);
        } else if (IntCoding.Support.includes(keyType)) {
            var coding = IntCoding.Support.getCoding(keyType).orNull();
            return new IntCodingDataSeriesBuilder<>(keyType, valueType, coding);
        } else {
            return new DefaultDataSeriesBuilder<>(keyType, valueType);
        }
    }


    /**
     * A Builder for DefaultSeries
     */
    class DefaultDataSeriesBuilder<K,V> implements DataSeriesBuilder<K,V> {

        private Class<K> keyType;
        private Class<V> valueType;
        private ArrayBuilder<K> keys;
        private Map<K,V> values;

        /**
         * Constructor
         * @param keyType   the key type
         * @param valueType the value type
         */
        DefaultDataSeriesBuilder(Class<K> keyType, Class<V> valueType) {
            this.keyType = keyType;
            this.valueType = valueType;
        }

        @Override()
        public DataSeries<K,V> build() {
            this.capacity(100);
            return new DefaultDataSeries<>(valueType, keys.toArray(), values);
        }

        @Override()
        public DataSeriesBuilder<K,V> capacity(int capacity) {
            if (values != null) {
                return this;
            } else {
                this.keys = ArrayBuilder.of(capacity, keyType);
                this.values = new HashMap<>(capacity);
                return this;
            }
        }

        @Override()
        public DataSeriesBuilder<K,V> putValue(@lombok.NonNull K key, V value) {
            this.capacity(100);
            this.keys.add(key);
            this.values.put(key, value);
            return this;
        }
    }




    /**
     * A Builder for IntCoding Series
     */
    @lombok.AllArgsConstructor()
    class IntCodingDataSeriesBuilder<K,V> implements DataSeriesBuilder<K,V> {

        private Class<K> keyType;
        private Class<V> valueType;
        private IntCoding<K> coding;
        private ArrayBuilder<K> keys;
        private TIntObjectMap<V> values;

        /**
         * Constructor
         * @param keyType   the key type
         * @param coding    the coding instance
         */
        IntCodingDataSeriesBuilder(
            @lombok.NonNull Class<K> keyType,
            @lombok.NonNull Class<V> valueType,
            @lombok.NonNull IntCoding<K> coding) {
            this.keyType = keyType;
            this.valueType = valueType;
            this.coding = coding;
        }

        @Override
        public DataSeries<K,V> build() {
            this.capacity(100);
            return new IntCodingDataSeries<>(valueType, coding, keys.toArray(), values);
        }

        @Override
        public DataSeriesBuilder<K,V> capacity(int capacity) {
            if (values != null) {
                return this;
            } else {
                this.keys = ArrayBuilder.of(capacity, keyType);
                this.values = new TIntObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR);
                return this;
            }
        }

        @Override
        public DataSeriesBuilder<K,V> putValue(K key, V value) {
            this.capacity(100);
            this.keys.add(key);
            this.values.put(coding.getCode(key), value);
            return this;
        }
    }




    /**
     * A Builder for LongCoding data series
     */
    @lombok.AllArgsConstructor()
    class LongCodingDataSeriesBuilder<K,V> implements DataSeriesBuilder<K,V> {

        private Class<K> keyType;
        private Class<V> valueType;
        private LongCoding<K> coding;
        private ArrayBuilder<K> keys;
        private TLongObjectMap<V> values;

        /**
         * Constructor
         * @param keyType   the key type
         * @param coding    the coding instance
         */
        LongCodingDataSeriesBuilder(
            @lombok.NonNull Class<K> keyType,
            @lombok.NonNull Class<V> valueType,
            @lombok.NonNull LongCoding<K> coding) {
            this.keyType = keyType;
            this.valueType = valueType;
            this.coding = coding;
        }

        @Override
        public DataSeries<K,V> build() {
            this.capacity(100);
            return new LongCodingDataSeries<>(valueType, coding, keys.toArray(), values);
        }

        @Override
        public DataSeriesBuilder<K,V> capacity(int capacity) {
            if (values != null) {
                return this;
            } else {
                this.keys = ArrayBuilder.of(capacity, keyType);
                this.values = new TLongObjectHashMap<>(capacity, DEFAULT_LOAD_FACTOR);
                return this;
            }
        }

        @Override
        public DataSeriesBuilder<K,V> putValue(K key, V value) {
            this.capacity(100);
            this.keys.add(key);
            this.values.put(coding.getCode(key), value);
            return this;
        }
    }




    /**
     * A convenience base class for DataSeries implementations
     * @param <K>   the key type
     */
    abstract class DataSeriesBase<K,V> implements DataSeries<K,V> {

        private boolean parallel;
        private Class<K> keyClass;
        private Class<V> valueClass;

        /**
         * Constructor
         * @param keyClass      the key class
         * @param valueClass    the value class
         */
        DataSeriesBase(Class<K> keyClass, Class<V> valueClass) {
            this.keyClass = keyClass;
            this.valueClass = valueClass;
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
        public Class<V> valueClass() {
            return valueClass;
        }

        @SuppressWarnings("unchecked")
        public DataSeries<K,V> sequential() {
            try {
                if (!isParallel()) {
                    return this;
                } else {
                    var clone = (DataSeriesBase<K,V>)super.clone();
                    clone.parallel = false;
                    return clone;
                }
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }


        @SuppressWarnings("unchecked")
        public DataSeries<K,V> parallel() {
            try {
                if (isParallel()) {
                    return this;
                } else {
                    var clone = (DataSeriesBase<K,V>)super.clone();
                    clone.parallel = true;
                    return clone;
                }
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        }
    }




    /**
     * The default implementation of DataSeries interface
     * @param <K>   the key type
     */
    class DefaultDataSeries<K,V> extends DataSeriesBase<K,V> implements Swapper {

        private Array<K> keys;
        private Map<K,V> values;
        private Object[] temp;

        /**
         * Constructor
         * @param valueClass    the value class
         * @param keys          the keys
         * @param values        the values
         */
        DefaultDataSeries(
            @lombok.NonNull Class<V> valueClass,
            @lombok.NonNull Array<K> keys,
            @lombok.NonNull Map<K,V> values) {
            super(keys.type(), valueClass);
            this.keys = keys;
            this.values = values;
        }

        @Override
        public final int size() {
            return keys.length();
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
        public final V getValue(K key) {
            return values.get(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        public final V getValueAt(int index) {
            if (temp != null) {
                return (V)temp[index];
            } else {
                var key = keys.getValue(index);
                return values.get(key);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new Object[keys.length()];
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
    class IntCodingDataSeries<K,V> extends DataSeriesBase<K,V> implements Swapper {

        private IntCoding<K> coding;
        private Array<K> keys;
        private TIntObjectMap<V> values;
        private Object[] temp;

        /**
         * Constructor
         * @param coding    the coding instance
         * @param keys      the keys for series
         * @param values    the values for series
         */
        IntCodingDataSeries(
            @lombok.NonNull Class<V> valueClass,
            @lombok.NonNull IntCoding<K> coding,
            @lombok.NonNull Array<K> keys,
            @lombok.NonNull TIntObjectMap<V> values) {
            super(coding.getType(), valueClass);
            this.coding = coding;
            this.keys = keys;
            this.values = values;
        }

        @Override
        public final int size() {
            return values.size();
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
        public final V getValue(K key) {
            return values.get(coding.getCode(key));
        }

        @Override
        @SuppressWarnings("unchecked")
        public final V getValueAt(int index) {
            if (temp != null) {
                return (V)temp[index];
            } else {
                var key = keys.getInt(index);
                return values.get(key);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new Object[keys.length()];
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
    class LongCodingDataSeries<K,V> extends DataSeriesBase<K,V> implements Swapper {

        private Array<K> keys;
        private LongCoding<K> coding;
        private TLongObjectMap<V> values;
        private Object[] temp;

        /**
         * Constructor
         * @param coding    the coding instance
         * @param keys      the keys for series
         * @param values    the values for series
         */
        LongCodingDataSeries(
            @lombok.NonNull Class<V> valueClass,
            @lombok.NonNull LongCoding<K> coding,
            @lombok.NonNull Array<K> keys,
            @lombok.NonNull TLongObjectMap<V> values) {
            super(coding.getType(), valueClass);
            this.coding = coding;
            this.keys = keys;
            this.values = values;
        }

        @Override
        public final int size() {
            return values.size();
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
        public final V getValue(K key) {
            return values.get(coding.getCode(key));
        }

        @Override
        @SuppressWarnings("unchecked")
        public final V getValueAt(int index) {
            if (temp != null) {
                return (V)temp[index];
            } else {
                var key = keys.getLong(index);
                return values.get(key);
            }
        }

        @Override
        public final void sort(IntComparator comparator) {
            try {
                this.temp = new Object[keys.length()];
                keys.forEachValue(v -> temp[v.index()] = values.get(v.getLong()));
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

}
