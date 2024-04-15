/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.vector;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.stats.SumSquares;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;

import lombok.NonNull;

/**
 * Provides a read-only view of {@code double} values that are accessed
 * by a fixed set of keys.
 *
 * @param <K> the runtime key type.
 *
 * @author Scott Shaffer
 */
public interface DataVectorView<K> {
    /**
     * Determines whether this view contains a particular element.
     *
     * @param key the key of interest.
     *
     * @return {@code true} iff this view contains an element indexed
     * by the specified key.
     */
    boolean containsElement(K key);

    /**
     * Extracts an element from this view.
     *
     * @param elementKey   the key of the desired element.
     * @param defaultValue a default value to return if this view
     *                     does not contain the specified element.
     *
     * @return the value of the element indexed by the specified
     * key, or the default value if the key is not present.
     */
    double getElement(K elementKey, double defaultValue);

    /**
     * Returns the number of elements in this view.
     *
     * @return the number of elements in this view.
     */
    int length();

    /**
     * Returns a stream containing the keys in this view.
     *
     * @return a stream containing the keys in this view.
     */
    Stream<K> streamKeys();

    /**
     * Returns a stream containing the values in this view.
     *
     * @return a stream containing the values in this view.
     */
    DoubleStream streamValues();

    /**
     * Returns an empty view.
     * @return an empty view.
     */
    @SuppressWarnings("unchecked")
    static <K> DataVectorView<K> empty() {
        return (DataVectorView<K>) EmptyVectorView.INSTANCE;
    }

    /**
     * Returns a DataVectorView of a Double map; changes to the
     * underlying map will be reflected in the returned vector.
     *
     * @param map the underlying Double map.
     *
     * @return a DataVectorView of the specified Double map.
     */
    static <K> DataVectorView<K> of(Map<K, Double> map) {
        return new MapDataVector<>(map);
    }

    /**
     * Creates a new DoubleSeries by copying values from a vector view and
     * padding any missing values with a default value.
     *
     * @param <K>      the runtime key type.
     * @param view     the vector view to copy from.
     * @param padding  the default value to use
     */
    static <K> DataVectorView<K> pad(DataVectorView<K> view, Iterable<K> keys, double padding) {
        DataVector<K> vector = new MapDataVector<>();

        for (K key : keys)
            vector.setElement(key, view.getElement(key, padding));

        return vector;
    }

    /**
     * Returns a collection view of the elements in this vector (in no particular
     * order); the collection may be modified, but changes will not be reflected
     * in this vector view.
     *
     * @return a collection view of the elements in this vector, in no particular
     * order.
     */
    default Collection<DataVectorElement<K>> collectElements() {
        return streamElements().collect(Collectors.toList());
    }

