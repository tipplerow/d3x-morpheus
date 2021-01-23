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

import com.d3x.morpheus.util.DoubleComparator;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SparseRealVector;

import java.util.Iterator;

/**
 * Extends the Apache OpenMapRealVector class to implement the D3xVector interface.
 *
 * @author  Scott Shaffer
 */
public final class ApacheSparseVector extends OpenMapRealVector implements D3xVector {
    private ApacheSparseVector(int length) {
        super(length);
    }

    private ApacheSparseVector(OpenMapRealVector vector) {
        super(vector);
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
    public static ApacheSparseVector ofLength(int length) {
        return new ApacheSparseVector(length);
    }

    @Override
    public D3xVector combineInPlace(double a, double b, D3xVector v) {
        if (v instanceof SparseRealVector)
            return combineSparseInPlace(a, b, (SparseRealVector) v);
        else
            return D3xVector.super.combineInPlace(a, b, v);
    }

    private D3xVector combineSparseInPlace(double a, double b, SparseRealVector v) {
        if (!DoubleComparator.DEFAULT.equals(a, 1.0))
            mapMultiplyToSelf(a);

        Iterator<RealVector.Entry> vIterator = v.sparseIterator();

        while (vIterator.hasNext()) {
            RealVector.Entry vEntry = vIterator.next();
            addToEntry(vEntry.getIndex(), b * vEntry.getValue());
        }

        return this;
    }

    @Override
    public ApacheSparseVector copyThis() {
        return new ApacheSparseVector(this);
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
    public ApacheSparseVector like(int length) {
        return ofLength(length);
    }

    @Override
    public void set(int index, double value) {
        setEntry(index, value);
    }
}

