/*
 * Copyright 2018-2024, Talos Trading - All Rights Reserved
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

import com.d3x.morpheus.numerictests.NumericTestBase;
import com.d3x.morpheus.stats.Median;
import com.d3x.morpheus.vector.D3xVector;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Random;

/**
 * @author Scott Shaffer
 */
public class TsCompositeAggregatorTest extends NumericTestBase {
    private static final String JSON1 = "/json/agg/ts-comp-agg-1.json";

    @Test
    public void testApply() {
        var io = JsonIO.forClass(TsCompositeAggregator.class);
        var agg = io.readResource(JSON1);
        var series = D3xVector.random(90, new Random(20240209));
        var median = new Median().compute(series);
        var ewmaWt = new double[] { 0.3905243, 0.2761424, 0.1952621, 0.1380712 };
        var ewma = ewmaWt[0] * series.get(89) + ewmaWt[1] * series.get(88) + ewmaWt[2] * series.get(87) + ewmaWt[3] * series.get(86);

        var actual = agg.apply(series);
        var expected = Math.sqrt(median * ewma);
        Assert.assertEquals(actual, expected, 1.0E-06);
    }

    @Test
    public void testJSON() {
        var tester = JsonTester.forClass(TsCompositeAggregator.class);
        tester.testIO(JSON1);

        var io = JsonIO.forClass(TsCompositeAggregator.class);
        var actual = io.readResource(JSON1);
        var expected = createExpected();
        Assert.assertEquals(actual, expected);
        Assert.assertEquals(actual.getWindowLen(), 90);
    }

    private TsCompositeAggregator createExpected() {
        var compositorPolicy = NanPolicy.builder().type(NanPolicy.Type.NAN).minObs(2).build();
        var component1Policy = NanPolicy.builder().type(NanPolicy.Type.OMIT).minObs(45).build();
        var component2Policy = NanPolicy.builder().type(NanPolicy.Type.OMIT).minObs(5).build();

        var compositor = new CsStatisticAggregator(AggregatorType.GEO_MEAN, compositorPolicy);
        var component1 = new TsStatisticAggregator(AggregatorType.MEDIAN, component1Policy, 90);
        var component2 = new EwmaAggregator(4, 2, true, component2Policy);

        return new TsCompositeAggregator(compositor, List.of(component1, component2));
    }
}
