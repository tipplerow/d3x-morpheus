package com.d3x.morpheus.collect;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import com.d3x.morpheus.concurrent.ConcurrentObject;

import lombok.NonNull;

/**
 * Provides a base class for concurrent in-memory caches.
 *
 * @author Scott Shaffer
 */
public abstract class CacheBase<K, V> extends ConcurrentObject {
    /**
     * The underlying map storage.
     */
    @NonNull
    protected final Map<K, V> map;

    /**
     * Creates a cache backed by a map.
     */
    protected CacheBase(@NonNull Map<K, V> map) {
        this.map = map;
    }

    /**
     * Identifies keys contained in this cache.
     *
     * @param key the key of interest.
     *
     * @return {@code true} iff this cache contains the given key.
     */
    public boolean containsKey(@NonNull K key) {
        return read(map::containsKey, key);
    }

    /**
     * Retrieves a value from this cache.
     *
     * @param key the key associated with the value.
     *
     * @return the value associated with the specified key, or
     * {@code null} if there is no match.
     */
    public V get(@NonNull K key) {
        return read(map::get, key);
    }

    /**
     * Retrieves an existing value from this cache or assigns a default.
     *
     * @param targetKey    the key associated with the value.
     * @param defaultValue the default value to assign and return if
     *                     there is no match for the key.
     *
     * @return the value associated with the specified key, or the
     * default value if there is no match.
     */
    public V getOrAssign(@NonNull K targetKey, @NonNull V defaultValue) {
        V result = get(targetKey);

        if (result != null) {
            return result;
        }
        else {
            write(this::putIfAbsent, targetKey, defaultValue);
            // Another thread might have acquired the write lock and assigned
            // the same key during the interval when this thread did not hold
            // the write lock, so we must retrieve the value associated with
            // the key again...
            return get(targetKey);
        }
    }

    private void putIfAbsent(@NonNull K key, @NonNull V value) {
        // Another thread might have acquired the write lock and assigned
        // the same key during the interval when this thread did not hold
        // the write lock.  If so, we cannot overwrite...
        if (!map.containsKey(key))
            map.put(key, value);
    }

    /**
     * Retrieves an existing value from this cache or computes and
     * assigns a value.
     *
     * @param key     the key associated with the value.
     * @param compute the function to compute missing values.
     *
     * @return the value associated with the specified keys, or the
     * computed value if there is no match.
     */
    public V getOrCompute(@NonNull K key, @NonNull Function<K, V> compute) {
        V result = get(key);

        if (result != null) {
            return result;
        }
        else {
            var value = compute.apply(key);
            write(this::putIfAbsent, key, value);
            // Another thread might have acquired the write lock and assigned
            // the same key during the interval when this thread did not hold
            // the write lock, so we must retrieve the value associated with
            // the key again...
            return get(key);
        }
    }

    /**
     * Retrieves a value from this cache or throws an exception.
     *
     * @param key the key associated with the value.
     *
     * @return the value associated with the specified key.
     *
     * @throws NoSuchElementException unless a value has been
     * associated with the specified key.
     */
    public V getOrThrow(@NonNull K key) {
        return getOrThrow(key, "No value for key [%s].");
    }

    /**
     * Retrieves a value from this cache or throws an exception.
     *
     * @param key the key associated with the value.
     * @param msg the formatted exception message, which must contain
     *            exactly one format specifier for the missing key.
     *
     * @return the value associated with the specified key.
     *
     * @throws NoSuchElementException unless a value has been
     * associated with the specified key.
     */
    public V getOrThrow(@NonNull K key, @NonNull String msg) {
        V result = get(key);

        if (result != null)
            return result;
        else
            throw new NoSuchElementException(String.format(msg, key));
    }

    /**
     * Identifies empty caches.
     * @return {@code true} iff this cache contains no items.
     */
    public boolean isEmpty() {
        return read(map::isEmpty);
    }

    /**
     * Returns a read-only set view of the keys in this cache.
     * @return a read-only set view of the keys in this cache.
     */
    public Set<K> keys() {
        return Collections.unmodifiableSet(read(map::keySet));
    }

    /**
     * Assigns a key/value pair.
     *
     * @param key   the key to associate with the value.
     * @param value the value to associate with the key.
     */
    public void put(@NonNull K key, @NonNull V value) {
        write(map::put, key, value);
    }

    /**
     * Returns the number of items in this cache.
     * @return the number of items in this cache.
     */
    public int size() {
        return read(map::size);
    }

    /**
     * Returns a read-only view of the items in this cache.
     * @return a read-only view of the items in this cache.
     */
    public Collection<V> values() {
        return Collections.unmodifiableCollection(read(map::values));
    }
}
