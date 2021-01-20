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
package com.d3x.morpheus.conreg;

import java.util.List;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import lombok.Getter;
import lombok.NonNull;

import com.d3x.core.lang.D3xException;
import com.d3x.morpheus.apache.ApacheColumnVector;
import com.d3x.morpheus.apache.ApacheMatrix;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.DoubleComparator;

/**
 * Encapsulates the augmented linear system that must be solved for the parameters
 * of a constrained linear regression model.
 *
 * @param <R> the runtime type of the observation keys.
 * @param <C> the runtime type of the regressor and regressand keys.
 *
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public final class ConstrainedRegressionSystem<R,C> {
    /** The constrained regression model to be estimated. */
    @Getter @NonNull
    private final ConstrainedRegressionModel<C> regressionModel;

    /** The DataFrame containing sample observations and weights. */
    @Getter @NonNull
    private final DataFrame<R,C> observationFrame;

    /** The subset of observations to be used in the estimation. */
    @Getter @NonNull
    private final List<R> observationRows;

    /**
     * The design matrix {@code A} for the linear regression {@code Ax = b},
     * populated from the underlying regression model and observation sample.
     */
    @Getter @NonNull
    private final RealMatrix designMatrix;

    /**
     * The observation vector {@code b} for the linear regression {@code Ax = b},
     * populated from the underlying regression model and observation sample.
     */
    @Getter @NonNull
    private final RealVector regressandVector;

    /** The vector of observation weights. */
    @Getter @NonNull
    private final RealVector weightVector;

    /**
     * The matrix of coefficients for the augmented linear system that defines
     * the constrained regression.
     *
     * <p>Let {@code M} be the number of observations, {@code N} the number of
     * regressor variables, and {@code P} the number of constraints. Then the
     * design matrix {@code A} has dimensions {@code M x N} and the constraint
     * matrix {@code C} has dimensions {@code P x N}.  The augmented matrix is
     * a square matrix with {@code N + P} rows and columns arranged as follows:
     * <pre>
     *     2A'WA  C'
     *     C      0
     * </pre>
     * where {@code W} is an {@code M x M} diagonal matrix of regression weights,
     * the prime character {@code '} denotes the matrix transpose and {@code 0}
     * is a {@code P x P} matrix of zeros.
     * </p>
     */
    @Getter @NonNull
    private final RealMatrix augmentedMatrix;

    /**
     * The right-hand side vector of values for the augmented linear system that
     * defines the constrained regression.
     *
     * <p>Let {@code M} be the number of observations, {@code N} the number of
     * regressor variables, and {@code P} the number of constraints. Then the
     * design matrix {@code A} has dimensions {@code M x N}, the regressand
     * vector {@code b} has dimensions {@code N x 1}, and constraint vector
     * {@code d} has dimensions {@code P x 1}.  The augmented right-hand side
     * vector has {@code N + P} rows and columns arranged as follows:
     * <pre>
     *     2A'Wb
     *     d
     * </pre>
     * where {@code W} is an {@code M x M} diagonal matrix of regression weights
     * and the prime character {@code '} denotes the matrix transpose.
     * </p>
     */
    @Getter @NonNull
    private final RealVector augmentedVector;

    // Intermediate quantity required to build the augmented matrix and vector:
    // The transpose of the design matrix (A), multiplied by the diagonal weight
    // matrix (W) and a constant factor of 2.0...
    private final RealMatrix twoATW;

    private ConstrainedRegressionSystem(ConstrainedRegressionModel<C> regressionModel, DataFrame<R, C> observationFrame, List<R> observationRows) {
        this.regressionModel  = regressionModel;
        this.observationFrame = observationFrame;
        this.observationRows  = observationRows;

        validateObservationRows();
        validateObservationColumns();

        this.designMatrix = buildDesignMatrix();
        this.weightVector = buildWeightVector();
        this.regressandVector = buildRegressandVector();

        this.twoATW = computeTwoATW();
        this.augmentedMatrix = buildAugmentedMatrix();
        this.augmentedVector = buildAugmentedVector();
    }

    private void validateObservationRows() {
        observationFrame.requireRows(observationRows);
    }

    private void validateObservationColumns() {
        observationFrame.requireColumn(regressionModel.getRegressand());
        observationFrame.requireColumns(regressionModel.getRegressors());

        if (regressionModel.hasWeights())
            observationFrame.requireColumn(regressionModel.getWeight());
    }

    private RealMatrix buildDesignMatrix() {
        return ApacheMatrix.wrap(observationFrame, observationRows, regressionModel.getRegressors()).copy();
    }

    private RealVector buildWeightVector() {
        if (!regressionModel.hasWeights())
            return new ArrayRealVector(observationRows.size(), 1.0);

        RealVector weights = ApacheColumnVector.wrap(observationFrame, regressionModel.getWeight(), observationRows).copy();

        // Ensure that weights are non-negative, count the number of positive (non-zero) weights, and compute the total weight...
        int positiveCount = 0;
        double totalWeight = 0.0;

        for (int index = 0; index < weights.getDimension(); index++) {
            double weight = weights.getEntry(index);

            if (DoubleComparator.DEFAULT.isNegative(weight))
                throw new D3xException("Regression weight for observation [%s] is negative.", observationRows.get(index));
            else if (DoubleComparator.DEFAULT.isPositive(weight))
                positiveCount++;

            totalWeight += weight;
        }

        // The estimation should be more stable if the non-zero weights are of order one,
        // that is, they sum to the total number of non-zero weights...
        weights.mapMultiplyToSelf(positiveCount / totalWeight);
        return weights;
    }

    private RealVector buildRegressandVector() {
        return ApacheColumnVector.wrap(observationFrame, regressionModel.getRegressand(), observationRows).copy();
    }

    private RealMatrix computeTwoATW() {
        return designMatrix.transpose().scalarMultiply(2.0).multiply(new DiagonalMatrix(weightVector.toArray()));
    }

    private RealMatrix buildAugmentedMatrix() {
        //
        // Builds the augmented matrix:
        //
        //    +-            -+
        //    |  2A'WA   C'  |
        //    |              |
        //    |    C     0   |
        //    +-            -+
        //
        RealMatrix A = designMatrix;
        RealMatrix C = regressionModel.getConstraintMatrix();
        RealMatrix CT = C.transpose();
        RealMatrix twoATWA = twoATW.multiply(A);

        int N = A.getColumnDimension();
        int P = C.getRowDimension();
        RealMatrix augmat = new BlockRealMatrix(N + P, N + P);

        augmat.setSubMatrix(twoATWA.getData(),0, 0);
        augmat.setSubMatrix(CT.getData(), 0, N);
        augmat.setSubMatrix(C.getData(), N, 0);

        return augmat;
    }

    private RealVector buildAugmentedVector() {
        //
        // Builds the augmented vector:
        //
        //    +-       -+
        //    |  2A'Wb  |
        //    |         |
        //    |    d    |
        //    +-       -+
        //
        RealMatrix A = designMatrix;
        RealVector b = regressandVector;
        RealVector d = regressionModel.getConstraintValues();
        RealVector twoATWb = twoATW.operate(b);

        int N = A.getColumnDimension();
        int P = d.getDimension();
        RealVector augvec = new ArrayRealVector(N + P);

        augvec.setSubVector(0, twoATWb);
        augvec.setSubVector(N, d);

        return augvec;
    }


    /**
     * Creates a new augmented linear system for a given constrained regression model
     * and observation frame (using all rows in the observation frame).
     *
     * @param regressionModel  the regression model to estimate.
     * @param observationFrame a DataFrame containing the sample observations.
     *
     * @return a new augmented linear system for the specified model and sample.
     *
     * @throws RuntimeException unless the observation frame contains columns for all
     * variables in the regression model.
     */
    public static <R,C> ConstrainedRegressionSystem<R,C> build(
            ConstrainedRegressionModel<C> regressionModel, DataFrame<R, C> observationFrame) {
        return build(regressionModel, observationFrame, observationFrame.listRowKeys());
    }

    /**
     * Creates a new augmented linear system for a given constrained regression model
     * and observation frame.
     *
     * @param regressionModel  the regression model to estimate.
     * @param observationFrame a DataFrame containing the sample observations.
     * @param observationRows  a subset of rows to include in the estimation.
     *
     * @return a new augmented linear system for the specified model and sample.
     *
     * @throws RuntimeException unless the observation frame contains columns for all
     * variables in the regression model and rows for all specified observations.
     */
    public static <R,C> ConstrainedRegressionSystem<R,C> build(
            ConstrainedRegressionModel<C> regressionModel, DataFrame<R, C> observationFrame, List<R> observationRows) {
        return new ConstrainedRegressionSystem<>(regressionModel, observationFrame, observationRows);
    }
}
