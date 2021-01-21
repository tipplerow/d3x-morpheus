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

import java.util.Arrays;
import com.d3x.core.lang.D3xException;

/**
 * Provides an Apache RealVector backed by a {@code double[]} array with
 * explicit control over data ownership and mutability.
 *
 * @author  Scott Shaffer
 */
public final class ApacheDenseVector extends ApacheVector {
    private final boolean mutable;
    private final double[] values;

    private ApacheDenseVector(double[] values, boolean mutable, boolean copy) {
        this.mutable = mutable;

        if (copy)
            this.values = Arrays.copyOf(values, values.length);
        else
            this.values = values;
    }

    /**
     * Creates a new mutable vector by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new mutable vector containing a copy of the specified array.
     */
    public static ApacheDenseVector copyOf(double... values) {
        return new ApacheDenseVector(values, true, true);
    }

    /**
     * Creates a new mutable vector by copying values from another vector.
     *
     * @param vector the vector to be copied.
     *
     * @return a new mutable vector containing a copy of the specified vector.
     */
    public static ApacheDenseVector copyOf(D3xVector vector) {
        ApacheDenseVector copy = ofLength(vector.length());

        for (int index = 0; index < vector.length(); ++index)
            copy.set(index, vector.get(index));

        return copy;
    }

    /**
     * Creates an <em>immutable</em> vector view over a bare array.
     *
     * @param values the values to be viewed.
     *
     * @return an <em>immutable</em> vector view over the specified array.
     */
    public static ApacheDenseVector of(double... values) {
        return new ApacheDenseVector(values, false, false);
    }

    /**
     * Creates a new mutable vector of a fixed length with all elements
     * initialized to zero.
     *
     * @param length the fixed length of the vector.
     *
     * @return a new mutable vector of the specified length with all elements
     * initialized to zero.
     *
     * @throws RuntimeException if the length is negative.
     */
    public static ApacheDenseVector ofLength(int length) {
        return new ApacheDenseVector(new double[length], true, false);
    }

    @Override
    public ApacheDenseVector like(int length) {
        return ofLength(length);
    }

    @Override
    public int getDimension() {
        return values.length;
    }

    @Override
    public double getEntry(int index) {
        return values[index];
    }

    @Override
    public void setEntry(int index, double value) {
        if (mutable)
            values[index] = value;
        else
            throw new D3xException("Cannot change an immutable ApacheDenseVector.");
    }

    @Override
    public double[] toArray() {
        return Arrays.copyOf(values, values.length);
    }
}
