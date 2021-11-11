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
package com.d3x.morpheus.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.d3x.morpheus.util.functions.TriConsumer;

/**
 * Provides a concurrent object with a one-writer, many-readers policy for
 * synchronized read and write operations.
 *
 * @author Scott Shaffer
 */
public abstract class ConcurrentObject {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    /**
     * The default constructor.
     */
    protected ConcurrentObject() {
    }

    /**
     * Acquires a read lock, reads data from this object, and releases the
     * read lock.
     *
     * @param function a function to extract the desired data.
     * @param argument the argument to the function.
     *
     * @return the data returned by the supplier.
     */
    public <T, R> R read(Function<T, R> function, T argument) {
        readLock.lock();

        try {
            return function.apply(argument);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Acquires a read lock, reads data from this object, and releases the
     * read lock.
     *
     * @param func a function to extract the desired data.
     * @param arg1 the first argument to the function.
     * @param arg2 the first argument to the function.
     *
     * @return the data returned by the supplier.
     */
    public <T, U, R> R read(BiFunction<T, U, R> func, T arg1, U arg2) {
        readLock.lock();

        try {
            return func.apply(arg1, arg2);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Acquires a read lock, evaluates a predicate on this object, and
     * releases the read lock.
     *
     * @param predicate the predicate to evaluate.
     * @param argument  the argument to the predicate.
     *
     * @return the data returned by the supplier.
     */
    public <T> boolean read(Predicate<T> predicate, T argument) {
        readLock.lock();

        try {
            return predicate.test(argument);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Acquires a read lock, evaluates a predicate on this object, and
     * releases the read lock.
     *
     * @param predicate the predicate to evaluate.
     * @param argument1 the first argument to the predicate.
     * @param argument2 the second argument to the predicate.
     *
     * @return the data returned by the supplier.
     */
    public <T, U> boolean read(BiPredicate<T, U> predicate, T argument1, U argument2) {
        readLock.lock();

        try {
            return predicate.test(argument1, argument2);
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Acquires a read lock, reads data from this object, and releases the
     * read lock.
     *
     * @param supplier a supplier to extract the desired data.
     *
     * @return the data returned by the supplier.
     */
    public <T> T read(Supplier<T> supplier) {
        readLock.lock();

        try {
            return supplier.get();
        }
        finally {
            readLock.unlock();
        }
    }

    /**
     * Acquires a write lock, writes data to this object, and releases the
     * write lock.
     *
     * @param consumer a consumer to write the supplied data.
     * @param data     the data item to write.
     */
    public <T> void write(Consumer<T> consumer, T data) {
        writeLock.lock();

        try {
            consumer.accept(data);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Acquires a write lock, writes data items to this object, and releases
     * the write lock.
     *
     * @param consumer a consumer to write the supplied data.
     * @param data     the data items to write.
     */
    public <T> void write(Consumer<T> consumer, Iterable<T> data) {
        writeLock.lock();

        try {
            for (var item : data)
                consumer.accept(item);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Acquires a write lock, writes a key/value pair to this object, and
     * releases the write lock.
     *
     * @param consumer a consumer to write the supplied data.
     * @param key      the key to write.
     * @param value    the value to write.
     */
    public <K, V> void write(BiConsumer<K, V> consumer, K key, V value) {
        writeLock.lock();

        try {
            consumer.accept(key, value);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Acquires a write lock, writes a row/column/value triplet to this object,
     * and releases the write lock.
     *
     * @param consumer a consumer to write the supplied data.
     * @param rowKey   the row key to write.
     * @param colKey   the column key to write.
     * @param value    the value to write.
     */
    public <R, C, V> void write(TriConsumer<R, C, V> consumer, R rowKey, C colKey, V value) {
        writeLock.lock();

        try {
            consumer.accept(rowKey, colKey, value);
        }
        finally {
            writeLock.unlock();
        }
    }
}
