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

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.stats.Stats;
import com.d3x.morpheus.util.AssertException;
import com.d3x.morpheus.util.Collect;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.GenericType;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.IntComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.util.Resource;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.D3xVectorView;
import com.d3x.morpheus.vector.DataVectorView;

/**
 * An interface to an immutable series of doubles stored against a unique key
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author Xavier Witdouck
 */
public interface DoubleSeries<K> extends DataSeries<K,Double>, D3xVectorView, DataVectorView<K> {

    /**
     * Returns the value for key, NaN if no match
     * @param key   the entry key
     * @return      the value for key, NaN if no match
     */
    double getDouble(K key);

    /**
     * Returns the value mapped to a particular key or a default
     * value for missing keys.
     *
     * @param key          the key of the desired value.
     * @param defaultValue the default value to use for missing keys.
     *
     * @return the value mapped to the specified key, or the default
     * value is this series does not contain the key.
     */
    default double getDouble(K key, double defaultValue) {
        double value = getDouble(key);
        return Double.isNaN(value) ? defaultValue : value;
    }

    @Override default boolean containsElement(K key) {
        return contains(key);
    }

    @Override
    default double getElement(K key, double defaultValue) {
        return getDouble(key, defaultValue);
    }

    @Override
    default Double getValue(K key) {
        return getDouble(key);
    }

    @Override
    default Double getValueAt(int index) {
        return getDoubleAt(index);
    }

    @Override
    default Class<Double> valueClass() {
        return Double.class;
    }

    @Override
    default boolean isNull(K key) {
        return Double.isNaN(getDouble(key));
    }

    @Override
    default boolean isNullAt(int index) {
        return Double.isNaN(getDoubleAt(index));
    }

    @Override
    default int length() {
        return size();
    }

    @Override
    default double get(int index) {
        return getValueAt(index);
    }

    @Override
    default Stream<K> streamKeys() {
        return keys();
    }

    @Override
    default DoubleStream streamValues() {
        return toDoubles();
    }

    /**
     * Returns a sequential version of this series
     * @return      the sequential series
     */
    default DoubleSeries<K> sequential() {
        return this;
    }

    /**
     * Returns a parallel version of this series, if supported
     * @return      the parallel series, if supported
     */
    default DoubleSeries<K> parallel() {
        return this;
    }

    /**
     * Returns the value for the index specified
     * @param index     the index to access
     * @return          the value at index
     */
    default double getDoubleAt(int index) {
        return getDouble(getKey(index));
    }

    /**
     * Returns the stream of doubles for this series
     * @return      the stream of doubles
     */
    default DoubleStream toDoubles() {
        return keys().mapToDouble(this::getDouble);
    }

    /**
     * Returns the stats interface to this series
     * @return  the stats interface to series
     */
    default Stats<Double> stats() {
        return Stats.of(this::toDoubles);
    }


    /**
     * Returns the value for asset, zero if no match for asset
     * @param key   the item identifier
     * @return      the value for item, zero if no match
     */
    default double getDoubleOrZero(K key) {
        return getDouble(key, 0.0);
    }

    /**
     * Returns the value mapped to a particular key, which must exist.
     *
     * @param key the key of the required value.
     *
     * @return the value mapped to the specified key.
     *
     * @throws RuntimeException unless this series contains the given key.
     */
    default double getRequired(K key) {
        double value = getDouble(key);

        if (Double.isNaN(value))
            throw new MorpheusException("Missing series entry for key [%s].", key);
        else
            return value;
    }

    /**
     * Returns the values mapped to a list of keys, which must exist.
     *
     * @param keys the keys of the required values.
     *
     * @return the values mapped to the specified keys.
     *
     * @throws RuntimeException unless this series contains all keys.
     */
    default D3xVector getRequired(List<K> keys) {
        D3xVector vector = D3xVector.dense(keys.size());

        for (int index = 0; index < keys.size(); ++index)
            vector.set(index, getRequired(keys.get(index)));

        return vector;
    }

