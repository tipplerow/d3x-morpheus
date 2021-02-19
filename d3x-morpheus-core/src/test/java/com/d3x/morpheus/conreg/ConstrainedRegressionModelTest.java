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

import java.util.ArrayList;
import java.util.List;

import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.vector.D3xVector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ConstrainedRegressionModelTest extends ConstrainedRegressionTestBase {
    @Test
    public void testUnconstrained() {
        ConstrainedRegressionModel<String, String> model = buildUnconstrainedModel();

        assertEquals(model.getRegressorKeys(), descriptors);
        assertEquals(model.getObservationKeys(), observationRows);
        assertTrue(model.getConstraintKeys().isEmpty());
        assertTrue(model.getObservationWeights().getRequired(observationRows).equalsVector(D3xVector.ones(11)));

        List<String> myRows = observationRows.subList(0, 8);
        List<String> myCols = descriptors.subList(1, 4);

        model.withRegressors(myCols).withObservations(myRows);

        assertEquals(model.getRegressorKeys(), myCols);
        assertEquals(model.getObservationKeys(), myRows);

        model.withWeights("Weight");
        assertTrue(model.getObservationWeights().getRequired(myRows).equalsArray(1.0, 2.0, 3.0, 4.0, 0.0, 1.0, 2.0, 3.0));
    }

    @Test
    public void testConstrained() {
        List<String> regressors = new ArrayList<>();
        regressors.addAll(descriptors);
        regressors.addAll(categoryColumns);

        ConstrainedRegressionModel<String, String> model =
                ConstrainedRegressionModel.create(regressand, regressors, buildObservationFrame());

        assertEquals(model.countConstraints(), 0);
        assertEquals(model.getConstraintKeys(), List.of());

        DoubleSeries<String> terms1 = DoubleSeries.build(String.class, List.of("x1", "x2", "x3"), List.of(1.0, 2.0, 3.0));
        model.withConstraint("con1", 4.0, terms1);

        assertEquals(model.countConstraints(), 1);
        assertEquals(model.getConstraintKeys(), List.of("con1"));

        model.withConstraint("make", 0.0, DoubleSeries.ones(String.class, categoryColumns));

        assertEquals(model.countConstraints(), 2);
        assertEquals(model.getConstraintKeys(), List.of("con1", "make"));
    }
}
