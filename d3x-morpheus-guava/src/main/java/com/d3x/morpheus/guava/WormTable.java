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
package com.d3x.morpheus.guava;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import com.d3x.morpheus.concurrent.ConcurrentObject;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import lombok.NonNull;

/**
 * A Guava Table that implements a write-once, read-many (WORM) protocol
 * for assigning cell values.  The value associated with a row/column key
 * pair may be assigned at most once. A second attempt to assign the same
 * cell will trigger a runtime exception.
 *
 * <p>This implementation is synchronized.</p>
 *
 * @param <R> the runtime row type.
 * @param <C> the runtime column type.
 * @param <V> the runtime value type.
 *
 * @author Scott Shaffer
 */
public class WormTable<R, C, V> extends ConcurrentObject implements Table<R, C, V> {
    /**
     * The underlying table.
     */
    @NonNull
    protected final Table<R, C, V> delegate;

    /**
     * Creates an empty WORM table using a hash-based table for the
     * underlying storage.
     */
    public WormTable() {
        this(HashBasedTable.create());
    }

    /**
     * Creates a new WORM table using another table for the underlying
     * storage; the contents of the delegate table become the initial
     * contents of the WORM table.
     *
     * @param delegate the underlying table.
     */
    public WormTable(@NonNull Table<R, C, V> delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates an empty WORM table using a hash-based table for the
     * underlying storage.
     *
     * @param <R> the runtime row type.
     * @param <C> the runtime column type.
     * @param <V> the runtime value type.
     *
     * @return a new empty WORM table using a hash-based table for
     * the underlying storage.
     */
    public static <R, C, V> WormTable<R, C, V> hash() {
        return new WormTable<>(HashBasedTable.create());
    }

    /**
     * Creates an empty WORM table using a tree-based table for the
     * underlying storage.
     *
     * @param <R> the runtime row type.
     * @param <C> the runtime column type.
     * @param <V> the runtime value type.
     *
     * @return a new empty WORM table using a tree-based table for
     * the underlying storage.
     */
    public static <R extends Comparable<?>, C extends Comparable<?>, V> WormTable<R, C, V> tree() {
        return new WormTable<>(TreeBasedTable.create());
    }

    /**
     * Returns a read-only view of the cells in this table.
     * @return an unmodifiable set containing the cells in this table.
     */
    @Override
    public Set<Cell<R, C, V>> cellSet() {
        return read(() -> Collections.unmodifiableSet(delegate.cellSet()));
    }

    /**
     * Guaranteed to throw an exception and leave the table unmodified.
     * @throws UnsupportedOperationException always: cell removal is forbidden.
     */
    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a read-only view of a column in this table.
     *
     * @param columnKey the key of the column to view.
     *
     * @return an unmodifiable mapping for the specified column.
     */
    @Override
    public Map<R, V> column(@NonNull C columnKey) {
        return read(() -> Collections.unmodifiableMap(delegate.column(columnKey)));
    }

    /**
     * Returns a read-only view of the column keys in this table.
     * @return an unmodifiable set containing the column keys.
     */
    @Override
    public Set<C> columnKeySet() {
        return read(() -> Collections.unmodifiableSet(delegate.columnKeySet()));
    }

    /**
     * Returns a column-oriented read-only view of the mappings in this table.
     *
     * @return an unmodifiable column-oriented map for this table.
     */
    @Override
    public Map<C, Map<R, V>> columnMap() {
        return read(() -> Collections.unmodifiableMap(delegate.columnMap()));
    }

    /**
     * Identifies cells contained in this table.
     *
     * @param rowKey the row key to search for.
     * @param colKey the column key to search for.
     *
     * @return {@code true} iff the table contains a mapping with the
     * specified row and column keys.
     */
    @Override
    public boolean contains(Object rowKey, Object colKey) {
        return read(delegate::contains, rowKey, colKey);
    }

    /**
     * Identifies columns contained in this table.
     *
     * @param colKey the column key to search for.
     *
     * @return {@code true} iff the table contains a mapping with the
     * specified column key.
     */
    @Override
    public boolean containsColumn(Object colKey) {
        return read(delegate::containsColumn, colKey);
    }

    /**
     * Identifies rows contained in this table.
     *
     * @param rowKey the row key to search for.
     *
     * @return {@code true} iff the table contains a mapping with the
     * specified row key.
     */
    @Override
    public boolean containsRow(Object rowKey) {
        return read(delegate::containsRow, rowKey);
    }

    /**
     * Identifies values contained in this table.
     *
     * @param value the value to search for.
     *
     * @return {@code true} iff the table contains a mapping with the
     * specified value.
     */
    @Override
    public boolean containsValue(Object value) {
        return read(delegate::containsValue, value);
    }

    /**
     * Compares the specified object with this table for equality.
     *
     * @param obj the object to compare with this table.
     *
     * @return {@code true} iff the input object is a Guava Table with
     * identical contents.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Table && read(delegate::equals, obj);
    }

    /**
     * Retrieves a value by its row and column keys.
     *
     * @param rowKey the target row key.
     * @param colKey the target column key.
     *
     * @return the value corresponding to the given row and column keys,
     * or {@code null} if no such mapping exists.
     */
    @Override
    public V get(Object rowKey, Object colKey) {
        return read(delegate::get, rowKey, colKey);
    }

    /**
     * Retrieves an existing value from this table or assigns a default.
     *
     * @param rowKey       the row key associated with the value.
     * @param columnKey    the column key associated with the value.
     * @param defaultValue the default value to assign and return if
     *                     there is no match for the keys.
     *
     * @return the value associated with the specified keys, or the
     * default value if there is no match.
     */
    public V getOrAssign(@NonNull R rowKey,
                         @NonNull C columnKey,
                         @NonNull V defaultValue) {
        var result = get(rowKey, columnKey);

        if (result != null) {
            return result;
        }
        else {
            write(this::putIfAbsent, rowKey, columnKey, defaultValue);
            // Another thread might have acquired the write lock and assigned
            // the same cell during the interval when this thread did not hold
            // the write lock, so we must retrieve the value associated with
            // the keys again...
            return get(rowKey, columnKey);
        }
    }

    private void putIfAbsent(R rowKey, C colKey, V value) {
        // Another thread might have acquired the write lock and assigned
        // the same cell during an interval when this thread did not hold
        // the write lock.  If so, we cannot overwrite...
        if (!delegate.contains(rowKey, colKey))
            delegate.put(rowKey, colKey, value);
    }

    /**
     * Retrieves an existing value from this table or computes and
     * assigns a default.
     *
     * @param rowKey  the row key associated with the value.
     * @param colKey  the column key associated with the value.
     * @param compute the function to compute missing values.
     *
     * @return the value associated with the specified keys, or the
     * computed value if there is no match.
     */
    public V getOrCompute(@NonNull R rowKey,
                          @NonNull C colKey,
                          @NonNull BiFunction<R, C, V> compute) {
        var result = get(rowKey, colKey);

        if (result != null) {
            return result;
        }
        else {
            var value = compute.apply(rowKey, colKey);
            write(this::putIfAbsent, rowKey, colKey, value);
            // Another thread might have acquired the write lock and assigned
            // the same cell during the interval when this thread did not hold
            // the write lock, so we must retrieve the value associated with
            // the keys again...
            return get(rowKey, colKey);
        }
    }

    /**
     * Retrieves a value from this table or throws an exception.
     *
     * @param rowKey the row key associated with the value.
     * @param colKey the column key associated with the value.
     *
     * @return the value associated with the specified keys.
     *
     * @throws RuntimeException unless a value has been associated with
     * the specified keys.
     */
    public V getOrThrow(@NonNull R rowKey, @NonNull C colKey) {
        var result = get(rowKey, colKey);

        if (result != null)
            return result;
        else
            throw new NoSuchElementException(String.format("No value for row [%s] and column [%s].", rowKey, colKey));
    }

    /**
     * Returns the hash code for this table.
     * @return the hash code for this table.
     */
    @Override
    public int hashCode() {
        return read(delegate::hashCode);
    }

    /**
     * Identifies tables with no mappings.
     * @return {@code true} iff the table contains no mappings.
     */
    @Override
    public boolean isEmpty() {
        return read(delegate::isEmpty);
    }

    /**
     * Permanently assigns the contents of a cell.
     *
     * <p>This method may be called at most once with a given row/column
     * key pair.  Calling it a second time with the same key pair will
     * trigger an exception.</p>
     *
     * @param cell the cell to assign.
     *
     * @throws IllegalStateException if this table already contains a
     * value associated with the row and column key pair of the cell.
     */
    public void put(@NonNull Cell<? extends R, ? extends C, ? extends V> cell) {
        write(this::putUniqueCell, cell);
    }

    private void putUniqueCell(@NonNull Cell<? extends R, ? extends C, ? extends V> cell) {
        putUnique(Objects.requireNonNull(cell.getRowKey()),
                  Objects.requireNonNull(cell.getColumnKey()),
                  Objects.requireNonNull(cell.getValue()));
    }

    /**
     * Permanently associates the specified value with the specified keys.
     *
     * <p>This method may be called at most once with a given row/column
     * key pair.  Calling it a second time with the same key pair will
     * trigger an exception.</p>
     *
     * @param rowKey the row key to associate with the value.
     * @param colKey the column key to associate with the value.
     * @param value  the value to associate with the keys.
     *
     * @return {@code null}, because there cannot be a previous value
     * stored in the cell.
     *
     * @throws IllegalStateException if this table already contains a
     * value associated with the row and column key pair.
     */
    @Override
    public V put(@NonNull R rowKey, @NonNull C colKey, @NonNull V value) {
        write(this::putUnique, rowKey, colKey, value);
        return null;
    }

    private void putUnique(@NonNull R rowKey, @NonNull C colKey, @NonNull V value) {
        if (delegate.contains(rowKey, colKey))
            throw new IllegalStateException(String.format("Cell [%s, %s] has already been assigned.", rowKey, colKey));
        else
            delegate.put(rowKey, colKey, value);
    }

    /**
     * Permanently assigns all mappings from the input table to this table.
     *
     * @param table the table to copy.
     *
     * @throws IllegalStateException if this table already contains mappings
     * for any row/column key pairs in the input table.
     */
    @Override
    public void putAll(@NonNull Table<? extends R, ? extends C, ? extends V> table) {
        write(this::putUniqueCell, table.cellSet());
    }

    /**
     * Guaranteed to throw an exception and leave the table unmodified.
     * @throws RuntimeException always: cell removal is forbidden.
     */
    @Override
    @Deprecated
    public V remove(Object rowKey, Object colKey) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a read-only view of a row in this table.
     *
     * @param rowKey the key of the row to view.
     *
     * @return an unmodifiable mapping for the specified row.
     */
    @Override
    public Map<C, V> row(@NonNull R rowKey) {
        return read(() -> Collections.unmodifiableMap(delegate.row(rowKey)));
    }

    /**
     * Returns a read-only view of the row keys in this table.
     * @return an unmodifiable set containing the row keys.
     */
    @Override
    public Set<R> rowKeySet() {
        return read(() -> Collections.unmodifiableSet(delegate.rowKeySet()));
    }

    /**
     * Returns a row-oriented read-only view of the mappings in this table.
     *
     * @return an unmodifiable row-oriented map for this table.
     */
    @Override
    public Map<R, Map<C, V>> rowMap() {
        return read(() -> Collections.unmodifiableMap(delegate.rowMap()));
    }

    /**
     * Returns the number of mappings in this table.
     * @return the number of mappings in this table.
     */
    @Override
    public int size() {
        return read(delegate::size);
    }

    /**
     * Returns a read-only view of the values in this table.
     * @return an unmodifiable collection containing the values in this table.
     */
    @Override
    public Collection<V> values() {
        return read(() -> Collections.unmodifiableCollection(delegate.values()));
    }
}
