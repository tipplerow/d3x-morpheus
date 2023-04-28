/*
 * Copyright 2018-2022, D3X Systems LLC - All Rights Reserved
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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class FilterParserTest {
    private void runParserTest(TimeSeriesFilter filter, String encoding) {
        Assert.assertEquals(encoding, filter.encode());
        Assert.assertEquals(TimeSeriesFilter.parse(encoding), filter);
    }

    @Test
    public void testParser() {
        runParserTest(TimeSeriesFilter.of(1.0, 2.0, 3.0), "filter(1.0, 2.0, 3.0)");
        runParserTest(TimeSeriesFilter.difference(1), "diff(1)");
        runParserTest(TimeSeriesFilter.difference(2), "diff(2)");
        runParserTest(TimeSeriesFilter.EWMA(2.3, 4), "ewma(2.3, 4)");
        runParserTest(TimeSeriesFilter.LWMA(4), "lwma(4)");
        runParserTest(TimeSeriesFilter.movingAverage(5), "ma(5)");
    }
}
