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

package com.d3x.morpheus.docs.array;

import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.stats.StatType;
import com.d3x.morpheus.util.PerfStat;

public class ArrayPerf5 {

    public static void main(String[] args) {

        final int count = 5;
        final int size = 1000000;

        final List<Class<?>> types = Arrays.asList(int.class, long.class, double.class, Date.class, LocalDate.class, String.class, ZonedDateTime.class);
        final Array<String> colKeys = Array.of("Native", "Morpheus (sequential)", "Morpheus (parallel)");
        final List<String> rowKeys = types.stream().map(Class::getSimpleName).collect(Collectors.toList());
        final DataFrame<String,String> memory = DataFrame.ofDoubles(rowKeys, colKeys);
        final DataFrame<String,String> times = DataFrame.ofDoubles(rowKeys, colKeys);
        types.forEach(type -> {
            for (int style : new int[] {0, 1, 2}) {
                System.out.println("Running tests for " + type);
                final String key = type.getSimpleName();
                final Callable<Object> callable = createCallable(style, type, size);
                final PerfStat stats = PerfStat.run(key, count, TimeUnit.MILLISECONDS, callable);
                final double runTime = stats.getCallTime(StatType.MEDIAN);
                final double gcTime = stats.getGcTime(StatType.MEDIAN);
                times.rows().setDouble(key, style, runTime + gcTime);
                memory.rows().setDouble(key, style, stats.getUsedMemory(StatType.MEDIAN));
            }
        });

        Chart.create().withBarPlot(times, false, chart -> {
            chart.title().withText("Native vs Morpheus (Sequential & Parallel) Median Aggregate Times (inc-GC), 1 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.plot().orient().horizontal();
            chart.legend().on();
            chart.writerPng(new File("./docs/images/native-vs-morpheus-aggregate-times.png"), 845, 400, true);
            chart.show();
        });
    }

    private static Callable<Object> createCallable(int style, Class<?> type, int size) {
        switch (style) {
            case 0: return ArrayPerf4.createNativeCallable(type, size);
            case 1: return ArrayPerf4.createMorpheusCallable(type, size, false);
            case 2: return ArrayPerf4.createMorpheusCallable(type, size, true);
            default:  throw new IllegalArgumentException("Unsupported style: " + style);
        }
    }
}