    /**
     * Returns a deep copy of this series
     * @return  a deep copy of series
     */
    @SuppressWarnings("unchecked")
    default DoubleSeries<K> copy() {
        if (isEmpty()) {
            var keyClass = keyClass();
            return DoubleSeries.empty(keyClass);
        } else {
            var keyType = (Class<K>)getKey(0).getClass();
            var builder = DoubleSeries.builder(keyType).capacity(size());
            this.forEach(builder::putValue);
            return builder.build();
        }
    }


    /**
     * Iterates over all entries in this map
     * @param consumer  the consumer to accept entries
     */
    default void forEach(BiConsumer<K> consumer) {
        keys().forEach(key -> {
            var value = getDouble(key);
            consumer.accept(key, value);
        });
    }


    /**
     * Returns a mapping of this data into a new keys
     * @param type      the target key type
     * @param mapper    the mapper to apply
     * @return          the mapped vector
     */
    default <V> DoubleSeries<V> mapKeys(Class<V> type, Function<K,V> mapper) {
        var length = this.size();
        var builder = DoubleSeries.builder(type).capacity(length);
        this.keys().forEach(key -> {
            var value = getDouble(key);
            var targetId = mapper.apply(key);
            builder.putValue(targetId, value);
        });
        return builder.build();
    }


    /**
     * Returns a filtered version of this series
     * @param predicate the predicate to filter series
     * @return          the filtered series
     */
    default DoubleSeries<K> filter(Predicate<K> predicate) {
        var length = this.size();
        var keyType = this.keyClass();
        var builder = DoubleSeries.builder(keyType).capacity(length);
        this.keys().filter(predicate).forEach(key -> {
            var value = getDouble(key);
            builder.putDouble(key, value);
        });
        return builder.build();
    }


    /**
     * Writes these asset values to a CSV file
     * @param file  the file to write to
     * @throws IOException  if there is an I/O exception
     */
    default void writeCsv(File file) throws IOException {
        this.writeCsv(IO.toFile(file));
    }


