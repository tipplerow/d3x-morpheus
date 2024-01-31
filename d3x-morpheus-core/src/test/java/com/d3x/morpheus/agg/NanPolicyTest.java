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

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class NanPolicyTest {
    private static final String JSON1 = "/json/agg/nan-policy-1.json";
    private static final String JSON2 = "/json/agg/nan-policy-2.json";
    private static final String JSON3 = "/json/agg/nan-policy-3.json";

    @Test
    public void testJSON() {
        var tester = JsonTester.forClass(NanPolicy.class);
        tester.testIO(JSON1);
        tester.testIO(JSON2);
        tester.testIO(JSON3);

        var io = JsonIO.forClass(NanPolicy.class);
        var nan1A = io.readResource(JSON1);
        var nan2A = io.readResource(JSON2);
        var nan3A = io.readResource(JSON3);
        var nan1E = NanPolicy.builder().type(NanPolicy.Type.NAN).minObs(10).build();
        var nan2E = NanPolicy.builder().type(NanPolicy.Type.OMIT).minObs(20).build();
        var nan3E = NanPolicy.builder().type(NanPolicy.Type.REPLACE).minObs(30).replace(0.0).build();
        Assert.assertEquals(nan1A, nan1E);
        Assert.assertEquals(nan2A, nan2E);
        Assert.assertEquals(nan3A, nan3E);
    }
}
