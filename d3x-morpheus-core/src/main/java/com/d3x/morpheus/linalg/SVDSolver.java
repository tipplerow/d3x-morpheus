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
package com.d3x.morpheus.linalg;

import lombok.NonNull;

import com.d3x.core.lang.D3xException;
import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.stats.Max;
import com.d3x.morpheus.stats.SumSquares;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.vector.D3xVector;

/**
 * Solves systems of linear equations by singular value decomposition of the
 * coefficient matrix.
 *
 * <p><b>Singular value threshold.</b> The solver ignores basis vectors whose
 * corresponding singular values fall below a threshold so that singular and
 * near-singular systems are treated robustly.  Given an existing solver, the
 * threshold may be updated by calling {@code solver.withThreshold()} without
 * invalidating the underlying decomposition. Using this capability, multiple
 * thresholds may be tested and the sensitivity of solutions to the threshold
 * may be established efficiently.</p>
 *
 * @author  Scott Shaffer
 */
public final class SVDSolver {
    @NonNull private final SVD svd;

    // Singular values below this threshold will be treated as if they are exactly
    // zero and ignored when computing solution vectors...
    private double threshold;

    // The machine tolerance...
    private static final double epsilon = DoubleComparator.epsilon();

    private SVDSolver(SVD svd) {
        this.svd = svd;
        this.threshold = defaultThreshold(svd);
        validateThreshold();
    }

    private void validateThreshold() {
        validateThreshold(this.threshold);
    }


    /**
     * Creates an SVD solver for a system of linear equations with the default
     * singular value threshold (using the Apache Commons Math library for the
     * decomposition).
     *
     * @param matrixA the matrix of coefficients in the linear system.
     *
     * @return an SVD solver for the specified linear system.
     */
    public static SVDSolver apache(D3xMatrix matrixA) {
        return new SVDSolver(SVD.apache(matrixA));
    }

    /**
     * Returns the default singular value threshold for a given decomposition.
     *
     * <p>We use the expression implemented in the {@code SVD::solve} method from
     * Section 2.6 of <em>Numerical Recipes, 3rd Edition</em>, which is a function
     * of the dimensions of the linear system, the maximum singular value, and the
     * machine tolerance.
     *
     * @param svd a singular value decomposition of a coefficient matrix.
     *
     * @return the default singular value threshold for the specified decomposition.
     */
    public static double defaultThreshold(SVD svd) {
        //
        // See the SVD::solve method in Section 2.6 of Numerical Recipes, 3rd Edition...
        //
        int M = svd.getRowDimension();
        int N = svd.getColumnDimension();
        double wmax = new Max().compute(svd.getSingularValueVector());

        return 0.5 * Math.sqrt(M + N + 1.0) * wmax * epsilon;
    }

    /**
     * Validates a singular value threshold.
     *
     * @param threshold the threshold to validate.
     *
     * @throws RuntimeException unless the input threshold is finite and greater than
     * the machine tolerance.
     */
    public static void validateThreshold(double threshold) {
        if (!Double.isFinite(threshold))
            throw new D3xException("The singular value threshold must be finite.");

        if (threshold < epsilon)
            throw new D3xException("The singular value threshold must be larger than the machine tolerance.");
    }

    /**
     * Computes the fitted or predicted values {@code (A * x)} for a linear system.
     *
     * @param A the {@code M x N} design matrix for the system.
     * @param x the {@code N x 1} solution vector for the system.

     * @return the fitted values, {@code A * x}.
     */
    public static D3xVector computeFittedValues(D3xMatrix A, D3xVector x) {
        return A.times(x);
    }

    /**
     * Computes the error vector {@code (A * x - b)} for the solution to a linear system.
     *
     * @param A the {@code M x N} design matrix for the system.
     * @param x the {@code N x 1} solution vector for the system.
     * @param b the {@code M x 1} observation (right-hand side) vector for the system.

     * @return the error vector {@code A * x - b}.
     */
    public static D3xVector computeResidual(D3xMatrix A, D3xVector x, D3xVector b) {
        return computeResidual(computeFittedValues(A, x), b);
    }

