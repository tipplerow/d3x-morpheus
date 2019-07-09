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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.index.Index;

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

    private Lock lock;
    private Class<R> rowType;
    private Class<C> colType;
    private Index<R> rowKeys;
    private int rowCapacity = 1000;
    private Map<C,ArrayBuilder<?>> arrayMap;


    /**
     * Constructor
     * @param rowType   the row key type for frame
     * @param colType   the column key type for frame
     */
    DataFrameBuilder(
        @lombok.NonNull Class<R> rowType,
        @lombok.NonNull  Class<C> colType) {
        this.rowType = rowType;
        this.colType = colType;
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
        if (rowKeys != null) {
            return this;
        } else {
            this.rowCapacity = rowCapacity;
            this.rowKeys = Index.of(rowType, rowCapacity);
            this.arrayMap = new LinkedHashMap<>(colCapacity);
            return this;
        }
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
    private int putRow(R rowKey) {
        this.rowKeys.add(rowKey);
        return rowKeys.getCoordinate(rowKey);
    }


    /**
     * Returns the array builder for column key
     * @param colKey    the column key
     * @return          the array builder
     */
    @SuppressWarnings("unchecked")
    private <T> ArrayBuilder<T> array(C colKey) {
        var array = arrayMap.get(colKey);
        if (array != null) {
            return (ArrayBuilder<T>)array;
        } else {
            array = ArrayBuilder.of(rowCapacity);
            this.arrayMap.put(colKey, array);
            return (ArrayBuilder<T>)array;
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
     * Applies a value to this builder for the row and column key
     * @param rowKey    the row key
     * @param colKey    the column key
     * @param value     the value to apply
     * @return          this builder
     */
    public DataFrameBuilder<R,C> putBoolean(R rowKey, C colKey, boolean value) {
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
    public DataFrameBuilder<R,C> putInt(R rowKey, C colKey, int value) {
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
    public DataFrameBuilder<R,C> putLong(R rowKey, C colKey, long value) {
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
    public DataFrameBuilder<R,C> putDouble(R rowKey, C colKey, double value) {
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
    public <T> DataFrameBuilder<R,C> putValue(R rowKey, C colKey, T value) {
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
}
