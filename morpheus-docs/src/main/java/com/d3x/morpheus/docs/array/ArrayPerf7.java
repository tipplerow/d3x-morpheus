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

import java.awt.*;
import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.PerfStat;

public class ArrayPerf7 {

    public static void main(String[] args) {

        final int sample = 5;
        final boolean includeGC = true;

        Range<Integer> arrayLengths = Range.of(1, 11).map(i -> i * 100000);
        Array<String> labels = Array.of("Native(Seq)", "Morpheus(Seq)", "Native(Par)", "Morpheus(Par)");
        DataFrame<String,String> results = DataFrame.ofDoubles(arrayLengths.map(String::valueOf), labels);

        arrayLengths.forEach(arrayLength -> {

            System.out.printf("\nRunng tests with array length of %s", arrayLength);

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, includeGC, tasks -> {

                tasks.put("Native(Seq)", () -> {
                    final AtomicInteger count = new AtomicInteger();
                    final LocalDateTime start = LocalDateTime.now().minusYears(5);
                    final LocalDateTime[] array = new LocalDateTime[arrayLength];
                    for (int i=0; i<array.length; ++i) {
                        array[i] = start.plusMinutes(i);
                    }
                    for (LocalDateTime value : array) {
                        if (value.getDayOfWeek() == DayOfWeek.MONDAY) {
                            count.incrementAndGet();
                        }
                    }
                    return array;
                });

                tasks.put("Morpheus(Seq)", () -> {
                    final AtomicInteger count = new AtomicInteger();
                    final LocalDateTime start = LocalDateTime.now().minusYears(5);
                    final Array<LocalDateTime> array = Array.of(LocalDateTime.class, arrayLength);
                    array.applyValues(v -> start.plusMinutes(v.index()));
                    array.forEach(value -> {
                        if (value.getDayOfWeek() == DayOfWeek.MONDAY) {
                            count.incrementAndGet();
                        }
                    });
                    return array;
                });

                tasks.put("Native(Par)", () -> {
                    final AtomicInteger count = new AtomicInteger();
                    final LocalDateTime start = LocalDateTime.now().minusYears(5);
                    final IntStream indexes = IntStream.range(0, arrayLength).parallel();
                    final Stream<LocalDateTime> dates = indexes.mapToObj(start::plusMinutes);
                    final LocalDateTime[] array = dates.toArray(LocalDateTime[]::new);
                    Stream.of(array).parallel().forEach(value -> {
                        if (value.getDayOfWeek() == DayOfWeek.MONDAY) {
                            count.incrementAndGet();
                        }
                    });
                    return array;
                });

                tasks.put("Morpheus(Par)", () -> {
                    final AtomicInteger count = new AtomicInteger();
                    final LocalDateTime start = LocalDateTime.now().minusYears(5);
                    final Array<LocalDateTime> array = Array.of(LocalDateTime.class, arrayLength);
                    array.parallel().applyValues(v -> start.plusMinutes(v.index()));
                    array.parallel().forEach(value -> {
                        if (value.getDayOfWeek() == DayOfWeek.MONDAY) {
                            count.incrementAndGet();
                        }
                    });
                    return array;
                });

            });

            String label = String.valueOf(arrayLength);
            results.setDouble(label, "Native(Seq)", timing.getDouble("Mean", "Native(Seq)"));
            results.setDouble(label, "Morpheus(Seq)", timing.getDouble("Mean", "Morpheus(Seq)"));
            results.setDouble(label, "Native(Par)", timing.getDouble("Mean", "Native(Par)"));
            results.setDouble(label, "Morpheus(Par)", timing.getDouble("Mean", "Morpheus(Par)"));

        });

        //Create title from template
        final String prefix = "LocalDateTime Array Initialization + Traversal Times";
        final String title = prefix + (includeGC ? " (including-GC)" : " (excluding-GC)");

        //Record chart to file
        final String fileSuffix = includeGC ? "2.png" : "1.png";
        final String filePrefix = "./docs/images/native-vs-morpheus-array-sequential-vs-parallel";

        //Plot results as a bar chart
        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText(title);
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Array Length");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on();
            chart.writerPng(new File(filePrefix + fileSuffix), 845, 400, true);
            chart.show();
        });
    }
}
