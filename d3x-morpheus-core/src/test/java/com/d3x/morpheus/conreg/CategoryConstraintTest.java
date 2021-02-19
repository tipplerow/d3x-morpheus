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
import java.util.Set;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.series.DoubleSeries;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class CategoryConstraintTest {
    @Test
    public void testBuild() {
        String constraintName = "TestConstraint";
        List<String> regressorKeys = List.of("Cat1", "Cat2", "Cat3");
        List<String> observationRows = List.of("Row1", "Row2", "Row3", "Row4", "Row5", "Row6");

        DoubleSeries<String> observationWeights =
                DoubleSeries.build(String.class, observationRows, List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));

        D3xMatrix observationMatrix =
                D3xMatrix.byrow(6, 3,
                        1.0, 0.0, 0.0,
                        0.8, 0.2, 0.0,
                        0.0, 1.0, 0.0,
                        0.0, 0.5, 0.5,
                        0.0, 0.1, 0.9,
                        0.0, 0.0, 1.0);

        DataFrame<String, String> observationFrame = DataFrame.ofDoubles(observationRows, regressorKeys, observationMatrix);
        RegressionConstraint<String> constraint =
                CategoryConstraint.build(constraintName, Set.copyOf(regressorKeys), observationFrame, observationWeights);

        assertEquals(constraint.getName(), constraintName);
        assertEquals(constraint.getValue(), 0.0);
        assertEquals(constraint.getTerms().size(), 3);
        assertEquals(constraint.getTerms().getDouble("Cat1"), 0.1238, 0.0001);
        assertEquals(constraint.getTerms().getDouble("Cat2"), 0.2810, 0.0001);
        assertEquals(constraint.getTerms().getDouble("Cat3"), 0.5952, 0.0001);
    }
}
