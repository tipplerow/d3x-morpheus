/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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

package com.d3x.morpheus.perf.util;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.range.Range;

public class TrovePerf {
    public static void main(String[] args) {
        final Array<Integer> keys = Range.of(0, 10000000).toArray().shuffle(4);
        final TLongIntMap indexMap = new TLongIntHashMap(10000000);
        for (int i=0; i<keys.length(); ++i) {
            indexMap.put((long)keys.getInt(i), i);
        }
        final int[] indexes = new int[indexMap.size()];
        for (int i=0; i<5; ++i) {
            final long t1 = System.nanoTime();
            keys.forEachInt(x -> {
                final int value = indexMap.get(x);
                indexes[x] = value;
            });
            /*
            indexMap.forEachEntry((key, value) -> {
                indexes[value] = value;
                return true;
            });
            */
            final long t2 = System.nanoTime();
            System.out.println("Map traversal in " + ((t2-t1)/1000000) + " millis");
        }
    }
}
