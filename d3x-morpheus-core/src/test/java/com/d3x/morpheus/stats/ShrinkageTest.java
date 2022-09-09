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
package com.d3x.morpheus.stats;

import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class ShrinkageTest {
    @Test
    public void testShrink() {
        var tolerance = 1.0E-06;
        var shrinkage = Shrinkage.builder().prior(1.0).shrinkage(0.25).build().validate();

        Assert.assertEquals(shrinkage.shrink(0.0), 0.25, tolerance);
        Assert.assertEquals(shrinkage.shrink(1.0), 1.0,  tolerance);
        Assert.assertEquals(shrinkage.shrink(2.0), 1.75, tolerance);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testInvalid1() {
        Shrinkage.builder().prior(0.0).shrinkage(-1.0).build().validate();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testInvalid2() {
        Shrinkage.builder().prior(0.0).shrinkage(2.0).build().validate();
    }
}
