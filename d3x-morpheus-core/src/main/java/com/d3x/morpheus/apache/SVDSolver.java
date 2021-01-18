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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import com.d3x.core.lang.D3xException;
import com.d3x.morpheus.stats.Max;
import com.d3x.morpheus.util.DoubleComparator;

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
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public final class SVDSolver {
    private final SingularValueDecomposition svd;

    // Singular values below this threshold will be treated as if they are exactly
    // zero and ignored when computing solution vectors...
    private double threshold;

    // The machine tolerance...
    private static final double epsilon = DoubleComparator.epsilon();

    private SVDSolver(RealMatrix matrixA) {
        this.svd = new SingularValueDecomposition(matrixA);
        this.threshold = defaultThreshold(svd);
        validateThreshold();
    }

    private void validateThreshold() {
        validateThreshold(this.threshold);
    }

    /**
     * Creates an SVD solver for a system of linear equations using a default
     * singular value threshold.
     *
     * @param matrixA the matrix of coefficients in the linear system.
     *
     * @return an SVD solver for the specified linear system.
     */
    public static SVDSolver build(double[][] matrixA) {
        return build(new BlockRealMatrix(matrixA));
    }

    /**
     * Creates an SVD solver for a system of linear equations using a default
     * singular value threshold.
     *
     * @param matrixA the matrix of coefficients in the linear system.
     *
     * @return an SVD solver for the specified linear system.
     */
    public static SVDSolver build(RealMatrix matrixA) {
        return new SVDSolver(matrixA);
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
    public static double defaultThreshold(SingularValueDecomposition svd) {
        //
        // See the SVD::solve method in Section 2.6 of Numerical Recipes, 3rd Edition...
        //
        int M = svd.getU().getRowDimension();
        int N = svd.getU().getColumnDimension();
        double wmax = Max.of(svd.getSingularValues());

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
     * Computes the error vector {@code A * x - b} for the solution to a linear system.
     *
     * @param A the {@code M x N} design matrix for the system.
     * @param x the {@code N x 1} solution vector for the system.
     * @param b the {@code M x 1} observation (right-hand side) vector for the system.

     * @return the error vector {@code A * x - b}.
     */
    public static RealVector computeResidual(RealMatrix A, RealVector x, RealVector b) {
        return A.operate(x).subtract(b);
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
    public static double computeRSS(RealMatrix A, RealVector x, RealVector b) {
        double rssNorm = computeResidual(A, x, b).getNorm();
        return rssNorm * rssNorm;
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
    public static boolean isExactSolution(RealMatrix A, RealVector x, RealVector b) {
        if (A.isSquare())
            return DoubleComparator.DEFAULT.equals(A.operate(x), b);
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
    public static boolean isLeastSquaresSolution(RealMatrix A, RealVector x, RealVector b) {
        //
        // If the vector "x" is the true least-squares solution, then any
        // changes to it will produce a larger residual sum of squares...
        //
        double minRSS = computeRSS(A, x, b);
        RealVector testX = x.copy();

        for (int index = 0; index < x.getDimension(); index++) {
            //
            // Increase and decrease this element by one percent and
            // examine the residual sum of squares...
            //
            double xi = x.getEntry(index);
            double dx = 0.01 * xi;

            testX.setEntry(index, xi + dx);

            if (computeRSS(A, testX, b) < minRSS)
                return false;

            testX.setEntry(index, xi - dx);

            if (computeRSS(A, testX, b) < minRSS)
                return false;

            testX.setEntry(index, xi);
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
        return svd.getU().getColumnDimension();
    }

    /**
     * Returns the row dimension of the coefficient matrix.
     *
     * @return the row dimension of the coefficient matrix.
     */
    public int getRowDimension() {
        return svd.getU().getRowDimension();
    }

    /**
     * Returns the singular value decomposition of the coefficient matrix.
     *
     * @return the singular value decomposition of the coefficient matrix.
     */
    public SingularValueDecomposition getSVD() {
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
    public RealMatrix invertSingularValues() {
        double[] values = svd.getSingularValues();
        RealMatrix inverse = new DiagonalMatrix(values.length);

        for (int index = 0; index < values.length; index++) {
            double value = values[index];

            if (value > threshold)
                inverse.setEntry(index, index, 1.0 / value);
        }

        return inverse;
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
    public double[] solve(double[] vectorB) {
        RealMatrix matrixB = new Array2DRowRealMatrix(vectorB);
        return solve(matrixB).getColumnVector(0).toArray();
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
    public RealVector solve(RealVector vectorB) {
        RealMatrix matrixB = new Array2DRowRealMatrix(vectorB.getDimension(), 1);
        matrixB.setColumnVector(0, vectorB);
        return solve(matrixB).getColumnVector(0);
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
    public RealMatrix solve(RealMatrix matrixB) {
        RealMatrix UT_B = svd.getUT().multiply(matrixB);
        RealMatrix Winv_UT_B = invertSingularValues().multiply(UT_B);
        RealMatrix V_Winv_UT_B = svd.getV().multiply(Winv_UT_B);

        return V_Winv_UT_B;
    }
}