    /**
     * Computes the error vector {@code (A * x - b)} for the solution to a linear system.
     *
     * @param Ax the {@code M x 1} vector of predicted values {@code (A * x)}.
     * @param b the {@code M x 1} observation (right-hand side) vector for the system.
     *
     * @return the error vector {@code A * x - b}.
     */
    public static D3xVector computeResidual(D3xVector Ax, D3xVector b) {
        return Ax.minus(b);
    }

    /**
     * Computes the residual sum of squares for the solution to a linear system: the
     * sum of the squared values in the residual vector.
     *
     * @param A the {@code M x N} design matrix for the system.
     * @param x the {@code N x 1} solution vector for the system.
     * @param b the {@code M x 1} observation (right-hand side) vector for the system.

     * @return the residual sum of squares for the solution to a linear system.
     */
    public static double computeRSS(D3xMatrix A, D3xVector x, D3xVector b) {
        return computeRSS(computeResidual(A, x, b));
    }

    /**
     * Computes the residual sum of squares for the solution to a linear system: the
     * sum of the squared values in the residual vector.
     *
     * @param Ax the {@code M x 1} vector of predicted values {@code (A * x)}.
     * @param b the {@code M x 1} observation (right-hand side) vector for the system.

     * @return the residual sum of squares for the solution to a linear system.
     */
    public static double computeRSS(D3xVector Ax, D3xVector b) {
        return computeRSS(computeResidual(Ax, b));
    }

    /**
     * Computes the residual sum of squares for the solution to a linear system: the
     * sum of the squared values in the residual vector.
     *
     * @param residuals the {@code M x 1} vector of residual values.

     * @return the residual sum of squares for the solution to a linear system.
     */
    public static double computeRSS(D3xVector residuals) {
        return new SumSquares().compute(residuals);
    }

    /**
     * Tests the solution to a square linear system.
     *
     * @param A the {@code N x N} design matrix for the system.
     * @param x the {@code N x 1} solution vector for the system.
     * @param b the {@code N x 1} observation (right-hand side) vector for the system.
     *
     * @return {@code true} if {@code A} is a square design matrix and the specified
     * solution vector {@code x} is the true solution for the linear system.
     */
    public static boolean isExactSolution(D3xMatrix A, D3xVector x, D3xVector b) {
        if (A.isSquare())
            return A.times(x).equalsVector(b);
        else
            return false;
    }

    /**
     * Tests the least-squares solution to an overdetermined linear system by searching
     * for nearby solution vectors with a smaller residual sum of squares.
     *
     * @param A the {@code M x N} design matrix for the system, with {@code M > N}.
     * @param x the {@code N x 1} solution vector for the system.
     * @param b the {@code M x 1} observation (right-hand side) vector for the system.
     *
     * @return {@code true} if the specified solution vector {@code x} is the true
     * least-squares solution for the linear system.
     */
    public static boolean isLeastSquaresSolution(D3xMatrix A, D3xVector x, D3xVector b) {
        //
        // If the vector "x" is the true least-squares solution, then any
        // changes to it will produce a larger residual sum of squares...
        //
        double minRSS = computeRSS(A, x, b);
        D3xVector testX  = x.copy();

        for (int index = 0; index < x.length(); index++) {
            //
            // Increase and decrease this element by one percent and
            // examine the residual sum of squares...
            //
            double xi = x.get(index);
            double dx = 0.01 * xi;

            testX.set(index, xi + dx);

            if (computeRSS(A, testX, b) < minRSS)
                return false;

            testX.set(index, xi - dx);

            if (computeRSS(A, testX, b) < minRSS)
                return false;

            testX.set(index, xi);
        }

        return true;
    }

