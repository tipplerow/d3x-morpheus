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

import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Scott Shaffer
 */
public class LRUCacheTest {
    private <K, V> void assertCache(LRUCache<K, V> cache, List<K> keys, List<V> values) {
        Assert.assertEquals(keys.size(), values.size());
        Assert.assertEquals(cache.size(), keys.size());
        Assert.assertEquals(cache.keys(), Set.copyOf(keys));
        Assert.assertEquals(List.copyOf(cache.values()), values);

        for (int index = 0; index < keys.size(); ++index) {
            Assert.assertTrue(cache.containsKey(keys.get(index)));
            Assert.assertEquals(cache.get(keys.get(index)), values.get(index));
        }
    }

    @Test
    public void testCapacity() {
        var cache = new LRUCache<String, Integer>(3);
        assertCache(cache, List.of(), List.of());

        cache.put("A", 1);
        assertCache(cache, List.of("A"), List.of(1));

        cache.put("B", 2);
        assertCache(cache, List.of("A", "B"), List.of(1, 2));

        cache.put("C", 3);
        assertCache(cache, List.of("A", "B", "C"), List.of(1, 2, 3));

        cache.put("D", 4);
        assertCache(cache, List.of("B", "C", "D"), List.of(2, 3, 4));

        cache.put("E", 5);
        assertCache(cache, List.of("C", "D", "E"), List.of(3, 4, 5));

        Assert.assertEquals(cache.get("C"), Integer.valueOf(3));

        cache.put("F", 6);
        assertCache(cache, List.of("E", "C", "F"), List.of(5, 3, 6));
    }
}