    /**
     * Returns a read-only set view of the keys in this view.
     *
     * @return a read-only set view of the keys in this view.
     */
    default Set<K> collectKeys() {
        return streamKeys().collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Writes the contents of this series to an output stream.
     *
     * @param stream where the display will appear.
     */
    default void display(PrintStream stream) {
        streamKeys().forEach(key -> stream.println(key.toString() + " => " + getElement(key)));
    }

    /**
     * Returns a read-only iterator over this vector view.
     * @return a read-only iterator over this vector view.
     */
    default PrimitiveIterator.OfDouble doubleIterator() {
        return new DataVectorIterator<>(this);
    }

    /**
     * Determines whether the keys and values in this view are equal to those in
     * another view <em>within the tolerance of the default DoubleComparator</em>.
     *
     * @param that the view to test for equality.
     *
     * @return {@code true} iff the input view has the same keys as this view and
     * each value matches the corresponding entry in this view within the tolerance
     * of the default DoubleComparator.
     */
    default boolean equalsView(DataVectorView<K> that) {
        return equalsView(that, DoubleComparator.DEFAULT);
    }

    /**
     * Determines whether the keys and values in this view are equal to those in
     * another view within the tolerance of a given DoubleComparator.
     *
     * @param that       the vector to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input view has the same keys as this view and
     * each value matches the corresponding entry in this view within the tolerance
     * of the specified comparator.
     */
    default boolean equalsView(DataVectorView<K> that, DoubleComparator comparator) {
        if (!this.collectKeys().equals(that.collectKeys()))
            return false;

        for (K key : collectKeys())
            if (!comparator.equals(this.getElement(key), that.getElement(key)))
                return false;

        return true;
    }

    /**
     * Extracts an element from this view.
     *
     * @param key the key of the desired element.
     *
     * @return the value of the element indexed by the specified
     * key, or {@code Double.NaN} if the key is not present.
     */
    default double getElement(K key) {
        return getElement(key, Double.NaN);
    }

    /**
     * Extracts a required element from this view.
     *
     * @param key the key of the desired element.
     *
     * @return the value of the element indexed by the specified key.
     *
     * @throws RuntimeException unless this vector contains an element
     * for the specified key.
     */
    default double getRequiredElement(K key) {
        requireElement(key);
        return getElement(key);
    }

    /**
     * Extracts elements from this view.
     *
     * @param elementKeys the keys of the desired elements.
     * @param defaultValue a default value to return if this view
     *                     does not contain the specified element.
     *
     * @return the value of the element indexed by the specified
     * key, or {@code Double.NaN} if the key is not present.
     */
    default D3xVector getElements(List<K> elementKeys, double defaultValue) {
        D3xVector vector = D3xVector.dense(length());

        for (int index = 0; index < elementKeys.size(); ++index)
            vector.set(index, getElement(elementKeys.get(index), defaultValue));

        return vector;
    }

    /**
     * Computes the inner (dot) product between this data vector and
     * another vector; missing values (elements not contained in both
     * vectors) are replaced with zero.
     *
     * @param that the other vector in the inner product.
     *
     * @return the inner (dot) product between this data vector and
     * the input vector.
     */
    default double innerProduct(DataVectorView<K> that) {
        return InnerProduct.compute(this, that);
    }

    /**
     * Computes the weighted inner product between this data vector and
     * another vector; missing values (elements not contained in both
     * vectors and the weight vector) are replaced with zero.
     *
     * @param operand the other vector operand in the inner product.
     * @param weights a vector of weights to apply to each term in the
     *                inner product.
     *
     * @return the weighted inner product between this data vector and
     * the input vector.
     */
    default double innerProduct(DataVectorView<K> operand, DataVectorView<K> weights) {
        return InnerProduct.compute(this, operand, weights);
    }

    /**
     * Computes the difference of this data vector and another data
     * vector and returns the result in a new data vector.
     *
     * @param subtrahend the data vector to subtract from this.
     *
     * @return the difference of this vector and the subtrahend, in a
     * new vector.
     */
    default DataVector<K> minus(DataVectorView<K> subtrahend) {
        return DataVector.combine(1.0, this, -1.0, subtrahend);
    }

    /**
     * Computes the 1-norm of this data vector: the sum of the absolute
     * values of each element.
     * @return the 1-norm of this data vector.
     */
    default double norm1() {
        return streamValues().filter(x -> !Double.isNaN(x)).map(Math::abs).sum();
    }

    /**
     * Computes the 2-norm (Euclidean norm) of this data vector.
     * @return the 2-norm (Euclidean norm) of this data vector.
     */
    default double norm2() {
        return Math.sqrt(new SumSquares().compute(this));
    }

    /**
     * Computes the sum of this data vector and another data vector
     * and returns the result in a new data vector.
     *
     * @param addend the data vector to add to this.
     *
     * @return the sum of this vector and the addend, in a new vector.
     */
    default DataVector<K> plus(DataVectorView<K> addend) {
        return DataVector.combine(1.0, this, 1.0, addend);
    }

    /**
     * Ensures that this vector contains an element for a particular key.
     *
     * @param key the key of the required element.
     *
     * @throws RuntimeException unless this vector contains an element for
     * the specified key.
     */
    default void requireElement(K key) {
        if (!containsElement(key))
            throw new MorpheusException("Missing key: [%s].", key);
    }

    /**
     * Ensures that this vector contains an element for particular keys.
     *
     * @param keys the keys of the required elements.
     *
     * @throws RuntimeException unless this vector contains an element for
     * each specified key.
     */
    default void requireElements(Iterable<K> keys) {
        for (K key : keys)
            requireElement(key);
    }

    /**
     * Ensures that this vector contains an element for particular keys.
     *
     * @param keys the keys of the required elements.
     *
     * @throws RuntimeException unless this vector contains an element for
     * each specified key.
     */
    default void requireElements(Stream<K> keys) {
        keys.forEach(this::requireElement);
    }

    /**
     * Returns the elements in this vector in a stream (in no particular order).
     *
     * @return the elements in this vector in a stream (in no particular order).
     */
    default Stream<DataVectorElement<K>> streamElements() {
        return streamKeys().map(key -> DataVectorElement.of(key, getElement(key)));
    }

    /**
     * Creates a copy of this vector as a data frame row.
     *
     * @param rowKey the key for the data frame row.
     *
     * @return the elements of this vector as a data frame row.
     */
    default <R> DataFrame<R, K> toDataFrameRow(R rowKey) {
        var frame = DataFrame.ofDoubles(rowKey, collectKeys());

        for (var element : collectElements())
            frame.setDouble(rowKey, element.getKey(), element.getValue());

        return frame;
    }

    /**
     * Creates a copy of this vector as a data frame column.
     *
     * @param colKey the key for the data frame column.
     *
     * @return the elements of this vector as a data frame column.
     */
    default <C> DataFrame<K, C> toDataFrameColumn(C colKey) {
        var frame = DataFrame.ofDoubles(collectKeys(), colKey);

        for (var element : collectElements())
            frame.setDouble(element.getKey(), colKey, element.getValue());

        return frame;
    }

    /**
     * Returns a DoubleSeries with the same elements as this view (or this view
     * itself, if it is a DoubleSeries).
     *
     * @return a DoubleSeries with the same elements as this view.
     */
    default DoubleSeries<K> toSeries(Class<K> keyClass) {
        if (this instanceof DoubleSeries)
            return (DoubleSeries<K>) this;
        else
            return DoubleSeries.copyOf(keyClass, this);
    }

    /**
     * Computes the weighted mean value of the vector elements.
     *
     * @param weights the weights to apply to each element (not necessarily normalized).
     *
     * @return the weighted mean value of the vector elements.
     */
    default double weightedMean(@NonNull DataVectorView<K> weights) {
        var numerator = new DoubleAdder();
        var denominator = new DoubleAdder();

        streamElements().forEach(element -> {
            var targetKey = element.getKey();
            var targetValue = element.getValue();
            var weightValue = weights.getElement(targetKey);

            if (Double.isFinite(weightValue)) {
                numerator.add(weightValue * targetValue);
                denominator.add(weightValue);
            }
        });

        if (!DoubleComparator.DEFAULT.isPositive(denominator.sum()))
            throw new MorpheusException("Total weight must be positive.");

        return numerator.sum() / denominator.sum();
    }
}
