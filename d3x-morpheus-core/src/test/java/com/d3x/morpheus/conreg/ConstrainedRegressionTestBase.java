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
import java.util.Random;
import java.util.Set;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.util.DoubleComparator;

public abstract class ConstrainedRegressionTestBase {
    public static final String weight = "Weight";
    public static final String regressand = "Regressand";
    public static final List<String> descriptors = List.of("x0", "x1", "x2", "x3");

    public static final String descriptorConstraintName = "DescriptorConstraint";
    public static final double descriptorConstraintValue = 3.0;
    public static final double[] descriptorConstraintCoeff = new double[] { 0.0, 1.0, 2.0, 0.0 };
    public static final DataFrame<String, String> descriptorConstraintFrame =
            DataFrame.ofDoubles(descriptorConstraintName, descriptors, descriptorConstraintCoeff);

    public static final String categoryName = "AutoMaker";
    public static final List<String> categoryColumns = List.of("Ford", "GM", "BMW");

    public static final List<String> observationRows =
            List.of("row1", "row2", "row3", "row4", "row5", "row6", "row7", "row8", "row9", "row10", "row11");

    public static final DoubleComparator comparator = DoubleComparator.DEFAULT;

    public static ConstrainedRegressionModel<String> buildUnconstrainedModel() {
        return ConstrainedRegressionModel.build(regressand, descriptors);
    }

    public static final D3xVector betaExact =
            D3xVector.wrap(10.0, 1.0, 2.0, -1.0, 3.0, -2.0, 4.0);

    public static final double regressandExact(double[] observation) {
        double result = 0.0;

        for (int col = 0; col < betaExact.length(); col++)
            result += betaExact.get(col) * observation[col];

        return result;
    }

    public static List<String> getRegressors() {
        List<String> regressors = new ArrayList<>();
        regressors.addAll(descriptors);
        regressors.addAll(categoryColumns);
        return regressors;
    }

    public static ConstrainedRegressionModel<String> buildConstrainedModel() {
        return ConstrainedRegressionModel.build(regressand, getRegressors())
                .withWeight(weight)
                .addConstraint(descriptorConstraintFrame, descriptorConstraintValue)
                .addCategory(categoryName, Set.copyOf(categoryColumns));
    }

    public static DataFrame<String, String> buildObservationFrame(Random random) {
        List<String> columnNames = new ArrayList<>();
        columnNames.addAll(descriptors);
        columnNames.addAll(categoryColumns);
        columnNames.add(regressand);
        columnNames.add(weight);

        double[][] frameValues = new double[][] {
                //  x0,    x1,    x2,     x3,  Ford,   GM,  BMW, Regressand, Weight
                {  1.0,  -5.0,  25.0, -125.0,   1.0,  0.0,  0.0, Double.NaN,  1.0 },
                {  1.0,  -4.0,  16.0,  -64.0,   1.0,  0.0,  0.0, Double.NaN,  2.0 },
                {  1.0,  -3.0,   9.0,  -27.0,   1.0,  0.0,  0.0, Double.NaN,  3.0 },
                {  1.0,  -2.0,   4.0,   -8.0,   0.0,  1.0,  0.0, Double.NaN,  4.0 },
                {  1.0,  -1.0,   1.0,   -1.0,   0.0,  1.0,  0.0, Double.NaN,  0.0 },
                {  1.0,   0.0,   0.0,    0.0,   0.0,  1.0,  0.0, Double.NaN,  1.0 },
                {  1.0,   1.0,   1.0,    1.0,   0.0,  1.0,  0.0, Double.NaN,  2.0 },
                {  1.0,   2.0,   2.0,    8.0,   0.0,  1.0,  0.0, Double.NaN,  3.0 },
                {  1.0,   3.0,   9.0,   27.0,   0.0,  1.0,  0.0, Double.NaN,  1.0 },
                {  1.0,   4.0,  16.0,   64.0,   0.0,  0.0,  1.0, Double.NaN,  2.0 },
                {  1.0,   5.0,  25.0,  125.0,   0.0,  0.0,  1.0, Double.NaN,  1.0 }
        };

        // Assign the regressand using the exact model function and some noise...
        for (int row = 0; row < frameValues.length; row++)
            frameValues[row][7] = regressandExact(frameValues[row]) + 0.01 * random.nextDouble();

        return DataFrame.ofDoubles(observationRows, columnNames, D3xMatrix.wrap(frameValues));
    }
}
