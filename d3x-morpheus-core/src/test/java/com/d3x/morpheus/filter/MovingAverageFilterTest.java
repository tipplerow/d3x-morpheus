/*
 * Copyright 2014-2021, D3X Systems LLC - All Rights Reserved
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

import com.d3x.morpheus.vector.D3xVectorView;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class MovingAverageFilterTest {
    private static final double NA = Double.NaN;
    private static final TimeSeriesFilter filter = new MovingAverageFilter(4);

    @Test
    public void testApply() {
        var original = D3xVectorView.of(1.0, 8.0, -3.0, 12.0, 3.0, -6.0);
        var expected = D3xVectorView.of( NA,  NA,   NA,  4.5, 5.0,  1.5);

        var filtered = filter.apply(original, false);
        var truncated = filter.apply(original, true);

        Assert.assertTrue(filtered.equalsView(expected));
        Assert.assertTrue(truncated.equalsView(expected.subVectorView(3, 3)));
    }

    @Test
    public void testRenormalization() {
        var original = D3xVectorView.of(6.0, NA, -3.0, 12.0,  NA, -6.0);
        var expected = D3xVectorView.of( NA, NA,   NA,  5.0, 4.5,  1.0);
        var filtered = filter.apply(original, false);
        Assert.assertTrue(filtered.equalsView(expected));
    }
}