    /**
     * Writes these asset values to a CSV output stream
     * @param os        the output stream to write to
     * @throws IOException  if there is an I/O exception
     */
    default void writeCsv(OutputStream os) throws IOException {
        BufferedWriter writer = null;
        try {
            var keys = keys().iterator();
            writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(os)));
            writer.write("Key,Value");
            while (keys.hasNext()) {
                var key = keys.next();
                var value = getDouble(key);
                writer.newLine();
                writer.write(String.format("%s,%s", key, value));
            }
        } finally {
            IO.close(writer);
        }
    }

    /**
     * Identifies series with the same keys and values as this series.
     *
     * @param that a series to test for equality.
     *
     * @return {@code true} iff the input series and this series contain
     * exactly the same keys and the corresponding values match within the
     * tolerance of the default DoubleComparator.
     */
    default boolean equalsSeries(DoubleSeries<K> that) {
        if (this.size() != that.size())
            return false;

        Collection<K> thisKeySet = Collect.collect(new HashSet<K>(size()), this.keys());
        Collection<K> thatKeySet = Collect.collect(new HashSet<K>(size()), that.keys());

        if (!thisKeySet.equals(thatKeySet))
            return false;

        for (K key : thisKeySet) {
            double thisValue = this.getDouble(key);
            double thatValue = that.getDouble(key);

            if (!DoubleComparator.DEFAULT.equals(thisValue, thatValue))
                return false;
        }

        return true;
    }

    /**
     * Returns a newly created double series based on the args
     * @param keyType   the key type
     * @param consumer  the consumer to receive builder
     * @param <K>       the series type
     * @return          the resulting series
     */
    static <K> DoubleSeries<K> of(Class<K> keyType, Consumer<DoubleSeriesBuilder<K>> consumer) {
        var builder = DoubleSeries.builder(keyType);
        consumer.accept(builder);
        return builder.build();
    }


    /**
     * Returns a newly created double series based on the args
     * @param keyType   the key type
     * @param keys      the input keys
     * @param values    the value generating function
     * @param <K>       the series type
     * @return          newly created double series
     */
    static <K> DoubleSeries<K> of(Class<K> keyType, Stream<K> keys, ToDoubleFunction<K> values) {
        var builder = DoubleSeries.builder(keyType);
        keys.forEach(key -> builder.putDouble(key, values.applyAsDouble(key)));
        return builder.build();
    }


    /**
     * Returns a newly created double series based on the args
     * @param keyType   the key type
     * @param keys      the input keys
     * @param values    the value generating function
     * @param <K>       the key type
     * @return          newly created double series
     */
    static <K> DoubleSeries<K> of(Class<K> keyType, Iterable<K> keys, ToDoubleFunction<K> values) {
        var builder = DoubleSeries.builder(keyType);
        keys.forEach(key -> builder.putDouble(key, values.applyAsDouble(key)));
        return builder.build();
    }

    /**
     * Creates a new series with all values equal to a constant.
     *
     * @param keyClass the runtime key type.
     * @param keys     the keys to include in the series.
     * @param value    the constant value for all entries.
     *
     * @return a new series with each key mapped to {@code value}.
     */
    static <K> DoubleSeries<K> of(Class<K> keyClass, Iterable<K> keys, double value) {
        return of(keyClass, keys, key -> value);
    }

    /**
     * Creates a new series with all values equal to {@code 1.0}.
     *
     * @param keyClass the runtime key type.
     * @param keys     the keys to include in the series.
     *
     * @return a new series with each key mapped to the value {@code 1.0}.
     */
    static <K> DoubleSeries<K> ones(Class<K> keyClass, Iterable<K> keys) {
        return of(keyClass, keys, 1.0);
    }

    /**
     * Creates a new series with all values equal to {@code 0.0}.
     *
     * @param keyClass the runtime key type.
     * @param keys     the keys to include in the series.
     *
     * @return a new series with each key mapped to the value {@code 0.0}.
     */
    static <K> DoubleSeries<K> zeros(Class<K> keyClass, Iterable<K> keys) {
        return of(keyClass, keys, 0.0);
    }

    /**
     * Creates a new DoubleSeries from a map.
     *
     * @param <K>    the runtime key type.
     * @param map a mapping from key to double value.
     *
     * @return a new DoubleSeries containing the same mapping as the input map.
     */
    static <K> DoubleSeries<K> build(Class<K> keyType, Map<K, Double> map) {
        return of(keyType, map.keySet(), map::get);
    }

    /**
     * Creates a new DoubleSeries from lists of keys and values.
     *
     * @param <K>    the runtime key type.
     * @param keys   the series keys.
     * @param values the series values.
     *
     * @return a new DoubleSeries containing the non-{@code NaN} entries
     * from the specified key and value lists.
     *
     * @throws RuntimeException unless the keys and values have equal sizes.
     */
    static <K> DoubleSeries<K> build(Class<K> keyType, List<K> keys, List<Double> values) {
        return build(keyType, keys, D3xVectorView.of(values));
    }

    /**
     * Creates a new DoubleSeries from a list of keys and a vector of values.
     *
     * @param <K>    the runtime key type.
     * @param keys   the series keys.
     * @param values the series values.
     *
     * @return a new DoubleSeries containing the non-{@code NaN} entries
     * from the specified keys and values.
     *
     * @throws RuntimeException unless the keys and values have equal sizes.
     */
    static <K> DoubleSeries<K> build(Class<K> keyType, List<K> keys, D3xVectorView values) {
        if (keys.size() != values.length())
            throw new MorpheusException("Key/value length mismatch.");

        DoubleSeriesBuilder<K> builder = builder(keyType);
        builder.capacity(keys.size());

        for (int index = 0; index < keys.size(); ++index) {
            K key = keys.get(index);
            double value = values.get(index);

            if (!Double.isNaN(value))
                builder.putDouble(key, value);
        }

        return builder.build();
    }

    /**
     * Returns a new DoubleSeries Builder for key type
     * @param keyType   the key type
     * @return          the builder
     */
    static <K> DoubleSeriesBuilder<K> builder(Class<K> keyType) {
        return DoubleSeriesBuilder.builder(keyType);
    }

    /**
     * Returns a parameterized type to represent double map with key type
     * @param keyType   the parameterized type for double map
     * @return          the parameterized type
     */
    static ParameterizedType ofType(Class<?> keyType) {
        return GenericType.of(DoubleSeries.class, keyType, Double.class);
    }

    /**
     * Returns a CSV data series read adapter for resource
     * @param file  the resource to read from
     * @param <K>   the key type for series
     * @return      the CSV read adapter
     */
    static <K> DataSeriesRead<K,Double,DoubleSeries<K>> read(File file) {
        return new DataSeriesRead<>(Resource.of(file));
    }

    /**
     * Returns a CSV data series read adapter for resource
     * @param url   the resource to read from
     * @param <K>   the key type for series
     * @return      the CSV read adapter
     */
    static <K> DataSeriesRead<K,Double,DoubleSeries<K>> read(URL url) {
        return new DataSeriesRead<>(Resource.of(url));
    }

    /**
     * Returns a CSV data series read adapter for resource
     * @param path  the resource to read from
     * @param <K>   the key type for series
     * @return      the CSV read adapter
     */
    static <K> DataSeriesRead<K,Double,DoubleSeries<K>> read(String path) {
        return new DataSeriesRead<>(Resource.of(path));
    }

    /**
     * Returns a CSV data series read adapter for resource
     * @param is    the resource to read from
     * @param <K>   the key type for series
     * @return      the CSV read adapter
     */
    static <K> DataSeriesRead<K,Double,DoubleSeries<K>> read(InputStream is) {
        return new DataSeriesRead<>(Resource.of(is));
    }

    /**
     * Returns series from the column values in a DataFrame
     * @param frame     the DataFrame to extract values from
     * @param colKey    the column key to extract
     * @return          the the does series
     */
    static <R,C> DoubleSeries<R> from(DataFrame<R,C> frame, C colKey) {
        frame.requireNumericColumn(colKey);

        var keyType = frame.rows().keyClass();
        var result = DoubleSeries.builder(keyType);
        var col = frame.cols().ordinal(colKey);
        frame.rows().forEach(row -> {
            var key = row.key();
            var value = row.getDoubleAt(col);
            if (!Double.isNaN(value)) {
                result.putValue(key, value);
            }
        });
        return result.build();
    }

    /**
     * Returns an empty dataset of asset values
     * @param keyType   the key type
     * @return      the empty dataset
     */
    static <K> DoubleSeries<K> empty(Class<K> keyType) {
        return new DoubleSeries<>() {
            @Override
            public int size() {
                return 0;
            }
            @Override
            public K getKey(int index) {
                return null;
            }
            @Override
            public Class<K> keyClass() {
                return keyType;
            }
            @Override
            public Stream<K> keys() {
                return Stream.empty();
            }
            @Override
            public boolean contains(K key) {
                return false;
            }
            @Override
            public boolean isNull(K key) {
                return false;
            }
            @Override
            public boolean isNullAt(int index) {
                return false;
            }
            @Override
            public double getDouble(K key) {
                return Double.NaN;
            }
            @Override
            public void sort(IntComparator comparator) {

            }
        };
    }


    /**
     * Asserts that the series is sorted in ascending order
     * @param series    the series to check sort order
     * @throws AssertException if series not sorted
     */
    static void assertAscending(DoubleSeries<?> series) {
        for (int i=1; i<series.size(); ++i) {
            var v1 = series.getDoubleAt(i-1);
            var v2 = series.getDoubleAt(i);
            if (v1 > v2) {
                throw new AssertException("Series not sorted at " + i);
            }
        }
    }


    /**
     * A consumer for entries of a DoubleSeries
     * @param <K>   the key type
     */
    interface BiConsumer<K> {

        void accept(K key, double value);
    }
}
