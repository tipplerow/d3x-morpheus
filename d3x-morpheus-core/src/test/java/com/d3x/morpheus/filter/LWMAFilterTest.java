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
package com.d3x.morpheus.filter;

import com.d3x.morpheus.vector.D3xVector;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class LWMAFilterTest {
    @Test
    public void testWeights() {
        var actual = LWMAFilter.computeWeights(4);
        var expected = D3xVector.copyOf(0.4, 0.3, 0.2, 0.1);
        Assert.assertTrue(actual.equalsVector(expected));

        actual = LWMAFilter.computeWeights(6);
        expected = D3xVector.copyOf(6.0 / 21.0, 5.0 / 21.0, 4.0 / 21.0, 3.0 / 21.0, 2.0 / 21.0, 1.0 / 21.0);
        Assert.assertTrue(actual.equalsVector(expected));
    }
}
