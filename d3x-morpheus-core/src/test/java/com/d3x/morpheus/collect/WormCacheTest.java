/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.collect;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * @author Scott Shaffer
 */
public class WormCacheTest {
    // A WORM cache whose contents are fixed for all tests...
    private static final WormCache<Integer, String> fixed = WormCache.create();

    // Creates string values on demand...
    private String compute(Integer key) {
        return String.format("V%d", key);
    }

    @BeforeClass
    private void populateFixed() {
        fixed.put(1, "V1");
        fixed.put(3, "V3");
        fixed.put(5, "V5");
    }

    private static Stream<Integer> parallelStream(int value, int count) {
        var list = new ArrayList<Integer>();

        while (list.size() < count)
            list.add(value);

        return list.parallelStream();
    }

    @Test
    public void testGet() {
        Assert.assertNull(fixed.get(2));
        Assert.assertNull(fixed.get(4));

        Assert.assertEquals(fixed.get(1), "V1");
        Assert.assertEquals(fixed.get(3), "V3");
        Assert.assertEquals(fixed.get(5), "V5");
    }

    @Test
    public void testGetConcurrent() {
        parallelStream(1, 100).forEach(x -> Assert.assertEquals(fixed.get(x), "V1"));
    }

    @Test
    public void testGetOrAssign() {
        WormCache<Integer, String> cache = WormCache.create();

        Assert.assertFalse(cache.containsKey(1));
        Assert.assertEquals(cache.getOrAssign(1, "foo"), "foo");

        // The default value is assigned to the key pair...
        Assert.assertTrue(cache.containsKey(1));
        Assert.assertEquals(cache.getOrAssign(1, "bar"), "foo");
    }

    @Test
    public void testGetOrAssignConcurrent() {
        WormCache<Integer, String> cache = WormCache.create();
        parallelStream(1, 100).forEach(x -> Assert.assertEquals(cache.getOrAssign(x, "foo"), "foo"));
    }

    @Test
    public void testGetOrCompute() {
        WormCache<Integer, String> cache = WormCache.create();

        Assert.assertFalse(cache.containsKey(1));
        Assert.assertEquals(cache.getOrCompute(1, this::compute), "V1");

        // The computed value is assigned to the key pair...
        Assert.assertTrue(cache.containsKey(1));
        Assert.assertEquals(cache.get(1), "V1");
    }

    @Test
    public void testGetOrComputeConcurrent() {
        WormCache<Integer, String> cache = WormCache.create();
        parallelStream(1, 100).forEach(x -> Assert.assertEquals(cache.getOrCompute(x, this::compute), "V1"));
    }

    @Test
    public void testGetOrThrowPresent() {
        Assert.assertEquals(fixed.getOrThrow(1), "V1");
        Assert.assertEquals(fixed.getOrThrow(3), "V3");
        Assert.assertEquals(fixed.getOrThrow(5), "V5");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetOrThrowAbsent() {
        fixed.getOrThrow(2);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutExisting1() {
        fixed.put(1, "foo");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutExisting2() {
        var cache = WormCache.create();
        cache.put(1, "foo");
        cache.put(1, "foo");
    }

    @Test
    public void testPutNew() {
        var cache = WormCache.create();
        Assert.assertFalse(cache.containsKey(1));
        cache.put(1, "foo");
        Assert.assertTrue(cache.containsKey(1));
        Assert.assertEquals(cache.get(1), "foo");
    }
}
