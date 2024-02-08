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
package com.d3x.morpheus.matrix;

import lombok.NonNull;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import com.d3x.morpheus.vector.D3xVector;

/**
 * Wraps an Apache RealMatrix implementation in a D3xMatrix interface.
 *
 * @author Scott Shaffer
 */
public final class ApacheMatrix implements D3xMatrix {
    @NonNull private final RealMatrix impl;

    // The number of elements in a single block of a BlockRealMatrix...
    private static final int BLOCK_ELEMENT_COUNT =
            BlockRealMatrix.BLOCK_SIZE * BlockRealMatrix.BLOCK_SIZE;

    // Use Array2DRowRealMatrix for matrices below this size,
    // but BlockRealMatrix for this size and above...
    private static final int BLOCK_THRESHOLD = 4 * BLOCK_ELEMENT_COUNT;

    private ApacheMatrix(@NonNull RealMatrix impl) {
        this.impl = impl;
    }

    /**
     * The single empty vector.
     */
    public static final ApacheMatrix EMPTY = wrap(new Array2DRowRealMatrix());

    /**
     * Returns a read-only RealMatrix view of a generic D3xMatrix to be used
     * as the operand in algebraic functions.  Modifying the returned matrix
     * is not permitted, and the results of doing so are undefined.
     *
     * @param matrix the algebraic operand.
     *
     * @return a read-only RealMatrix view of the specified operand.
     */
    public static RealMatrix asOperand(D3xMatrix matrix) {
        //
        // Finds the best RealMatrix implementation to use when operating on
        // generic D3xMatrix instances.
        //
        if (matrix instanceof ApacheMatrix)
            return ((ApacheMatrix) matrix).impl;

        if (matrix instanceof RealMatrix)
            return (RealMatrix) matrix;

        return new Array2DRowRealMatrix(matrix.toArray(), false);
    }

    /**
     * Creates a new matrix by copying values from a bare array.
     *
     * @param values the values to be copied.
     *
     * @return a new matrix containing a copy of the specified array.
     */
    public static ApacheMatrix copyOf(double[][] values) {
        return wrap(new Array2DRowRealMatrix(values));
    }

    /**
     * Creates a new matrix with dense physical storage and all elements
     * initialized to zero.
     *
     * @param nrow the number of matrix rows.
     * @param ncol the number of matrix columns.
     *
     * @return a new matrix with dense physical storage.
     *
     * @throws RuntimeException if either dimension is negative.
     */
    public static ApacheMatrix dense(int nrow, int ncol) {
        int size = nrow * ncol;

        if (size < BLOCK_THRESHOLD)
            return wrap(new Array2DRowRealMatrix(nrow, ncol));
        else
            return wrap(new BlockRealMatrix(nrow, ncol));
    }

    /**
     * Creates a new matrix with storage only for diagonal elements (all
     * diagonal elements initialized to zero).
     *
     * @param N the length of the diagonal.
     *
     * @return a new matrix with storage for only diagonal elements.
     *
     * @throws RuntimeException if the diagonal length is negative.
     */
    public static ApacheMatrix diagonal(int N) {
        return wrap(new DiagonalMatrix(N));
    }

    /**
     * Creates a new matrix with storage only for diagonal elements and
     * initializes those elements.
     *
     * @param diagonals the diagonal values to assign.
     *
     * @return a new matrix with storage for only diagonal elements with
     * those elements assigned by the input vector.
     */
    public static ApacheMatrix diagonal(D3xVector diagonals) {
        return wrap(new DiagonalMatrix(diagonals.toArray()));
    }

    /**
     * Creates a new matrix with sparse physical storage and all elements
     * initialized to zero.
     *
     * @param nrow the number of matrix rows.
     * @param ncol the number of matrix columns.
     *
     * @return a new matrix with sparse physical storage.
     *
     * @throws RuntimeException if either dimension is negative.
     */
    public static ApacheMatrix sparse(int nrow, int ncol) {
        return wrap(new OpenMapRealMatrix(nrow, ncol));
    }

    /**
     * Creates a mutable matrix view over a bare array (a shallow copy).
     * Changes to the returned matrix will be reflected in the input
     * array, and changes to the array will be reflected in the matrix.
     *
     * @param values the values to be wrapped.
     *
     * @return a new mutable matrix using the input array for its
     * physical storage.
     */
    public static ApacheMatrix wrap(double[][] values) {
        return wrap(new Array2DRowRealMatrix(values, false));
    }

    /**
     * Wraps an existing RealMatrix implementation in a D3xMatrix interface.
     *
     * @param matrix the RealMatrix implementation to wrap.
     *
     * @return a new D3xMatrix interface with the specified implementation.
     */
    public static ApacheMatrix wrap(RealMatrix matrix) {
        return new ApacheMatrix(matrix);
    }

    @Override
    public void add(int row, int col, double addend) {
        impl.addToEntry(row, col, addend);
    }

    @Override
    public ApacheMatrix copy() {
        return new ApacheMatrix(impl.copy());
    }

    @Override
    public double get(int row, int col) {
        return impl.getEntry(row, col);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public ApacheMatrix like(int nrow, int ncol) {
        return new ApacheMatrix(impl.createMatrix(nrow, ncol));
    }

    @Override
    public D3xMatrix multiplyInPlace(double scalar) {
        for (int i = 0; i < nrow(); ++i)
            for (int j = 0; j < ncol(); ++j)
                impl.multiplyEntry(i, j, scalar);

        return this;
    }

    @Override
    public int nrow() {
        return impl.getRowDimension();
    }

    @Override
    public int ncol() {
        return impl.getColumnDimension();
    }

    @Override
    public ApacheMatrix minus(double subtrahend) {
        return wrap(impl.scalarAdd(-subtrahend));
    }

    @Override
    public ApacheMatrix minus(D3xMatrix B) {
        return wrap(impl.subtract(asOperand(B)));
    }

    @Override
    public void multiply(int row, int col, double factor) {
        impl.multiplyEntry(row, col, factor);
    }

    @Override
    public ApacheMatrix plus(double addend) {
        return wrap(impl.scalarAdd(addend));
    }

    @Override
    public ApacheMatrix plus(D3xMatrix B) {
        return wrap(impl.add(asOperand(B)));
    }

    @Override
    public void set(int row, int col, double value) {
        impl.setEntry(row, col, value);
    }

    @Override
    public ApacheMatrix times(double scalar) {
        return wrap(impl.scalarMultiply(scalar));
    }

    @Override
    public D3xVector times(D3xVector x) {
        return D3xVector.wrap(impl.operate(x.toArray()));
    }

    @Override
    public ApacheMatrix times(D3xMatrix B) {
        return wrap(this.impl.multiply(asOperand(B)));
    }

    @Override
    public ApacheMatrix transpose() {
        return wrap(impl.transpose());
    }

    @Override
    public String toString() {
        return impl.toString();
    }
}