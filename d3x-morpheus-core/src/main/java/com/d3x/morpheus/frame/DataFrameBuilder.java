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
package com.d3x.morpheus.frame;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.index.IndexException;

/**
 * A builder class to iteratively construct a DataFrame
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Xavier Witdouck
 */
public class DataFrameBuilder<R,C> {

    private static final int DEFAULT_ROW_CAPACITY = 1000;
    private static final int DEFAULT_COL_CAPACITY = 100;

    /** The row key type for frame */
    @Getter
    private final Class<R> rowType;
    /** The column key type for frame */
    @Getter
    private final Class<C> colType;
    /** The function to provide the load factor for columns */
    @Setter @NonNull
    private Function<C,Float> loadFactor = c -> 1f;
    /** The function to provide the default value for columns */
    @Setter @NonNull
    private Function<C,Object> defaultValue = c -> null;

    private Lock lock;
    private Index<R> rowKeys;
    private int rowCapacity = DEFAULT_ROW_CAPACITY;
    private Map<C,ArrayBuilder<?>> arrayMap;


    /**
     * Constructor
     * @param rowType   the row key type for frame
     * @param colType   the column key type for frame
     */
    DataFrameBuilder(
        @NonNull Class<R> rowType,
        @NonNull Class<C> colType) {
        this.rowType = rowType;
        this.colType = colType;
    }


    /**
     * Constructor
     * @param frame the frame to initialize this builder from
     */
    DataFrameBuilder(@NonNull DataFrame<R,C> frame) {
        this(frame.rows().keyClass(), frame.cols().keyClass());
        this.capacity(frame.rowCount(), frame.colCount());
        this.putAll(frame);
    }


    /**
     * Returns a newly created frame from the contents of this builder
     * @return      the newly created DataFrame
     */
    public DataFrame<R,C> build() {
        this.capacity(100, 10);
        return DataFrame.of(rowKeys, colType, columns -> {
            arrayMap.forEach((key, value) -> {
                var array = value.toArray();
                columns.add(key, array);
            });
        });
    }


