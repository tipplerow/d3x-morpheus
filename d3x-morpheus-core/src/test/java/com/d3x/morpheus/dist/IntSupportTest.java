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
package com.d3x.morpheus.dist;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Scott Shaffer
 */
public final class IntSupportTest {
    private final IntSupport support = IntSupport.over(-5, 10);

    @Test
    public void testAttributes() {
        Assert.assertEquals(support.getLower(), -5);
        Assert.assertEquals(support.getUpper(), 10);
    }

    @Test
    public void testContains() {
        Assert.assertTrue(support.contains(-5));
        Assert.assertTrue(support.contains(10));
        Assert.assertFalse(support.contains(-6));
        Assert.assertFalse(support.contains(11));
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(support, support);
        Assert.assertEquals(support, IntSupport.over(-5, 10));
        Assert.assertNotEquals(support, IntSupport.over(-6, 10));
        Assert.assertNotEquals(support, IntSupport.over(-5, 9));
    }

    @Test
    public void testRange() {
        var range = IntSupport.over(1, 4).toRange();
        var values = new HashSet<Integer>();

        for (int j : range) {
            values.add(j);
        }

        Assert.assertEquals(values, Set.of(1, 2, 3, 4));

        var iter = range.iterator();
        Assert.assertEquals(iter.next(), 1);
        Assert.assertEquals(iter.next(), 2);
        Assert.assertEquals(iter.next(), 3);
        Assert.assertEquals(iter.next(), 4);
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void testSize() {
        Assert.assertEquals(support.size(), 16);
    }
}
