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
package com.d3x.morpheus.collect;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.google.common.collect.ForwardingTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import com.d3x.morpheus.util.MorpheusException;

import lombok.NonNull;

/**
 * A Guava Table that implements a write-once, read-many (WORM) protocol
 * for assigning cell values.  The value associated with a row/column key
 * pair may be assigned at most once. A second attempt to assign the same
 * cell will trigger a runtime exception.
 *
 * @param <R> the runtime row type.
 * @param <C> the runtime column type.
 * @param <V> the runtime value type.
 *
 * @author Scott Shaffer
 */
public class WormTable<R, C, V> extends ForwardingTable<R, C, V> {
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
        V result = delegate.get(rowKey, columnKey);

        if (result != null) {
            return result;
        }
        else {
            delegate.put(rowKey, columnKey, defaultValue);
            return defaultValue;
        }
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
        V result = delegate.get(rowKey, colKey);

        if (result != null) {
            return result;
        }
        else {
            var value = compute.apply(rowKey, colKey);
            delegate.put(rowKey, colKey, value);
            return value;
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
        V result = delegate.get(rowKey, colKey);

        if (result != null)
            return result;
        else
            throw new MorpheusException("No value for row [%s] and column [%s].", rowKey, colKey);
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
     * @throws RuntimeException if this table already contains a value
     * associated with the row and column key pair.
     */
    @Override
    public V put(@NonNull R rowKey, @NonNull C colKey, @NonNull V value) {
        if (delegate.contains(rowKey, colKey))
            throw new MorpheusException("Cell [%s, %s] has already been assigned.", rowKey, colKey);
        else
            return delegate.put(rowKey, colKey, value);
    }

    /**
     * Returns a read-only view of the cells in this table.
     * @return an unmodifiable set containing the cells in this table.
     */
    @Override
    public Set<Cell<R, C, V>> cellSet() {
        return Collections.unmodifiableSet(delegate.cellSet());
    }

    /**
     * Guaranteed to throw an exception and leave the table unmodified.
     * @throws RuntimeException always: cell removal is forbidden.
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
        return Collections.unmodifiableMap(delegate.column(columnKey));
    }

    /**
     * Returns a read-only view of the column keys in this table.
     * @return an unmodifiable set containing the column keys.
     */
    @Override
    public Set<C> columnKeySet() {
        return Collections.unmodifiableSet(delegate.columnKeySet());
    }

    /**
     * Returns a column-oriented read-only view of the mappings in this table.
     *
     * @return an unmodifiable column-oriented map for this table.
     */
    @Override
    public Map<C, Map<R, V>> columnMap() {
        return Collections.unmodifiableMap(delegate.columnMap());
    }

    /**
     * Guaranteed to throw an exception and leave the table unmodified.
     * @throws RuntimeException always: cell removal is forbidden.
     */
    @Override
    @Deprecated
    public V remove(@NonNull Object rowKey, @NonNull Object colKey) {
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
        return Collections.unmodifiableMap(delegate.row(rowKey));
    }

    /**
     * Returns a read-only view of the row keys in this table.
     * @return an unmodifiable set containing the row keys.
     */
    @Override
    public Set<R> rowKeySet() {
        return Collections.unmodifiableSet(delegate.rowKeySet());
    }

    /**
     * Returns a row-oriented read-only view of the mappings in this table.
     *
     * @return an unmodifiable row-oriented map for this table.
     */
    @Override
    public Map<R, Map<C, V>> rowMap() {
        return Collections.unmodifiableMap(delegate.rowMap());
    }

    /**
     * Returns a read-only view of the values in this table.
     * @return an unmodifiable collection containing the values in this table.
     */
    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(delegate.values());
    }

    @Override
    protected Table<R, C, V> delegate() {
        return delegate;
    }
}