    /**
     * Sets the capacity for this builder if not already set
     * @param rowCapacity   the initial row capacity
     * @param colCapacity   the initial column capacity
     * @return              this builder
     */
    public final DataFrameBuilder<R,C> capacity(int rowCapacity, int colCapacity) {
        try {
            this.acquireLock();
            this.rowCapacity(rowCapacity);
            if (arrayMap != null) {
                return this;
            } else {
                this.arrayMap = new LinkedHashMap<>(Math.max(colCapacity, 10));
                return this;
            }
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Sets the row capacity for this builder if not already set
     * @param rowCapacity   the initial row capacity
     * @return              this builder
     */
    public final DataFrameBuilder<R,C> rowCapacity(int rowCapacity) {
        try {
            this.acquireLock();
            if (rowKeys != null) {
                return this;
            } else {
                this.rowCapacity = Math.max(rowCapacity, 10);
                this.rowKeys = Index.of(rowType, this.rowCapacity);
                return this;
            }
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Sets the load factor for builder
     * @param loadFactor    the load factor for builder
     * @return              this builder
     */
    public DataFrameBuilder<R,C> loadFactor(@NonNull Function<C,Float> loadFactor) {
        this.loadFactor = loadFactor;
        return this;
    }


    /**
     * Sets the default value function for builder
     * @param defaultValue  the default value function
     * @return              this builder
     */
    public DataFrameBuilder<R,C> defaultValue(@NonNull Function<C,Object> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }


    /**
     * Returns the current row count for builder
     * @return  the current row count
     */
    public int rowCount() {
        return rowKeys != null ? rowKeys.size() : 0;
    }


    /**
     * Returns the current column count for builder
     * @return  the current column count
     */
    public int colCount() {
        return arrayMap != null ? arrayMap.size() : 0;
    }


    /**
     * Returns the stream of row keys for this builder
     * @return  the stream of row keys
     */
    public Stream<R> rowKeys() {
        return rowKeys != null ? rowKeys.keys() : Stream.empty();
    }


    /**
     * Returns the stream of column keys for this builder
     * @return  the stream of column keys
     */
    public Stream<C> colKeys() {
        return arrayMap != null ? arrayMap.keySet().stream() : Stream.empty();
    }


    /**
     * Returns true if this builder is thread safe
     * @return  true if builder is thread safe
     */
    public boolean isThreadSafe() {
        return lock != null;
    }


    /**
     * Called to acquire the lock
     */
    private void acquireLock() {
        if (this.lock != null) {
            this.lock.lock();
        }
    }


    /**
     * Called to release the lock
     */
    private void releaseLock() {
        if (this.lock != null) {
            this.lock.unlock();
        }
    }


    /**
     * Adds the row key if new, and returns the coordinate for key
     * @param rowKey    the row key to add
     * @return          this coordinate for row key
     */
    private int putRow(@NonNull R rowKey) {
        this.rowKeys.add(rowKey);
        return rowKeys.getCoordinate(rowKey);
    }


    /**
     * Returns the array builder for column key
     * @param colKey    the column key
     * @return          the array builder
     */
    @SuppressWarnings("unchecked")
    private <T> ArrayBuilder<T> array(@NonNull C colKey) {
        var array = arrayMap.get(colKey);
        if (array != null) {
            return (ArrayBuilder<T>)array;
        } else {
            var loadFactor = this.loadFactor.apply(colKey);
            if (loadFactor < 0 || loadFactor > 1) {
                throw new IllegalStateException("Invalid load factor for " + colKey + ", must be > 0 and <= 1, not " + loadFactor);
            } else {
                var defaultValue = (T)this.defaultValue.apply(colKey);
                if (defaultValue == null) {
                    array = ArrayBuilder.of(rowCapacity, loadFactor);
                    this.arrayMap.put(colKey, array);
                    return (ArrayBuilder<T>)array;
                } else {
                    var dataType = (Class<T>)defaultValue.getClass();
                    array = ArrayBuilder.of(rowCapacity, dataType, defaultValue, loadFactor);
                    this.arrayMap.put(colKey, array);
                    return (ArrayBuilder<T>)array;
                }
            }
        }
    }


    /**
     * Returns true if this builder contains the row key
     * @param rowKey    the row key to check
     * @return          true if row key exists
     */
    public boolean hasRow(@NonNull R rowKey) {
        return rowKeys != null && rowKeys.contains(rowKey);
    }


    /**
     * Returns true if this builder contains the column key
     * @param colKey    the column key to check
     * @return          true if column key exists
     */
    public boolean hasColumn(@NonNull C colKey) {
        return arrayMap != null && arrayMap.containsKey(colKey);
    }


    /**
     * Returns true if the builder contains the row and column
     * @param rowKey        the row key
     * @param colKey        the column key
     * @return
     */
    public boolean contains(
        @NonNull R rowKey,
        @NonNull C colKey) {
        return hasRow(rowKey) && hasColumn(colKey);
    }


    /**
     * Returns true if the value at coordinates is null
     * @param rowKey    the row key
     * @param colKey    the column key
     * @return          true if value is null
     */
    public boolean isNull(R rowKey, C colKey) {
        var array = arrayMap.get(colKey);
        if (array == null) {
            return true;
        } else {
            var rowIndex = rowKeys.getCoordinate(rowKey);
            return rowIndex < 0 || array.isNull(rowIndex);
        }
    }


    /**
     * Makes this builder thread safe
     * @return  this builder
     */
    public synchronized DataFrameBuilder<R,C> threadSafe() {
        if (lock != null) {
            return this;
        } else {
            this.lock = new ReentrantLock();
            return this;
        }
    }


    /**
     * Replaces an existing row key with a new key
     * @param existing      the existing key to replace
     * @param replacement   the replacement key
     * @return              this builder
     */
    public DataFrameBuilder<R,C> replaceRowKey(
        @NonNull R existing,
        @NonNull R replacement) {
        try {
            this.rowKeys.replace(existing, replacement);
            return this;
        } catch (IndexException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }


    /**
     * Replaces an existing column key with a new key
     * @param existing      the existing key to replace
     * @param replacement   the replacement key
     * @return              this builder
     */
    public DataFrameBuilder<R,C> replaceColKey(
        @NonNull C existing,
        @NonNull C replacement) {
        var array = arrayMap.remove(existing);
        if (array == null) {
            return this;
        } else if (arrayMap.containsKey(replacement)) {
            throw new IllegalArgumentException("Replacement key already exists: " + replacement);
        } else {
            this.arrayMap.put(replacement, array);
            return this;
        }
    }


    /**
     * Adds all rows to this builder
     * @param rowKeys   the row keys to add
     * @return          this builder
     */
    public DataFrameBuilder<R,C> addRows(@NonNull Iterable<R> rowKeys) {
        try {
            this.acquireLock();
            this.rowCapacity(DEFAULT_ROW_CAPACITY);
            rowKeys.forEach(this::putRow);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Adds a column to this builder if it does not already exist
     * @param colKey    the column key to add
     * @param dataType  the data type
     * @return          this builder
     */
    public <T> DataFrameBuilder<R,C> addColumn(
        @NonNull C colKey,
        @NonNull Class<T> dataType) {
        return addColumns(Set.of(colKey), dataType);
    }


    /**
     * Adds columns to this builder if they do not already exist
     * @param colKeys   the column keys
     * @param dataType  the data type for columns
     * @return          this builder
     */
    @SuppressWarnings("unchecked")
    public <T> DataFrameBuilder<R,C> addColumns(
        @NonNull Set<C> colKeys,
        @NonNull Class<T> dataType) {
        this.acquireLock();
        this.capacity(DEFAULT_ROW_CAPACITY, DEFAULT_COL_CAPACITY);
        colKeys.forEach(colKey -> {
            var array = arrayMap.get(colKey);
            if (array == null) {
                var loadFactor = this.loadFactor.apply(colKey);
                if (loadFactor < 0 || loadFactor > 1) {
                    throw new IllegalStateException("Invalid load factor for " + colKey + ", must be > 0 and <= 1, not " + loadFactor);
                } else {
                    var defaultValue = (T)this.defaultValue.apply(colKey);
                    array = ArrayBuilder.of(rowCapacity, dataType, defaultValue, loadFactor);
                    this.arrayMap.put(colKey, array);
                }
            }
        });
        return this;
    }



    /**
     * Applies a value to this builder for the row and column key
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to apply
     * @return          this builder
     */
    public DataFrameBuilder<R,C> putBoolean(
        @NonNull R rowKey,
        @NonNull C colKey, boolean value) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            var coord = this.putRow(rowKey);
            var array = this.array(colKey);
            array.setBoolean(coord, value);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Applies a value to this builder for the row and column key
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to apply
     * @return          this builder
     */
    public DataFrameBuilder<R,C> putInt(
        @NonNull R rowKey,
        @NonNull C colKey, int value) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            var coord = this.putRow(rowKey);
            var array = this.array(colKey);
            array.setInt(coord, value);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Applies a value to this builder for the row and column key
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to apply
     * @return          this builder
     */
    public DataFrameBuilder<R,C> putLong(
        @NonNull R rowKey,
        @NonNull C colKey, long value) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            var coord = this.putRow(rowKey);
            var array = this.array(colKey);
            array.setLong(coord, value);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Applies a value to this builder for the row and column key
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to apply
     * @return          this builder
     */
    public DataFrameBuilder<R,C> putDouble(
        @NonNull R rowKey,
        @NonNull C colKey, double value) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            var coord = this.putRow(rowKey);
            var array = this.array(colKey);
            array.setDouble(coord, value);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Applies a value to this builder for the row and column key
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to apply
     * @return          this builder
     */
    public <T> DataFrameBuilder<R,C> putValue(
        @NonNull R rowKey,
        @NonNull C colKey, T value) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            var coord = this.putRow(rowKey);
            var array = this.array(colKey);
            array.setValue(coord, value);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Adds the int value to an existing value at coordinates, or applies the value if no existing value
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to add
     * @return          this builder
     */
    public DataFrameBuilder<R,C> plusInt(
        @NonNull R rowKey,
        @NonNull C colKey, int value) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            var coord = this.putRow(rowKey);
            var array = this.array(colKey);
            array.plusInt(coord, value);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Adds the int value to an existing value at coordinates, or applies the value if no existing value
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to add
     * @return          this builder
     */
    public DataFrameBuilder<R,C> plusLong(
        @NonNull R rowKey,
        @NonNull C colKey, long value) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            var coord = this.putRow(rowKey);
            var array = this.array(colKey);
            array.plusLong(coord, value);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Adds the double value to an existing value at coordinates, or applies the value if no existing value
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to add
     * @return          this builder
     */
    public DataFrameBuilder<R,C> plusDouble(
        @NonNull R rowKey,
        @NonNull C colKey, double value) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            var coord = this.putRow(rowKey);
            var array = this.array(colKey);
            array.plusDouble(coord, value);
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Adds all double values from the input frame to this builder
     * @param other     the other frame to add doubles from
     * @return          this builder
     */
    public DataFrameBuilder<R,C> plusDoubles(
        @NonNull DataFrame<R,C> other) {
        try {
            this.acquireLock();
            this.capacity(1000, 10);
            other.values().filter(DataFrameValue::isDouble).forEach(v -> {
                var rowKey = v.rowKey();
                var colKey = v.colKey();
                this.plusDouble(rowKey, colKey, v.getDouble());
            });
            return this;
        } finally {
            this.releaseLock();
        }
    }


    /**
     * Puts all data from the arg frame into this builder
     * @param other the other frame to extract data from
     * @return      this builder
     */
    public DataFrameBuilder<R,C> putAll(@NonNull DataFrame<R,C> other) {
        other.cols().forEach(column -> {
            var dataClass = column.dataClass();
            var dataType = ArrayType.of(dataClass);
            switch (dataType) {
                case BOOLEAN:   column.forEach(v -> putBoolean(v.rowKey(), v.colKey(), v.getBoolean()));    break;
                case INTEGER:   column.forEach(v -> putInt(v.rowKey(), v.colKey(), v.getInt()));            break;
                case LONG:      column.forEach(v -> putLong(v.rowKey(), v.colKey(), v.getLong()));          break;
                case DOUBLE:    column.forEach(v -> putDouble(v.rowKey(), v.colKey(), v.getDouble()));      break;
                default:        column.forEach(v -> putValue(v.rowKey(), v.colKey(), v.getValue()));        break;
            }
        });
        return this;
    }
}
