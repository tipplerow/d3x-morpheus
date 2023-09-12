/*
 * Copyright 2018-2023, Talos Trading - All Rights Reserved
 *
 * Licensed under a proprietary end-user agreement issued by D3X Systems.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.d3xsystems.com/static/eula/quanthub-eula.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.root;

import com.d3x.morpheus.numerictests.NumericTestBase;

import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class BrentRootFinderTest extends NumericTestBase {
    private final BrentRootFinder finder = new BrentRootFinder(0.1 * TOLERANCE);
    private static final double TOLERANCE = 1.0E-06;

    public BrentRootFinderTest() {
        super(TOLERANCE);
    }

    @Test
    public void testExponential() {
        var values = new double[] { 0.01, 0.1, 1.0, 10.0, 100.0 };

        for (var value : values) {
            var root = finder.solve(x -> Math.exp(x) - value, 0.0);
            assertDouble(root, Math.log(value));
        }
    }

    @Test
    public void testLog() {
        var values = new double[] { -2.0, -1.0, 0.0, 1.0, 2.0 };

        for (var value : values) {
            var root = finder.solve(x -> Math.log(x) - value, 1.0);
            assertDouble(root, Math.exp(value));
        }
    }
}
