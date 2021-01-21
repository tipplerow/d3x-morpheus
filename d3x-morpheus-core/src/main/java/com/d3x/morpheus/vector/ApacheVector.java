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

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.RealVector;

/**
 * Provides a partial implementation of the abstract methods in {@code RealVector}
 * in a manner that is independent of the underlying physical storage.
 *
 * @author Scott Shaffer
 */
public abstract class ApacheVector extends RealVector implements D3xVector {
    @Override
    public RealVector append(double value) {
        RealVector result = (RealVector) like(getDimension() + 1);

        result.setSubVector(0, this);
        result.setEntry(this.getDimension(), value);

        return result;
    }

    @Override
    public RealVector append(RealVector that) {
        RealVector result = (RealVector) like(this.getDimension() + that.getDimension());

        result.setSubVector(0, this);
        result.setSubVector(this.getDimension(), that);

        return result;
    }

    @Override
    public RealVector copy() {
        return (RealVector) copyOf();
    }

    @Override
    public RealVector ebeDivide(RealVector that) throws DimensionMismatchException {
        checkVectorDimensions(that);
        RealVector result = (RealVector) like();

        for (int index = 0; index < getDimension(); index++)
            result.setEntry(index, this.getEntry(index) / that.getEntry(index));

        return result;
    }

    @Override
    public RealVector ebeMultiply(RealVector that) throws DimensionMismatchException {
        checkVectorDimensions(that);
        RealVector result = (RealVector) like();

        for (int index = 0; index < getDimension(); index++)
            result.setEntry(index, this.getEntry(index) * that.getEntry(index));

        return result;
    }

    @Override
    public double get(int index) {
        return getEntry(index);
    }

    @Override
    public RealVector getSubVector(int start, int length) throws NotPositiveException, OutOfRangeException {
        if (length < 1)
            throw new NotPositiveException(length);

        checkIndex(start);
        RealVector subVector = (RealVector) like(length);

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
    public int length() {
        return getDimension();
    }

    @Override
    public void set(int index, double value) {
        setEntry(index, value);
    }

    @Override
    public void setSubVector(int start, RealVector subVector) throws OutOfRangeException {
        checkIndex(start);

        for (int subIndex = 0; subIndex < subVector.getDimension(); subIndex++)
            this.setEntry(start + subIndex, subVector.getEntry(subIndex));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof D3xVector)
            return equalsVector((D3xVector) obj);
        else if (obj instanceof RealVector)
            return equalsArray(((RealVector) obj).toArray());
        else
            return false;
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
        return String.format("%s(%s)", getClass().getSimpleName(), format());
    }
}