    /**
     * Specifies a singular value threshold for this solver. Singular values that
     * fall below the threshold are assumed to be exactly zero and ignored in the
     * calculation of solution vectors.
     *
     * @param threshold the threshold for singular values.
     *
     * @return this solver, updated.
     *
     * @throws RuntimeException if the threshold is less than the machine tolerance
     * (approximately {@code 2.2E-16}).
     */
    public SVDSolver withThreshold(double threshold) {
        validateThreshold(threshold);
        this.threshold = threshold;
        return this;
    }

    /**
     * Returns the column dimension of the coefficient matrix.
     *
     * @return the column dimension of the coefficient matrix.
     */
    public int getColumnDimension() {
        return svd.getColumnDimension();
    }

    /**
     * Returns the row dimension of the coefficient matrix.
     *
     * @return the row dimension of the coefficient matrix.
     */
    public int getRowDimension() {
        return svd.getRowDimension();
    }

    /**
     * Returns the singular value decomposition of the coefficient matrix.
     *
     * @return the singular value decomposition of the coefficient matrix.
     */
    public SVD getSVD() {
        return svd;
    }

    /**
     * Returns the singular value threshold for this solver. Singular values that
     * fall below the threshold are assumed to be exactly zero and ignored in the
     * calculation of solution vectors.
     *
     * @return the singular value threshold for this solver.
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * Returns the diagonal matrix of inverted singular values used to solve the
     * linear system.
     *
     * <p>Let {@code w} be the vector of singular values and {@code D} be the
     * diagonal matrix of inverted singular values returned by this method. The
     * entry {@code D(k, k)} is equal to {@code 1.0 / w(k)} if {@code w(k)} is
     * greater than the singular value threshold but {@code 0.0} otherwise.</p>
     *
     * @return the diagonal matrix of inverted singular values used to solve the
     * linear system.
     */
    public D3xMatrix invertSingularValues() {
        D3xVector valueVector = svd.getSingularValueVector();
        D3xVector valueInverse = D3xVector.dense(valueVector.length());

        for (int index = 0; index < valueVector.length(); index++)
            valueInverse.set(index, invertSingularValue(valueVector.get(index)));

        return D3xMatrix.diagonal(valueInverse);
    }

    private double invertSingularValue(double singularValue) {
        if (singularValue > threshold)
            return 1.0 / singularValue;
        else
            return 0.0;
    }

    /**
     * Computes the inverse (or pseudoinverse) of the original matrix {@code A}.
     *
     * @return the inverse (or pseudoinverse) of the original matrix {@code A}.
     */
    public D3xMatrix invert() {
        D3xMatrix V = svd.getV();
        D3xMatrix UT = svd.getUT();
        D3xMatrix invW = invertSingularValues();

        return V.times(invW.times(UT));
    }

    /**
     * Computes the solution vector {@code x} of the linear system {@code A * x = b}
     *  for a given right-hand side vector {@code b}.
     *
     * @param vectorB the vector of right-hand side values.
     *
     * @return the solution of the linear system for the specified right-hand side.
     *
     * @throws RuntimeException unless the length of the input vector matches the
     * row dimension of the coefficient matrix.
     */
    public D3xVector solve(D3xVector vectorB) {
        D3xMatrix matrixB = D3xMatrix.dense(vectorB.length(), 1);
        matrixB.setColumn(0, vectorB);
        return solve(matrixB).getColumn(0);
    }

    /**
     * Computes the solution matrix {@code X} of the linear system {@code A * X = B}
     * for a given right-hand side matrix {@code B}.
     *
     * @param matrixB the matrix of right-hand side values.
     *
     * @return the solution of the linear system for the specified right-hand side.
     *
     * @throws RuntimeException unless the row dimension of the input matrix matches
     * the row dimension of the coefficient matrix.
     */
    public D3xMatrix solve(D3xMatrix matrixB) {
        D3xMatrix UT_B = svd.getUT().times(matrixB);
        D3xMatrix Winv_UT_B = invertSingularValues().times(UT_B);
        D3xMatrix V_Winv_UT_B = svd.getV().times(Winv_UT_B);

        return V_Winv_UT_B;
    }
}
