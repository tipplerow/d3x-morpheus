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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class SVDSolverTest {
    @Test
    public void testSquare() {
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

        assertEquals(x.getEntry(0), -3.991750, 0.000001);
        assertEquals(x.getEntry(1),  4.691809, 0.000001);
        assertEquals(x.getEntry(2),  1.817325, 0.000001);
        assertEquals(x.getEntry(3), -0.210961, 0.000001);
    }
}
