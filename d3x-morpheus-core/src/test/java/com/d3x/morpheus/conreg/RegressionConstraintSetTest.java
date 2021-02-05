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

import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.vector.D3xVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class RegressionConstraintSetTest {
    @Test
    public void testFullRank() {
        List<String> regressorKeys = List.of("Col1", "Col2", "Col3", "Col4", "Col5");

        RegressionConstraint<String> constraint1 =
                new RegressionConstraint("Con1", 1.0, DoubleSeries.build(String.class, regressorKeys.subList(0, 3), List.of(1.0, 2.0, 3.0)));

        RegressionConstraint<String> constraint2 =
                new RegressionConstraint("Con2", 2.0, DoubleSeries.build(String.class, regressorKeys.subList(2, 5), List.of(3.0, 4.0, 5.0)));

        RegressionConstraintSet<String> constraintSet =
                RegressionConstraintSet.create(List.of(constraint1, constraint2));

        D3xMatrix actualMatrix = constraintSet.getConstraintMatrix(regressorKeys);
        D3xMatrix expectedMatrix =
                D3xMatrix.byrow(2, 5,
                        1.0, 2.0, 3.0, 0.0, 0.0,
                        0.0, 0.0, 3.0, 4.0, 5.0);

        D3xVector actualValues = constraintSet.getConstraintValues();
        D3xVector expectedValues = D3xVector.wrap(1.0, 2.0);

        assertTrue(actualMatrix.equalsMatrix(expectedMatrix));
        assertTrue(actualValues.equalsVector(expectedValues));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRankDeficient() {
        List<String> regressorKeys = List.of("Col1", "Col2", "Col3");

        RegressionConstraint<String> constraint1 =
                new RegressionConstraint("Con1", 1.0, DoubleSeries.build(String.class, regressorKeys, List.of(1.0, 2.0, 3.0)));

        RegressionConstraint<String> constraint2 =
                new RegressionConstraint("Con2", 2.0, DoubleSeries.build(String.class, regressorKeys, List.of(2.0, 4.0, 6.0)));

        RegressionConstraintSet<String> constraintSet =
                RegressionConstraintSet.create(List.of(constraint1, constraint2));
    }
}
