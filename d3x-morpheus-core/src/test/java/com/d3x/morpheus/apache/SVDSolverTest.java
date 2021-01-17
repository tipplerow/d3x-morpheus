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

import java.util.Random;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import com.d3x.morpheus.util.DoubleComparator;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SVDSolverTest {
    private static final Random random = new Random(20210116);

    @Test
    public void testSquareSmall() {
        double[][] dataA = new double[][]{
                {  1.0,   2.0,   3.0,  4.0 },
                { -3.0,  -5.0,   8.0,  5.0 },
                { 10.0, -22.0,  90.0,  2.0 },
                {  5.0,   6.0,  12.0,  0.0 }
        };

        double[] datab = new double[] { 10.0, 2.0, 20.0, 30.0 };

        RealMatrix A = new BlockRealMatrix(dataA);
        RealVector b = new ArrayRealVector(datab);

        RealVector x = SVDSolver.build(A).solve(b);
        assertTrue(SVDSolver.computeRSS(A, x, b) < DoubleComparator.epsilon());

        assertEquals(x.getEntry(0), -3.991750, 0.000001);
        assertEquals(x.getEntry(1),  4.691809, 0.000001);
        assertEquals(x.getEntry(2),  1.817325, 0.000001);
        assertEquals(x.getEntry(3), -0.210961, 0.000001);
    }

    @Test
    public void testSquareLarge() {
        int N = 200;

        RealMatrix A = new BlockRealMatrix(N, N);
        RealVector b = new ArrayRealVector(N);

        for (int row = 0; row < N; row++) {
            b.setEntry(row, random.nextDouble());

            for (int col = 0; col < N; col++)
                A.setEntry(row, col, random.nextDouble());
        }

        RealVector x = SVDSolver.build(A).solve(b);
        assertTrue(SVDSolver.computeRSS(A, x, b) < DoubleComparator.epsilon());
    }

    @Test
    public void testLeastSquaresSmall() {
        //
        // Set up a linear regression to fit the quadratic function
        // y(x) = 5 - 3x + x^2, with some noise in the observations...
        //
        double b0 =  5.0;
        double b1 = -3.0;
        double b2 =  1.0;

        double[] x = new double[] { -3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0 };
        double[] y = new double[x.length];
        double[][] A = new double[x.length][];

        for (int obs = 0; obs < x.length; obs++)
            y[obs] = b0 + b1 * x[obs] + b2 * x[obs] * x[obs] + 0.01 * random.nextDouble();

        for (int row = 0; row < x.length; row++)
            A[row] = new double[] { 1.0, x[row], x[row] * x[row] };

        double[] b = SVDSolver.build(A).solve(y);

        assertEquals(b[0], b0,0.01);
        assertEquals(b[1], b1,0.01);
        assertEquals(b[2], b2,0.01);
    }

    @Test
    public void testLeastSquaresLarge() {
        int M = 1000;
        int N = 50;

        RealMatrix A = new BlockRealMatrix(M, N);
        RealVector b = new ArrayRealVector(M);

        for (int row = 0; row < M; row++) {
            b.setEntry(row, random.nextDouble());

            for (int col = 0; col < N; col++)
                A.setEntry(row, col, random.nextDouble());
        }

        RealVector x = SVDSolver.build(A).solve(b);
        double minRSS = SVDSolver.computeRSS(A, x, b);

        // The vector "x" should be the least-squares solution,
        // so any changes to it should produce an error vector
        // with a larger norm...
        for (int test = 0; test < 1000; test++) {
            int index = random.nextInt(x.getDimension());
            double delta = 0.01 * random.nextDouble();
            x.addToEntry(index, delta);
            assertTrue(SVDSolver.computeRSS(A, x, b) > minRSS);
            x.addToEntry(index, -delta);
        }
    }
}
