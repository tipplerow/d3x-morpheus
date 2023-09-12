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
package com.d3x.morpheus.root;

import com.d3x.morpheus.util.DoubleInterval;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class UnivariateRootFinderTest {
    @Test
    public void testBracket() {
        var root1 = 10.0;
        var root2 = -100.0;

        var bracket1 = UnivariateRootFinder.bracket(x -> x - root1, DoubleInterval.closed(-1.0, 1.0));
        var bracket2 = UnivariateRootFinder.bracket(x -> x - root2, DoubleInterval.closed(-1.0, 1.0));

        Assert.assertTrue(bracket1.contains(root1));
        Assert.assertTrue(bracket2.contains(root2));

        var values = new double[] { 0.01, 0.1, 1.0, 10.0, 100.0 };

        for (var value : values) {
            var bracket = UnivariateRootFinder.bracket(x -> Math.exp(x) - value, DoubleInterval.closed(-1.0, 1.0));
            Assert.assertTrue(Math.exp(bracket.getLower()) <= value);
            Assert.assertTrue(Math.exp(bracket.getUpper()) >= value);
        }
    }
}
