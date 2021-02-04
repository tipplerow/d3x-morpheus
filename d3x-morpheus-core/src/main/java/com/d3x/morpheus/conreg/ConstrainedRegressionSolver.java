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

import lombok.Getter;
import lombok.NonNull;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.linalg.SVDSolver;
import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.util.LazyValue;

/**
 * Estimates the parameters for a constrained linear regression model.
 *
 * @param <R> the runtime type of the observation keys.
 * @param <C> the runtime type of the regressor and regressand keys.
 *
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
@lombok.extern.slf4j.Slf4j
public final class ConstrainedRegressionSolver<R,C> {
    /** The constrained regression model to be estimated. */
    @Getter @NonNull
    private final ConstrainedRegressionModel<C> regressionModel;

    /** The DataFrame containing sample observations and weights. */
    @Getter @NonNull
    private final DataFrame<R,C> observationFrame;

    /** The subset of observations to be used in the estimation. */
    @Getter @NonNull
    private List<R> observationRows;

    // Singular values below this threshold will be treated as if they are exactly
    // zero and they will be ignored in the calculation of regression coefficients.
    // A NaN value will call for the use of a default value based on the model and
    // problem size.
    private double singularValueThreshold = Double.NaN;

    // The augmented linear system and its SVD solver, built on demand and cached...
    private final LazyValue<SVDSolver> solver = LazyValue.of(this::buildSolver);
    private final LazyValue<ConstrainedRegressionSystem<R,C>> system = LazyValue.of(this::buildSystem);

    private ConstrainedRegressionSolver(ConstrainedRegressionModel<C> regressionModel, DataFrame<R, C> observationFrame) {
        this.regressionModel  = regressionModel;
        this.observationFrame = observationFrame;
        this.observationRows  = observationFrame.listRowKeys();

        validateState();
    }

    private void validateState() {
        validateObservationRows();
        validateObservationColumns();
        validateSingularValueThreshold();
    }

    private void validateObservationRows() {
        validateObservationRows(observationRows);
    }

    private void validateObservationRows(Iterable<R> rows) {
        observationFrame.requireRows(rows);
    }

    private void validateObservationColumns() {
        observationFrame.requireColumn(regressionModel.getRegressand());
        observationFrame.requireColumns(regressionModel.getRegressors());

        if (regressionModel.hasWeights())
            observationFrame.requireColumn(regressionModel.getWeight());
    }

    private void validateSingularValueThreshold() {
        validateSingularValueThreshold(singularValueThreshold);
    }

    private void validateSingularValueThreshold(double threshold) {
        if (!Double.isNaN(threshold))
            SVDSolver.validateThreshold(threshold);
    }

    private ConstrainedRegressionSystem<R,C> buildSystem() {
        log.info("Building the augmented linear system...");

        ConstrainedRegressionSystem<R,C> system =
                ConstrainedRegressionSystem.build(regressionModel, observationFrame, observationRows);

        return system;
    }

    private SVDSolver buildSolver() {
        log.info("Building the constrained regression solver...");
        validateSingularValueThreshold();

        D3xMatrix augmat = system.get().getAugmentedMatrix();
        SVDSolver solver = SVDSolver.apache(augmat);

        if (!Double.isNaN(singularValueThreshold))
            solver = solver.withThreshold(singularValueThreshold);

        return solver;
    }

    /**
     * Creates a new solver for a constrained regression model and observation frame.
     *
     * @param regressionModel  the regression model to estimate.
     * @param observationFrame a DataFrame containing the sample observations.
     *
     * @return a new solver for the specified model and sample.
     *
     * @throws RuntimeException unless the observation frame contains columns for all
     * variables in the regression model.
     */
    public static <R,C> ConstrainedRegressionSolver<R,C> build(
            ConstrainedRegressionModel<C> regressionModel, DataFrame<R,C> observationFrame) {
        return new ConstrainedRegressionSolver<>(regressionModel, observationFrame);
    }

    /**
     * Defines a subset of observations to be used in the parameter estimation.
     *
     * @param rows the subset of observations to be used for parameter estimation.
     *
     * @return this solver, updated.
     *
     * @throws RuntimeException unless the observation frame contains every specified row.
     */
    ConstrainedRegressionSolver<R,C> withObservationRows(List<R> rows) {
        validateObservationRows(rows);
        observationRows = rows;

        // New observations invalidate the augmented linear system and solver...
        system.reset();
        solver.reset();

        return this;
    }

    /**
     * Specifies a singular value threshold for the SVD solution of the constrained
     * normal equations.  Singular values that fall below the threshold are assumed
     * to be exactly zero and ignored in the calculation of regression coefficients.
     *
     * @param threshold the threshold for singular values.
     *
     * @return this solver, updated.
     *
     * @throws RuntimeException if the threshold is less than the machine tolerance.
     */
    ConstrainedRegressionSolver<R,C> withSingularValueThreshold(double threshold) {
        validateSingularValueThreshold(threshold);
        singularValueThreshold = threshold;

        // A new threshold does not invalidate the existing system or solver...
        solver.get().withThreshold(threshold);
        return this;
    }

    /**
     * Returns the augmented linear system that corresponds to the constrained
     * regression model and observation set.
     *
     * @return the augmented linear system that corresponds to the constrained
     * regression model and observation set.
     */
    public ConstrainedRegressionSystem<R,C> getAugmentedSystem() {
        return system.get();
    }

    /**
     * Estimates the parameters in the constrained regression model.
     *
     * @return the estimated parameters for the constrained regression
     * model given the observation set.
     */
    public ConstrainedRegressionResult<R,C> solve() {
        int N = regressionModel.countRegressors();
        int P = regressionModel.countConstraints();

        D3xVector solution = solver.get().solve(system.get().getAugmentedVector());
        assert solution.length() == (N + P);

        D3xVector betaVector = solution.getSubVector(0, N);
        D3xVector dualVector = solution.getSubVector(N, P);

        D3xMatrix designMatrix = system.get().getDesignMatrix();
        D3xVector observations = system.get().getRegressandVector();
        D3xVector fittedVector = designMatrix.times(betaVector);
        D3xVector residualVector = fittedVector.minus(observations);

        DataFrame<ConstrainedRegressionField, C> betaFrame =
                DataFrame.ofDoubles(ConstrainedRegressionField.BETA, regressionModel.getRegressors(), betaVector);

        DataFrame<ConstrainedRegressionField, String> dualFrame =
                DataFrame.ofDoubles(ConstrainedRegressionField.DUAL, regressionModel.getConstraintKeys(), dualVector);

        DataFrame<ConstrainedRegressionField, R> fittedFrame =
                DataFrame.ofDoubles(ConstrainedRegressionField.FITTED, observationRows, fittedVector);

        DataFrame<ConstrainedRegressionField, R> residualFrame =
                DataFrame.ofDoubles(ConstrainedRegressionField.RESIDUAL, observationRows, residualVector);

        return new ConstrainedRegressionResult<>(betaFrame, dualFrame, fittedFrame, residualFrame);
    }

    /**
     * Computes an effective pseudo-inverse for the constrained regression
     * system.
     *
     * <p>The effective pseudo-inverse is a matrix {@code Q} with shape {@code (N + P) x (M + P)},
     * where {@code N} is the number of regressor variables, {@code M} the number of observations,
     * and {@code P} is the number of linear constraints on the regression coefficients.  Given the
     * pseudo-inverse matrix {@code Q}, the solution vector for the constrained regression system is
     * computed as follows: {@code (beta, dual)' = Q * (obs, con)'}, where {@code (beta, dual)'} is
     * an {@code (N + P) x 1} column vector containing the regression coefficients stacked over the
     * dual values for the constraints and {@code (obs, con)'} is an {@code (M + P) x 1} column vector
     * containing the observations stacked on the constraint right-hand side values.  In a system with
     * no constraints and equally-weighted observations, this effective pseudo-inverse reduces to the
     * standard least-squares pseudo-inverse {@code inv(A'A) * A'}, where {@code A} is the design matrix
     * for the regression model.</p>
     *
     * @return an effective pseudo-inverse for the constrained regression
     * system.
     */
    public D3xMatrix computePseudoInverse() {
        D3xMatrix augInv = solver.get().invert();
        D3xMatrix blockR = buildPseudoInverseRightBlock();

        return augInv.times(blockR);
    }

    private D3xMatrix buildPseudoInverseRightBlock() {
        int M = observationRows.size();
        int N = regressionModel.countRegressors();
        int P = regressionModel.countConstraints();

        D3xMatrix blockR = D3xMatrix.dense(N + P, M + P);

        blockR.setSubMatrix(0, 0, system.get().getTwoATW());
        blockR.setSubMatrix(N, M, D3xMatrix.identity(P));

        return blockR;
    }
}
