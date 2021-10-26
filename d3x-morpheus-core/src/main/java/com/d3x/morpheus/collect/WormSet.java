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

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.d3x.morpheus.util.MorpheusException;

import lombok.NonNull;

/**
 * A set that implements a write-once, read-many (WORM) protocol for
 * adding elements.  An element may be added only once; additional
 * attempts to add the same element will trigger a runtime exception.
 *
 * @param <E> the runtime element type.
 *           
 * @author Scott Shaffer
 */
public class WormSet<E> extends AbstractSet<E> {
    /**
     * The underlying set storage.
     */
    @NonNull
    protected Set<E> set;

    /**
     * Creates an empty WORM set using a HashSet for the underlying storage.
     */
    public WormSet() {
        this(new HashSet<>());
    }

    /**
     * Creates a new WORM set using another set for the underlying storage;
     * the contents of the input set become the contents of the WORM set.
     *
     * @param set the underlying set.
     */
    public WormSet(@NonNull Set<E> set) {
        this.set = set;
    }

    /**
     * Creates an empty WORM set using a HashSet for the underlying storage.
     *
     * @param <E> the runtime element type.
     *
     * @return a new empty WORM set using a HashSet for the underlying storage.
     */
    public static <E> WormSet<E> hash() {
        return new WormSet<>(new HashSet<>());
    }

    /**
     * Creates an empty WORM set using a TreeSet for the underlying storage.
     *
     * @param <E> the runtime element type.
     *
     * @return a new empty WORM set using a TreeSet for the underlying storage.
     */
    public static <E extends Comparable<?>> WormSet<E> tree() {
        return new WormSet<>(new TreeSet<>());
    }

    /**
     * Permanently adds an element to this set.
     *
     * <p>This method may be called at most once with a given element;
     * calling it a second time with the same element will trigger an
     * exception.</p>
     *
     * @param element the element to add.
     *
     * @return {@code true}, because this set must not already contain
     * the element.
     *
     * @throws RuntimeException if this set already contains the element.
     */
    @Override
    public boolean add(@NonNull E element) {
        if (set.contains(element))
            throw new MorpheusException("Element [%s] has already been added.", element);
        else
            return set.add(element);
    }

    /**
     * Guaranteed to throw an exception and leave the set unmodified.
     * @throws RuntimeException always: element removal is forbidden.
     */
    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the set unmodified.
     * @throws RuntimeException always: element removal is forbidden.
     */
    @Override
    @Deprecated
    public boolean remove(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object element) {
        return set.contains(element);
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableSet(set).iterator();
    }

    @Override
    public int size() {
        return set.size();
    }
}
