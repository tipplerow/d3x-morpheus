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
package com.d3x.morpheus.apache;

import java.util.Arrays;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import com.d3x.morpheus.util.DoubleComparator;

/**
 * Provides a partial implementation of the abstract methods in {@code RealVector}
 * in a manner that is independent of the underlying physical storage.
 *
 * <p>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public abstract class AbstractRealVector extends RealVector {
    /**
     * Returns a new vector with the same underlying storage type (dense or sparse) as this vector.
     *
     * @param length the desired length of the new vector.
     *
     * @return a new vector with the specified length having the same underlying storage type
     * (dense or sparse) as this vector.
     *
     * @throws IllegalArgumentException if the length is negative.
     */
    public abstract RealVector like(int length);

    /**
     * Returns a new vector with the same length and underlying storage type (dense or sparse) as this vector.
     *
     * @return a new vector with the same length and underlying storage type (dense or sparse) as this vector.
     */
    public RealVector like() {
        return like(getDimension());
    }

    /**
     * Determines whether the entries in this vector are equal to those in a bare array
     * <em>within the fixed tolerance of the default DoubleComparator</em>.
     *
     * @param values the values to test for equality.
     *
     * @return {@code true} iff the input array has the same length as this vector and
     * each value matches the corresponding entry in this vector within the tolerance
     * of the default DoubleComparator.
     */
    public boolean equalsArray(double... values) {
        return equalsArray(values, DoubleComparator.FIXED_DEFAULT);
    }

    /**
     * Determines whether the entries in this vector are equal to those in a bare array
     * within the tolerance of a DoubleComparator.
     *
     * @param values     the values to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input array has the same length as this vector and
     * each value matches the corresponding entry in this vector within the tolerance
     * of the specified comparator.
     */
    public boolean equalsArray(double[] values, DoubleComparator comparator) {
        return equalsVector(new ArrayRealVector(values), comparator);
    }

    /**
     * Determines whether the entries in this vector are equal to those in another
     * vector <em>within the fixed tolerance of the default DoubleComparator</em>.
     *
     * @param that the vector to test for equality.
     *
     * @return {@code true} iff the input vector has the same length as this vector and
     * each value matches the corresponding entry in this vector within the tolerance
     * of the default DoubleComparator.
     */
    public boolean equalsVector(RealVector that) {
        return equalsVector(that, DoubleComparator.FIXED_DEFAULT);
    }

    /**
     * Determines whether the entries in this vector are equal to those in another
     * vector within the tolerance of a DoubleComparator</em>.
     *
     * @param that       the vector to test for equality.
     * @param comparator the element comparator.
     *
     * @return {@code true} iff the input vector has the same length as this vector and
     * each value matches the corresponding entry in this vector within the tolerance
     * of the specified comparator.
     */
    public boolean equalsVector(RealVector that, DoubleComparator comparator) {
        if (this.getDimension() != that.getDimension())
            return false;

        for (int index = 0; index < getDimension(); index++) {
            double thisEntry = this.getEntry(index);
            double thatEntry = that.getEntry(index);

            if (!comparator.equals(thisEntry, thatEntry))
                return false;
        }

        return true;
    }

    @Override
    public RealVector append(double value) {
        RealVector result = like(getDimension() + 1);

        result.setSubVector(0, this);
        result.setEntry(this.getDimension(), value);

        return result;
    }

    @Override
    public RealVector append(RealVector that) {
        RealVector result = like(this.getDimension() + that.getDimension());

        result.setSubVector(0, this);
        result.setSubVector(this.getDimension(), that);

        return result;
    }

    @Override
    public RealVector copy() {
        RealVector copy = like();

        for (int index = 0; index < getDimension(); index++)
            copy.setEntry(index, this.getEntry(index));

        return copy;
    }

    @Override
    public RealVector ebeDivide(RealVector that) throws DimensionMismatchException {
        checkVectorDimensions(that);
        RealVector result = like();

        for (int index = 0; index < getDimension(); index++)
            result.setEntry(index, this.getEntry(index) / that.getEntry(index));

        return result;
    }

    @Override
    public RealVector ebeMultiply(RealVector that) throws DimensionMismatchException {
        checkVectorDimensions(that);
        RealVector result = like();

        for (int index = 0; index < getDimension(); index++)
            result.setEntry(index, this.getEntry(index) * that.getEntry(index));

        return result;
    }

    @Override
    public RealVector getSubVector(int start, int length) throws NotPositiveException, OutOfRangeException {
        if (length < 1)
            throw new NotPositiveException(length);

        checkIndex(start);
        RealVector subVector = like(length);

        for (int subIndex = 0; subIndex < length; subIndex++)
            subVector.setEntry(subIndex, this.getEntry(start + subIndex));

        return subVector;
    }

    @Override
    public boolean isNaN() {
        for (int index = 0; index < getDimension(); index++) {
            if (Double.isNaN(getEntry(index)))
                return true;
        }

        return false;
    }

    @Override
    public boolean isInfinite() {
        for (int index = 0; index < getDimension(); index++) {
            if (Double.isInfinite(getEntry(index)))
                return true;
        }

        return false;
    }

    @Override
    public void setSubVector(int start, RealVector subVector) throws OutOfRangeException {
        checkIndex(start);

        for (int subIndex = 0; subIndex < subVector.getDimension(); subIndex++)
            this.setEntry(start + subIndex, subVector.getEntry(subIndex));
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RealVector) && equalsVector((RealVector) obj);
    }

    @Override
    public int hashCode() {
        //
        // As containers of floating-point values which are subject to
        // round-off error, RealVectors should not be used as hash keys...
        //
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), Arrays.toString(toArray()));
    }
}
