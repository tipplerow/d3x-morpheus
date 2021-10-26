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

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class WormSetTest {
    // An empty WORM set that remains empty for all tests...
    private static final WormSet<String> empty = WormSet.hash();

    // A WORM set whose contents are fixed for all tests...
    private static final WormSet<String> fixed = WormSet.hash();

    @BeforeClass
    private void populateFixed() {
        fixed.add("E1");
        fixed.add("E2");
        fixed.add("E3");
    }

    @Test
    public void testAddUnique() {
        var set = new WormSet<String>();
        Assert.assertFalse(set.contains("E1"));
        Assert.assertFalse(set.contains("E2"));

        Assert.assertTrue(set.add("E1"));
        Assert.assertTrue(set.contains("E1"));
        Assert.assertFalse(set.contains("E2"));

        Assert.assertTrue(set.add("E2"));
        Assert.assertTrue(set.contains("E1"));
        Assert.assertTrue(set.contains("E2"));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testAddDuplicate() {
        var set = new WormSet<String>();
        set.add("E1");
        set.add("E1");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testClear() {
        fixed.clear();
    }

    @Test
    public void testConstructor() {
        Set<String> set1 = new HashSet<>();
        set1.add("E1");

        Set<String> set2 = new WormSet<>(set1);
        Assert.assertTrue(set2.contains("E1"));
        Assert.assertEquals(set1, set2);
    }

    @Test
    public void testContains() {
        Assert.assertTrue(fixed.contains("E1"));
        Assert.assertTrue(fixed.contains("E2"));
        Assert.assertTrue(fixed.contains("E3"));
        Assert.assertFalse(fixed.contains("E4"));
        Assert.assertFalse(fixed.contains("E5"));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(fixed.isEmpty());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testIterator() {
        var iter = fixed.iterator();
        iter.next();
        iter.remove();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRemoveAbsent() {
        fixed.remove("E1");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRemovePresent() {
        fixed.remove("foo");
    }

    @Test
    public void testSize() {
        Assert.assertEquals(empty.size(), 0);
        Assert.assertEquals(fixed.size(), 3);
    }
}

