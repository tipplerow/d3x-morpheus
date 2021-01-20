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
import java.util.Set;

import com.d3x.morpheus.frame.DataFrame;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ConstrainedRegressionModelTest extends ConstrainedRegressionTestBase {
    @Test
    public void testUnconstrainedModel() {
        ConstrainedRegressionModel<String> model = buildUnconstrainedModel();

        assertFalse(model.containsConstraint(descriptorConstraintName));
        assertFalse(model.containsConstraint(categoryName));

        assertTrue(model.containsRegressor(descriptors.get(0)));
        assertTrue(model.containsRegressor(descriptors.get(1)));
        assertTrue(model.containsRegressor(descriptors.get(2)));
        assertTrue(model.containsRegressor(descriptors.get(3)));
        assertFalse(model.containsRegressor(weight));
        assertFalse(model.containsRegressor("x4"));

        assertEquals(model.countConstraints(), 0);
        assertEquals(model.countRegressors(), 4);

        assertEquals(model.getConstraintKeys(), List.of());
        assertEquals(model.getConstraintValues().getDimension(), 0);
        assertEquals(model.getConstraintMatrix().getRowDimension(), 0);

        assertEquals(model.getRegressand(), regressand);
        assertEquals(model.getRegressors(), descriptors);
        assertNull(model.getWeight());

        assertFalse(model.hasWeights());
    }

    @Test
    public void testConstraints() {
        List<String> regressors = new ArrayList<>();
        regressors.addAll(descriptors);
        regressors.addAll(categoryColumns);

        ConstrainedRegressionModel<String> model =
                ConstrainedRegressionModel.build(regressand, regressors);

        assertFalse(model.containsConstraint("con1"));
        assertFalse(model.containsConstraint("make"));
        assertEquals(model.countConstraints(), 0);
        assertEquals(model.getConstraintKeys(), List.of());
        assertEquals(model.getConstraintValues().getDimension(), 0);
        assertEquals(model.getConstraintMatrix().getRowDimension(), 0);

        DataFrame<String, String> con1 = DataFrame.ofDoubles("con1", List.of("x1", "x2", "x3"));
        con1.setDouble("con1", "x1", 1.0);
        con1.setDouble("con1", "x2", 2.0);
        con1.setDouble("con1", "x3", 3.0);

        model = model.addConstraint(con1, 4.0);

        assertTrue(model.containsConstraint("con1"));
        assertFalse(model.containsConstraint("make"));
        assertEquals(model.countConstraints(), 1);
        assertEquals(model.getConstraintKeys(), List.of("con1"));

        assertTrue(comparator.equals(model.getConstraintValues().toArray(), new double[] { 4.0 }));
        assertTrue(comparator.equals(model.getConstraintMatrix().getData(), new double[][] {{ 0.0, 1.0, 2.0, 3.0, 0.0, 0.0, 0.0 }} ));

        model = model.addCategory("make", Set.copyOf(categoryColumns));

        assertTrue(model.containsConstraint("con1"));
        assertTrue(model.containsConstraint("make"));
        assertEquals(model.countConstraints(), 2);
        assertEquals(model.getConstraintKeys(), List.of("con1", "make"));

        assertTrue(comparator.equals(model.getConstraintValues().toArray(), new double[] { 4.0, 0.0 }));
        assertTrue(comparator.equals(model.getConstraintMatrix().getData(), new double[][] {
                { 0.0, 1.0, 2.0, 3.0, 0.0, 0.0, 0.0 },
                { 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0 }
        }));
    }

    @Test
    public void testWithWeight() {
        ConstrainedRegressionModel<String> model = buildUnconstrainedModel();

        assertNull(model.getWeight());
        assertFalse(model.hasWeights());

        model.withWeight(weight);

        assertEquals(model.getWeight(), weight);
        assertTrue(model.hasWeights());
    }
}
