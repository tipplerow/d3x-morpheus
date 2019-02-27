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

import java.time.Duration;
import java.time.LocalDateTime;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.range.Range;

public class IndexPerf {

    public static void main(String[] args) {
        for (int x=0; x<5; ++x) {
            final LocalDateTime start = LocalDateTime.now();
            final LocalDateTime end = start.plusSeconds(10000000);
            final Array<LocalDateTime> dates = Range.of(start, end, Duration.ofSeconds(1)).toArray().shuffle(3);
            final Index<LocalDateTime> index = Index.of(dates);
            final long t1 = System.nanoTime();
            final Index<LocalDateTime> sorted = index.sort(true, true);
            final long t2 = System.nanoTime();
            System.out.println("Sorted Index in " + ((t2-t1)/1000000 + " millis"));
            for (int j=1; j<index.size(); ++j) {
                final LocalDateTime d1 = sorted.getKey(j-1);
                final LocalDateTime d2 = sorted.getKey(j);
                if (d1.isAfter(d2)) {
                    throw new RuntimeException("Index keys are not sorted");
                } else {
                    final int i1 = index.getIndexForKey(d1);
                    final int i2 = sorted.getIndexForKey(d1);
                    if (i1 != i2) {
                        throw new RuntimeException("The indexes do not match between original and sorted");
                    }
                }
            }
        }
    }
}
