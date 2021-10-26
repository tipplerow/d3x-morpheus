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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class WormTableTest {
    // An empty WORM table that remains empty for all tests...
    private static final WormTable<Integer, String, String> empty = WormTable.hash();

    // A WORM table whose contents are fixed for all tests...
    private static final WormTable<Integer, String, String> fixed = WormTable.hash();

    // Creates string values on demand...
    private String compute(Integer row, String col) {
        return String.format("%d-%s", row, col);
    }

    @BeforeClass
    private void populateFixed() {
        fixed.put(1, "two", "1-two");
        fixed.put(3, "four", "3-four");
        fixed.put(5, "six", "5-six");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testCellSet() {
        var iter = fixed.cellSet().iterator();
        iter.next();
        iter.remove();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testClear() {
        fixed.clear();
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testColumn() {
        fixed.column("two").put(1, "foo");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testColumnKeySet() {
        fixed.columnKeySet().remove("two");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testColumnMap() {
        fixed.columnMap().remove("two");
    }

    @Test
    public void testConstructor() {
        Table<Integer, String, String> table1 = HashBasedTable.create();
        table1.put(1, "two", "1-two");

        Table<Integer, String, String> table2 = new WormTable<>(table1);
        Assert.assertTrue(table2.contains(1, "two"));
        Assert.assertEquals(table2.get(1, "two"), "1-two");
        Assert.assertEquals(table1, table2);
    }

    @Test
    public void testGet() {
        Assert.assertNull(fixed.get(1, "foo"));
        Assert.assertNull(fixed.get(2, "two"));

        Assert.assertEquals(fixed.get(1, "two"), "1-two");
        Assert.assertEquals(fixed.get(3, "four"), "3-four");
        Assert.assertEquals(fixed.get(5, "six"), "5-six");
    }

    @Test
    public void testGetOrAssign() {
        WormTable<Integer, String, String> table = WormTable.hash();

        Assert.assertFalse(table.contains(1, "three"));
        Assert.assertEquals(table.getOrAssign(1, "three", "foo"), "foo");

        // The default value is assigned to the key pair...
        Assert.assertTrue(table.contains(1, "three"));
        Assert.assertEquals(table.getOrAssign(1, "three", "bar"), "foo");
    }

    @Test
    public void testGetOrCompute() {
        WormTable<Integer, String, String> table = WormTable.hash();

        Assert.assertFalse(table.contains(1, "three"));
        Assert.assertEquals(table.getOrCompute(1, "three", this::compute), "1-three");

        // The computed value is assigned to the key pair...
        Assert.assertTrue(table.contains(1, "three"));
        Assert.assertEquals(table.get(1, "three"), "1-three");
    }

    @Test
    public void testGetOrThrowPresent() {
        Assert.assertEquals(fixed.getOrThrow(1, "two"), "1-two");
        Assert.assertEquals(fixed.getOrThrow(3, "four"), "3-four");
        Assert.assertEquals(fixed.getOrThrow(5, "six"), "5-six");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetOrThrowAbsent1() {
        fixed.getOrThrow(1, "foo");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testGetOrThrowAbsent2() {
        fixed.getOrThrow(2, "four");
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(fixed.isEmpty());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutExisting1() {
        fixed.put(1, "two", "foo");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testPutExisting2() {
        var table = WormTable.hash();
        table.put(1, "two", "foo");
        table.put(1, "two", "foo");
    }

    @Test
    public void testPutNew() {
        var table = WormTable.hash();
        Assert.assertFalse(table.contains(1, "two"));
        table.put(1, "two", "foo");
        Assert.assertTrue(table.contains(1, "two"));
        Assert.assertEquals(table.get(1, "two"), "foo");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRemoveAbsent() {
        fixed.remove(2, "bar");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRemovePresent() {
        fixed.remove(1, "two");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRow() {
        fixed.row(1).put("abc", "foo");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRowKeySet() {
        fixed.rowKeySet().remove(1);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testRowMap() {
        fixed.rowMap().remove(1);
    }

    @Test
    public void testSize() {
        Assert.assertEquals(empty.size(), 0);
        Assert.assertEquals(fixed.size(), 3);
    }
}
