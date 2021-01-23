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

import org.apache.commons.math3.linear.ArrayRealVector;

/**
 * Extends the Apache ArrayRealVector class to implement the D3xVector interface
 * and to add explicit control over data ownership.
 *
 * @author  Scott Shaffer
 */
public final class ApacheDenseVector extends ArrayRealVector implements D3xVector {
    private ApacheDenseVector(int length, double fill) {
        super(length, fill);
    }

    private ApacheDenseVector(double[] values, boolean copy) {
        super(values, copy);
    }

    private ApacheDenseVector(ArrayRealVector vector, boolean deepCopy) {
        super(vector, deepCopy);
    }

    /**
     * Creates a new vector by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new vector containing a copy of the specified array.
     */
    public static ApacheDenseVector copyOf(double... values) {
        return new ApacheDenseVector(values, true);
    }

    /**
     * Creates a new vector by copying values from another vector.
     *
     * @param vector the vector to be copied.
     *
     * @return a new vector containing a copy of the specified vector.
     */
    public static ApacheDenseVector copyOf(D3xVector vector) {
        ApacheDenseVector copy = ofLength(vector.length());

        for (int index = 0; index < vector.length(); ++index)
            copy.setEntry(index, vector.get(index));

        return copy;
    }

    /**
     * Creates a new vector with fixed length and all elements initialized to zero.
     *
     * @param length the fixed length of the vector.
     *
     * @return a new vector of the specified length with all elements initialized to zero.
     *
     * @throws RuntimeException if the length is negative.
     */
    public static ApacheDenseVector ofLength(int length) {
        return new ApacheDenseVector(length, 0.0);
    }

    /**
     * Like the {@code R} function {@code rep(x, n)}, creates a new mutable vector
     * containing the value {@code x} replicated {@code n} times.
     *
     * @param x the value to replicate.
     * @param n the number of times to replicate.
     *
     * @return a new mutable vector of length {@code n} with each element assigned
     * the value {@code x}.
     *
     * @throws RuntimeException if {@code n < 0}.
     */
    public static ApacheDenseVector rep(double x, int n) {
        return new ApacheDenseVector(n, x);
    }

    /**
     * Creates a vector view over a bare array.
     *
     * @param values the values to be viewed.
     *
     * @return a vector view over the specified array.
     */
    public static ApacheDenseVector wrap(double... values) {
        return new ApacheDenseVector(values, false);
    }

    @Override
    public D3xVector combine(double a, double b, D3xVector v) {
        if (v instanceof ArrayRealVector)
            return new ApacheDenseVector(combine(a, b, (ArrayRealVector) v), false);
        else
            return D3xVector.super.combineInPlace(a, b, v);
    }

    @Override
    public D3xVector combineInPlace(double a, double b, D3xVector v) {
        if (v instanceof ArrayRealVector)
            return (D3xVector) combineToSelf(a, b, (ArrayRealVector) v);
        else
            return D3xVector.super.combineInPlace(a, b, v);
    }

    @Override
    public ApacheDenseVector copyThis() {
        return new ApacheDenseVector(this, true);
    }

    @Override
    public double get(int index) {
        return getEntry(index);
    }

    @Override
    public int length() {
        return getDimension();
    }

    @Override
    public ApacheDenseVector like(int length) {
        return ofLength(length);
    }

    @Override
    public void set(int index, double value) {
        setEntry(index, value);
    }
}
