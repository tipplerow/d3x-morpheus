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
package com.d3x.morpheus.agg;

import com.d3x.morpheus.json.JsonIO;
import com.d3x.morpheus.json.JsonTester;
import com.d3x.morpheus.vector.D3xVectorView;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class TsStatisticAggregatorTest {
    private static final double TOL = 1.0E-12;
    private static final String JSON1 = "/json/agg/ts-stat-agg-1.json";
    private static final String JSON2 = "/json/agg/ts-stat-agg-2.json";
    private static final String JSON3 = "/json/agg/ts-stat-agg-3.json";

    @Test
    public void testApply() {
        var io = JsonIO.forClass(TimeSeriesAggregator.class);
        var agg1 = io.readResource(JSON1); // NAN
        var agg2 = io.readResource(JSON2); // OMIT
        var agg3 = io.readResource(JSON3); // REPLACE
        var vec1 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0);
        var vec2 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0);
        var vec3 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0, 5.0, Double.NaN);
        var vec4 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);

        // Vector shorter than the window length...
        Assert.assertThrows(RuntimeException.class, () -> agg1.apply(vec1));
        Assert.assertThrows(RuntimeException.class, () -> agg2.apply(vec1));
        Assert.assertThrows(RuntimeException.class, () -> agg3.apply(vec1));

        // Vector longer than the window length...
        Assert.assertThrows(RuntimeException.class, () -> agg1.apply(vec2));
        Assert.assertThrows(RuntimeException.class, () -> agg2.apply(vec2));
        Assert.assertThrows(RuntimeException.class, () -> agg3.apply(vec2));

        // Vector has a missing value...
        Assert.assertTrue(Double.isNaN(agg1.apply(vec3)));
        Assert.assertEquals(agg2.apply(vec3), 15.0 / 5, TOL);
        Assert.assertEquals(agg3.apply(vec3), 15.0 / 6, TOL);

        // No missing values...
        Assert.assertEquals(agg1.apply(vec4), 3.5, TOL);
        Assert.assertEquals(agg2.apply(vec4), 3.5, TOL);
        Assert.assertEquals(agg3.apply(vec4), 3.5, TOL);
    }

    @Test
    public void testJSON() {
        var tester = JsonTester.forClass(TimeSeriesAggregator.class);
        tester.testIO(JSON1);
        tester.testIO(JSON2);
        tester.testIO(JSON3);

        var io = JsonIO.forClass(TimeSeriesAggregator.class);
        var agg1A = io.readResource(JSON1);
        var agg2A = io.readResource(JSON2);
        var agg3A = io.readResource(JSON3);
        var nan1E = NanPolicy.builder().type(NanPolicy.Type.NAN).minObs(4).build();
        var nan2E = NanPolicy.builder().type(NanPolicy.Type.OMIT).minObs(4).build();
        var nan3E = NanPolicy.builder().type(NanPolicy.Type.REPLACE).minObs(4).replace(0.0).build();
        var agg1E = new TsStatisticAggregator(AggregatorType.MEAN, nan1E, 6);
        var agg2E = new TsStatisticAggregator(AggregatorType.MEAN, nan2E, 6);
        var agg3E = new TsStatisticAggregator(AggregatorType.MEAN, nan3E, 6);
        Assert.assertEquals(agg1A, agg1E);
        Assert.assertEquals(agg2A, agg2E);
        Assert.assertEquals(agg3A, agg3E);
    }
}
