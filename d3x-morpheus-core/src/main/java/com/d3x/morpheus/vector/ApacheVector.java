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

import lombok.NonNull;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SparseRealVector;

/**
 * Wraps an Apache RealVector implementation in a D3xVector interface.
 *
 * @author Scott Shaffer
 */
public final class ApacheVector implements D3xVector {
    @NonNull private final RealVector impl;

    private ApacheVector(RealVector impl) {
        this.impl = impl;
    }

    /**
     * The single empty vector.
     */
    public static final ApacheVector EMPTY = wrap(new ArrayRealVector());

    /**
     * Returns a read-only RealVector view of a generic D3xVector to be used
     * as the operand in algebraic functions.  Modifying the returned vector
     * is not permitted, and the results of doing so are undefined.
     *
     * @param vector the algebraic operand.
     *
     * @return a read-only RealVector view of the specified operand.
     */
    public static RealVector asOperand(D3xVector vector) {
        //
        // Finds the best RealVector implementation to use when operating on
        // generic D3xVector instances.
        //
        if (vector instanceof ApacheVector)
            return ((ApacheVector) vector).impl;

        if (vector instanceof RealVector)
            return (RealVector) vector;

        return new ArrayRealVector(vector.toArray(), false);
    }

    /**
     * Returns a new vector by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new vector containing a copy of the specified array.
     */
    public static ApacheVector copyOf(double... values) {
        return wrap(new ArrayRealVector(values, true));
    }

    /**
     * Returns a new vector with dense physical storage and all elements
     * initialized to zero.
     *
     * @param length the length for the new vector.
     *
     * @return a new vector of the specified length with dense physical
     * storage and all elements initialized to zero.
     *
     * @throws RuntimeException if the length is negative.
     */
    public static ApacheVector dense(int length) {
        return rep(0.0, length);
    }

    /**
     * Like the {@code R} function {@code rep(x, n)}, creates a new vector
     * containing the value {@code x} replicated {@code n} times.
     *
     * @param x the value to replicate.
     * @param n the number of times to replicate.
     *
     * @return a new vector of length {@code n} with each element assigned
     * the value {@code x}.
     *
     * @throws RuntimeException if {@code n < 0}.
     */
    public static ApacheVector rep(double x, int n) {
        return wrap(new ArrayRealVector(n, x));
    }

    /**
     * Returns a new vector with sparse physical storage and all elements
     * initialized to zero.
     *
     * @param length the length for the new vector.
     *
     * @return a new vector of the specified length with sparse physical
     * storage and all elements initialized to zero.
     *
     * @throws RuntimeException if the length is negative.
     */
    public static ApacheVector sparse(int length) {
        return wrap(new OpenMapRealVector(length));
    }

    /**
     * Returns a shallow wrapper around a bare array.  Changes to the
     * array will be reflected in the returned vector, and changes to
     * the returned vector will be reflected
     *
     * @param values the bare values to wrap.
     *
     * @return a new vector view of the specified values.
     */
    public static ApacheVector wrap(double... values) {
        return wrap(new ArrayRealVector(values, false));
    }

    /**
     * Wraps an existing RealVector implementation in a D3xVector interface.
     *
     * @param vector the RealVector implementation to wrap.
     *
     * @return a new D3xVector interface with the specified implementation.
     */
    public static ApacheVector wrap(RealVector vector) {
        return new ApacheVector(vector);
    }

    @Override
    public ApacheVector combine(double a, double b, D3xVector v) {
        return wrap(impl.combine(a, b, asOperand(v)));
    }

    @Override
    public ApacheVector combineInPlace(double a, double b, D3xVector v) {
        impl.combineToSelf(a, b, asOperand(v));
        return this;
    }

    @Override
    public ApacheVector copy() {
        return new ApacheVector(impl.copy());
    }

    @Override
    public double get(int index) {
        return impl.getEntry(index);
    }

    @Override
    public ApacheVector getSubVector(int start, int length) {
        return wrap(impl.getSubVector(start, length));
    }

    @Override
    public int length() {
        return impl.getDimension();
    }

    @Override
    public ApacheVector like(int length) {
        if (impl instanceof SparseRealVector)
            return sparse(length);
        else
            return dense(length);
    }

    @Override
    public ApacheVector multiplyInPlace(double scalar) {
        impl.mapMultiplyToSelf(scalar);
        return this;
    }

    @Override
    public void set(int index, double value) {
        impl.setEntry(index, value);
    }

    @Override
    public ApacheVector setSubVector(int index, D3xVector subVector) {
        impl.setSubVector(index, asOperand(subVector));
        return this;
    }

    @Override
    public ApacheVector times(double scalar) {
        return wrap(impl.mapMultiply(scalar));
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}
