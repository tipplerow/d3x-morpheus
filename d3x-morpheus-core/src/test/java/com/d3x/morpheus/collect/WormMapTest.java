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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class WormMapTest {
    // An empty WORM map that remains empty for all tests...
    private static final WormMap<Integer, String> empty = WormMap.hash();

    // A WORM map whose contents are fixed for all tests...
    private static final WormMap<Integer, String> fixed = WormMap.hash();

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

    @Test(expectedExceptions = RuntimeException.class)
    public void testClear() {
        fixed.clear();
    }

    @Test
    public void testConstructor() {
        Map<Integer, String> map1 = new HashMap<>();
        map1.put(1, "V1");

        Map<Integer, String> map2 = new WormMap<>(map1);
        Assert.assertTrue(map2.containsKey(1));
        Assert.assertEquals(map2.get(1), "V1");
        Assert.assertEquals(map1, map2);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testEntrySet() {
        var set = fixed.entrySet();
        var iter = set.iterator();
        iter.next();
        iter.remove();
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
    public void testGetOrAssign() {
        WormMap<Integer, String> map = WormMap.hash();

        Assert.assertFalse(map.containsKey(1));
        Assert.assertEquals(map.getOrAssign(1, "foo"), "foo");

        // The default value is assigned to the key pair...
        Assert.assertTrue(map.containsKey(1));
        Assert.assertEquals(map.getOrAssign(1, "bar"), "foo");
    }

    @Test
    public void testGetOrCompute() {
        WormMap<Integer, String> map = WormMap.hash();

        Assert.assertFalse(map.containsKey(1));
        Assert.assertEquals(map.getOrCompute(1, this::compute), "V1");

        // The computed value is assigned to the key pair...
        Assert.assertTrue(map.containsKey(1));
        Assert.assertEquals(map.get(1), "V1");
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(fixed.isEmpty());
    }

    @Test
    public void testKeySet1() {
        Assert.assertEquals(fixed.keySet(), Set.of(1, 3, 5));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testKeySet2() {
        fixed.keySet().remove(1);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutExisting1() {
        fixed.put(1, "foo");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutExisting2() {
        var map = WormMap.hash();
        map.put(1, "foo");
        map.put(1, "foo");
    }

    @Test
    public void testPutNew() {
        var map = WormMap.hash();
        Assert.assertFalse(map.containsKey(1));
        map.put(1, "foo");
        Assert.assertTrue(map.containsKey(1));
        Assert.assertEquals(map.get(1), "foo");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRemoveAbsent() {
        fixed.remove(2);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRemovePresent() {
        fixed.remove(1);
    }

    @Test
    public void testRequirePresent() {
        Assert.assertEquals(fixed.require(1), "V1");
        Assert.assertEquals(fixed.require(3), "V3");
        Assert.assertEquals(fixed.require(5), "V5");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRequireAbsent() {
        fixed.require(2);
    }

    @Test
    public void testSize() {
        Assert.assertEquals(empty.size(), 0);
        Assert.assertEquals(fixed.size(), 3);
    }
}
