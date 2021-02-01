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

import java.util.Random;

import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.vector.D3xVector;

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

        double[] dataB = new double[] { 10.0, 2.0, 20.0, 30.0 };

        D3xMatrix A = D3xMatrix.wrap(dataA);
        D3xVector b = D3xVector.wrap(dataB);

        D3xVector x = SVDSolver.apache(A).solve(b);
        assertTrue(SVDSolver.isExactSolution(A, x, b));

        assertEquals(x.get(0), -3.991750, 0.000001);
        assertEquals(x.get(1),  4.691809, 0.000001);
        assertEquals(x.get(2),  1.817325, 0.000001);
        assertEquals(x.get(3), -0.210961, 0.000001);

        D3xMatrix actualInverse = SVDSolver.apache(A).invert();
        D3xMatrix expectedInverse =
                D3xMatrix.byrow(4, 4,
                         0.5604007, -0.47554508,  0.068061285, -0.33352976,
                        -0.2206836,  0.19357690, -0.042575133,  0.24543312,
                        -0.1231585,  0.10135533, -0.007071302,  0.09958751,
                         0.3126105, -0.05391868,  0.009575722, -0.11402475);

        DoubleComparator comparator = DoubleComparator.relative(1.0E-06);
        assertTrue(actualInverse.equalsMatrix(expectedInverse, comparator));

        assertTrue(actualInverse.times(A).equalsMatrix(D3xMatrix.identity(4)));
        assertTrue(A.times(actualInverse).equalsMatrix(D3xMatrix.identity(4)));
    }

    @Test
    public void testSquareLarge() {
        int N = 200;

        D3xMatrix A = D3xMatrix.random(N, N, random);
        D3xVector b = D3xVector.random(N, random);
        D3xVector x = SVDSolver.apache(A).solve(b);

        assertTrue(SVDSolver.isExactSolution(A, x, b));

        D3xMatrix invA = SVDSolver.apache(A).invert();
        assertTrue(invA.times(A).equalsMatrix(D3xMatrix.identity(N)));
        assertTrue(A.times(invA).equalsMatrix(D3xMatrix.identity(N)));
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

        D3xVector x = D3xVector.wrap(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0);
        D3xVector y = D3xVector.dense(x.length());
        D3xMatrix A = D3xMatrix.dense(x.length(), 3);

        for (int obs = 0; obs < x.length(); obs++)
            y.set(obs, b0 + b1 * x.get(obs) + b2 * x.get(obs) * x.get(obs) + 0.01 * random.nextDouble());

        for (int row = 0; row < x.length(); row++) {
            A.set(row, 0, 1.0);
            A.set(row, 1, x.get(row));
            A.set(row, 2, x.get(row) * x.get(row));
        }

        D3xVector b = SVDSolver.apache(A).solve(y);

        assertEquals(b.get(0), b0, 0.01);
        assertEquals(b.get(1), b1, 0.01);
        assertEquals(b.get(2), b2, 0.01);

        assertTrue(SVDSolver.isLeastSquaresSolution(A, b, y));

        D3xMatrix invATA = SVDSolver.apache(A.transpose().times(A)).invert();
        D3xVector exactB = invATA.times(A.transpose()).times(y);
        assertTrue(exactB.equalsVector(b));
    }

    @Test
    public void testLeastSquaresLarge() {
        int M = 1000;
        int N = 50;

        D3xMatrix A = D3xMatrix.random(M, N, random);
        D3xVector b = D3xVector.random(M, random);
        D3xVector x = SVDSolver.apache(A).solve(b);

        assertTrue(SVDSolver.isLeastSquaresSolution(A, x, b));

        D3xMatrix invATA = SVDSolver.apache(A.transpose().times(A)).invert();
        D3xVector exactX = invATA.times(A.transpose()).times(b);
        assertTrue(exactX.equalsVector(x));
    }
}
