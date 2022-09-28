/*
 * Copyright 2018-2021, D3X Systems LLC - All Rights Reserved
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
package com.d3x.morpheus.filter;

import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.vector.D3xVector;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public final class EWMAFilterTest {
    private static final double TOLERANCE = 1.0E-06;

    @Test
    public void testWeights() {
        var actual = EWMAFilter.computeWeights(1.0, 4);
        var expected = D3xVector.copyOf(1.0 / 1.875, 0.5 / 1.875, 0.25 / 1.875, 0.125 / 1.875);
        Assert.assertTrue(actual.equalsVector(expected));

        actual = EWMAFilter.computeWeights(4.0, 5);
        expected = D3xVector.copyOf(1.0, 0.8408964, 0.7071068, 0.5946036, 0.5).normalize();
        Assert.assertTrue(actual.equalsVector(expected, DoubleComparator.fixed(TOLERANCE)));
    }
}
