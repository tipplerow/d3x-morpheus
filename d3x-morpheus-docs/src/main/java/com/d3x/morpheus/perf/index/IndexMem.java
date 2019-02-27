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

package com.d3x.morpheus.perf.index;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import com.d3x.morpheus.util.MemoryEstimator;

public class IndexMem {

    public static void main(String[] args) {
        final int size = 5000000;
        final TIntIntMap indexMap = new TIntIntHashMap(size, 0.75f, -1, -1);
        final MemoryEstimator estimator = new MemoryEstimator.DefaultMemoryEstimator();
        final long memory = estimator.getObjectSize(indexMap);
        System.out.println("Index Size = " + (memory / (1024 * 1024)) + "MB");
    }
}
