/*
 * Copyright (C) 2014-2022 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
public final class DoubleUtilTest {
    @Test
    public void testBound() {
        var lower = 0.0;
        var upper = 1.0;
        Assert.assertEquals(DoubleUtil.bound(-1.0, lower, upper), lower, 1.0E-12);
        Assert.assertEquals(DoubleUtil.bound(0.0, lower, upper), 0.0, 1.0E-12);
        Assert.assertEquals(DoubleUtil.bound(0.2, lower, upper), 0.2, 1.0E-12);
        Assert.assertEquals(DoubleUtil.bound(0.8, lower, upper), 0.8, 1.0E-12);
        Assert.assertEquals(DoubleUtil.bound(1.0, lower, upper), 1.0, 1.0E-12);
        Assert.assertEquals(DoubleUtil.bound(2.0, lower, upper), upper, 1.0E-12);
    }

    @Test
    public void testRatio() {
        Assert.assertEquals(DoubleUtil.ratio(1, 2), 0.5, 1.0E-15);
        Assert.assertEquals(DoubleUtil.ratio(1, 3), 0.33333333, 1.0E-08);
        Assert.assertEquals(DoubleUtil.ratio(5, 3), 1.66666667, 1.0E-08);
    }

    @Test
    public void testRound() {
        Assert.assertEquals(DoubleUtil.round(0.234567, 0.01), 0.23, 1.0E-15);
        Assert.assertEquals(DoubleUtil.round(0.234567, 0.0001), 0.2346, 1.0E-15);
        Assert.assertEquals(DoubleUtil.round(1.234567, 0.25), 1.25, 1.0E-15);
    }
}
