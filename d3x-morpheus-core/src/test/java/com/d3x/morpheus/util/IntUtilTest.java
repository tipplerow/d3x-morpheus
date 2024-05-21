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
package com.d3x.morpheus.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class IntUtilTest {
    @Test
    public void testBound() {
        var lower = 2;
        var upper = 5;
        Assert.assertEquals(IntUtil.bound(0, lower, upper), 2);
        Assert.assertEquals(IntUtil.bound(1, lower, upper), 2);
        Assert.assertEquals(IntUtil.bound(2, lower, upper), 2);
        Assert.assertEquals(IntUtil.bound(3, lower, upper), 3);
        Assert.assertEquals(IntUtil.bound(4, lower, upper), 4);
        Assert.assertEquals(IntUtil.bound(5, lower, upper), 5);
        Assert.assertEquals(IntUtil.bound(6, lower, upper), 5);
        Assert.assertEquals(IntUtil.bound(7, lower, upper), 5);
    }
}
