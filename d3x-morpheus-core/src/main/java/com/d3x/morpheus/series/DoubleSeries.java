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
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.stats.Stats;
import com.d3x.morpheus.util.AssertException;
import com.d3x.morpheus.util.GenericType;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.IntComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.util.Resource;

/**
 * An interface to an immutable series of doubles stored against a unique key
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author Xavier Witdouck
 */
public interface DoubleSeries<K> extends DataSeries<K,Double> {

    /**
     * Returns the value for key, NaN if no match
     * @param key   the entry key
     * @return      the value for key, NaN if no match
     */
    double getDouble(K key);


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
        var value = getDouble(key);
        return Double.isNaN(value) ? 0d : value;
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
        if (keys.size() != values.size())
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

    /**
     * Computes the inner product of two series, assuming that missing
     * series values are {@code 0.0}, not {@code Double.NaN}.
     *
     * @param <K> the runtime type for the series.
     * @param s1  the first series in the inner product.
     * @param s2  the second series in the inner product.
     *
     * @return the inner product of the two series.
     */
    static <K> double innerProduct(DoubleSeries<K> s1, DoubleSeries<K> s2) {
        return InnerProduct.compute(s1, s2);
    }

    /**
     * Computes a weighted inner product of two series, assuming that
     * missing series values are {@code 0.0}, not {@code Double.NaN}.
     *
     * @param <K> the runtime type for the series.
     * @param s1  the first series in the inner product.
     * @param s2  the second series in the inner product.
     * @param wt  the weights to apply to each term in the inner product.
     *
     * @return the inner product of the two series.
     */
    static <K> double innerProduct(DoubleSeries<K> s1, DoubleSeries<K> s2, DoubleSeries<K> wt) {
        return InnerProduct.compute(s1, s2, wt);
    }
}
