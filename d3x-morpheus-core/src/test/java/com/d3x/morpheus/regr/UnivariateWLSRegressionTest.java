/*
 * Copyright (C) 2014-2022 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.regr;

import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class UnivariateWLSRegressionTest {
    Random random = new Random(20220913);

    private void runTest(double a, double b, int ndata, double noise, double tolerance) {
        var model = new UnivariateWLSRegression();

        for (int index = 0; index < ndata; ++index) {
            var x = random.nextDouble();
            var y = a + b * x + noise * random.nextGaussian();
            model.add(x, y);
        }

        Assert.assertTrue(Double.isNaN(model.getSlope()));
        Assert.assertTrue(Double.isNaN(model.getIntercept()));

        model.fit();

        Assert.assertEquals(model.getSlope(), b, tolerance);
        Assert.assertEquals(model.getIntercept(), a, tolerance);
    }

    @Test
    public void testFit() {
        runTest(5.0, 3.0, 10, 0.01, 0.02);
        runTest(5.0, 3.0, 100, 0.1, 0.03);
        runTest(5.0, 3.0, 1000, 1.0, 0.20);
    }
}
