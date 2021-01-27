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

import java.util.Random;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.vector.D3xVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ConstrainedRegressionSolverTest extends ConstrainedRegressionTestBase {
    public static final Random random = new Random(20210120);

    private final ConstrainedRegressionModel<String> model;
    private final DataFrame<String, String> frame;
    private final ConstrainedRegressionSolver<String, String> solver;
    private final ConstrainedRegressionSystem<String, String> system;

    public ConstrainedRegressionSolverTest() {
        this.model = buildConstrainedModel();
        this.frame = buildObservationFrame(random);
        this.solver = ConstrainedRegressionSolver.build(model, frame);
        this.system = solver.getAugmentedSystem();
    }

    @Test
    public void testSolution() {
        //
        // The solution vector, computed separately in R...
        //
        // 15.2567 -0.4256  1.7128 -0.9699 -1.7853 -6.0586  7.8439 42.7805  0.0000
        //
        // Note that the optimal CONSTRAINED coefficients are fairly different
        // from the coefficients used to generate the observations, because the
        // unconstrained coefficients in the generating model do not satisfy the
        // constraints.
        //
        ConstrainedRegressionResult<String, String> result = solver.solve();
        DoubleComparator comparator0001 = DoubleComparator.fixed(0.0001);

        double[] betaActual = result.getBetaCoefficients().getDoubleMatrix()[0];
        double[] dualActual = result.getDualValues().getDoubleMatrix()[0];

        double[] betaExpected = new double[] { 15.2567, -0.4256,  1.7128, -0.9699, -1.7853, -6.0586,  7.8439 };
        double[] dualExpected = new double[] { 42.7805, 0.0 };

        assertTrue(comparator0001.equals(betaActual, betaExpected));
        assertTrue(comparator0001.equals(dualActual, dualExpected));

        // Ensure that the beta constraints are satisfied...
        assertEquals(betaActual[1] + 2.0 * betaActual[2], 3.0, 1.0E-09);
        assertEquals(betaActual[4] + betaActual[5] + betaActual[6], 0.0,1.0E-09);

        double[] fittedActual = result.getFittedValues().getDoubleMatrix()[0];
        double[] fittedExpected = new double[] {
                179.6608, 104.6542, 56.3516, 24.6599, 12.3064, 9.1980, 9.5153, 4.0130, -2.8516, -13.2724, -57.4485
        };

        assertTrue(comparator0001.equals(fittedActual, fittedExpected));

        // The pseudo-inverse is actually not used to determine the beta coefficients
        // and dual values, but it is necessary to extract the "hat" matrix or compute
        // the leverage of an observation...
        D3xMatrix pseudoInverse = solver.computePseudoInverse();
        D3xVector actualPseudoSolution = pseudoInverse.times(D3xVector.concat(system.getRegressandVector(), model.getConstraintValues()));
        D3xVector expectedPseudoSolution = D3xVector.concat(D3xVector.wrap(betaExpected), D3xVector.wrap(dualExpected));

        assertTrue(comparator0001.equals(actualPseudoSolution, expectedPseudoSolution));
    }
}
