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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;

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
    /**
     * The constrained regression model to be estimated.
     */
    @Getter @NonNull
    private final ConstrainedRegressionModel<R,C> regressionModel;

    /**
     * The keys of the regressor variables.
     */
    @Getter @NonNull
    private final List<C> regressorKeys;

    /**
     * The keys of the observations to include in the regression.
     */
    @Getter @NonNull
    private final List<R> observationKeys;

    /**
     * The design matrix {@code A} for the linear regression {@code Ax = b},
     * populated from the underlying regression model and observation sample.
     */
    @Getter @NonNull
    private final D3xMatrix designMatrix;

    /**
     * The observation vector {@code b} for the linear regression {@code Ax = b},
     * populated from the underlying regression model and observation sample.
     */
    @Getter @NonNull
    private final D3xVector regressandVector;

    /** The vector of observation weights. */
    @Getter @NonNull
    private final D3xVector weightVector;

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
    private final D3xMatrix augmentedMatrix;

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
    private final D3xVector augmentedVector;

    /**
     * The intermediate quantity {@code 2A'W}, twice the product of the transpose
     * of the design matrix and the regression weight matrix, required to build
     * the augmented matrix, the augmented vector, and the pseudo-inverse of the
     * augmented matrix.
     */
    @Getter(AccessLevel.PACKAGE) @NonNull
    private final D3xMatrix twoATW;

    private ConstrainedRegressionSystem(ConstrainedRegressionModel<R,C> regressionModel) {
        this.regressionModel = regressionModel;

        this.regressorKeys = regressionModel.getRegressorKeys();
        this.observationKeys = regressionModel.getObservationKeys();

        this.designMatrix = buildDesignMatrix();
        this.weightVector = buildWeightVector();
        this.regressandVector = buildRegressandVector();

        this.twoATW = computeTwoATW();
        this.augmentedMatrix = buildAugmentedMatrix();
        this.augmentedVector = buildAugmentedVector();
    }

    private D3xMatrix buildDesignMatrix() {
        return D3xMatrix.copyFrame(regressionModel.getRegressorFrame(), observationKeys, regressorKeys);
    }

    private D3xVector buildWeightVector() {
        D3xVector weights = D3xVector.copyOf(regressionModel.getObservationWeights(), observationKeys, 0.0);

        // Ensure that weights are non-negative, count the number of positive
        // (non-zero) weights, and compute the total weight...
        int positiveCount = 0;
        double totalWeight = 0.0;

        for (int index = 0; index < weights.length(); index++) {
            double weight = weights.get(index);

            if (DoubleComparator.DEFAULT.isNegative(weight))
                throw new MorpheusException("Regression weight for observation [%s] is negative.", observationKeys.get(index));
            else if (DoubleComparator.DEFAULT.isPositive(weight))
                positiveCount++;

            totalWeight += weight;
        }

        // The estimation should be more stable if the non-zero weights are of order one,
        // that is, they sum to the total number of non-zero weights...
        weights.multiplyInPlace(positiveCount / totalWeight);
        return weights;
    }

    private D3xVector buildRegressandVector() {
        return regressionModel.getRegressandSeries().getRequired(observationKeys);
    }

    private D3xMatrix computeTwoATW() {
        return designMatrix.transpose().times(D3xMatrix.diagonal(weightVector.times(2.0)));
    }

    private D3xMatrix buildAugmentedMatrix() {
        //
        // Builds the augmented matrix:
        //
        //    +-            -+
        //    |  2A'WA   C'  |
        //    |              |
        //    |    C     0   |
        //    +-            -+
        //
        D3xMatrix A = designMatrix;
        D3xMatrix C = regressionModel.getConstraintSet().getConstraintMatrix(regressorKeys);
        D3xMatrix CT = C.transpose();
        D3xMatrix twoATWA = twoATW.times(A);

        int N = A.ncol();
        int P = C.nrow();
        D3xMatrix augmat = D3xMatrix.dense(N + P, N + P);

        augmat.setSubMatrix(0, 0, twoATWA);
        augmat.setSubMatrix(0, N, CT);
        augmat.setSubMatrix(N, 0, C);

        return augmat;
    }

    private D3xVector buildAugmentedVector() {
        //
        // Builds the augmented vector:
        //
        //    +-       -+
        //    |  2A'Wb  |
        //    |         |
        //    |    d    |
        //    +-       -+
        //
        D3xMatrix A = designMatrix;
        D3xVector b = regressandVector;
        D3xVector d = regressionModel.getConstraintSet().getConstraintValues();
        D3xVector twoATWb = twoATW.times(b);

        int N = A.ncol();
        int P = d.length();
        D3xVector augvec = D3xVector.dense(N + P);

        augvec.setSubVector(0, twoATWb);
        augvec.setSubVector(N, d);

        return augvec;
    }

    /**
     * Creates a new augmented linear system for a given constrained regression model.
     *
     * @param regressionModel the regression model to estimate.
     *
     * @return a new augmented linear system for the specified model.
     */
    public static <R,C> ConstrainedRegressionSystem<R,C> build(ConstrainedRegressionModel<R,C> regressionModel) {
        return new ConstrainedRegressionSystem<>(regressionModel);
    }
}
