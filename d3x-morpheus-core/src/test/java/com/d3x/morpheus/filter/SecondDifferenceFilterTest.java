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
public class SecondDifferenceFilterTest {
    private static final double NA = Double.NaN;
    private static final TimeSeriesFilter filter = DifferenceFilter.SECOND;
    private static final D3xVectorView original = D3xVectorView.of(1.0, 8.0, 27.0, 64.0, 125.0, 216.0);
    private static final D3xVectorView expected = D3xVectorView.of( NA,  NA, 12.0, 18.0,  24.0,  30.0);

    @Test
    public void testApply1() {
        var filtered = filter.apply(original, false);
        var truncated = filter.apply(original, true);

        Assert.assertTrue(filtered.equalsView(expected));
        Assert.assertTrue(truncated.equalsView(expected.subVectorView(2, 4)));
    }

    @Test
    public void testApply2() {
        var series1 = D3xVectorView.of(1.0, 2.0, 3.0,  4.0,  5.0,  6.0);
        var series2 = D3xVectorView.of(1.0, 4.0, 9.0, 16.0, 25.0, 36.0);

        var filtered = filter.apply(series1, series2, false);
        var truncated = filter.apply(series1, series2, true);

        Assert.assertTrue(filtered.equalsView(expected));
        Assert.assertTrue(truncated.equalsView(expected.subVectorView(2, 4)));
    }
}
