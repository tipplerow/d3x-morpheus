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
import com.d3x.morpheus.util.DoubleComparator;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ConstrainedRegressionModelTest {
    private static final String weight = "w";
    private static final String regressand = "y";
    private static final List<String> descriptors = List.of("x0", "x1", "x2", "x3");
    private static final List<String> categories = List.of("Ford", "GM", "Chrysler");

    private static final DoubleComparator comparator = DoubleComparator.DEFAULT;

    private static ConstrainedRegressionModel<String> buildBasicModel() {
        return ConstrainedRegressionModel.build(regressand, descriptors);
    }

    @Test
    public void testBasicModel() {
        ConstrainedRegressionModel<String> model = buildBasicModel();

        assertFalse(model.containsConstraint("con1"));
        assertFalse(model.containsConstraint("make"));

        assertTrue(model.containsRegressor("x0"));
        assertTrue(model.containsRegressor("x1"));
        assertTrue(model.containsRegressor("x2"));
        assertTrue(model.containsRegressor("x3"));
        assertFalse(model.containsRegressor("y"));
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
        regressors.addAll(categories);

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

        model = model.addCategory("make", Set.copyOf(categories));

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
        ConstrainedRegressionModel<String> model = buildBasicModel();

        assertNull(model.getWeight());
        assertFalse(model.hasWeights());

        model = model.withWeight(weight);

        assertEquals(model.getWeight(), weight);
        assertTrue(model.hasWeights());
    }
}
