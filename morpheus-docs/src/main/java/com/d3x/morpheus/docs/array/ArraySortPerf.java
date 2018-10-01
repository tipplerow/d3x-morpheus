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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.PerfStat;

public class ArraySortPerf {

    public static void main(String[] args) {
        testWithDoubles(5);
        testWithLocalDatesTimes(5);
    }



    /**
     * Tests Native sort performance for a range of lengths
     * @param sample    the number of samples to run
     */
    private static void testWithDoubles(int sample) {

        Range<Integer> arrayLengths = Range.of(1, 11).map(i -> i * 100000);
        Array<String> labels = Array.of("Native(Seq)", "Morpheus(Seq)", "Native(Par)", "Morpheus(Par)");
        DataFrame<String,String> results = DataFrame.ofDoubles(arrayLengths.map(String::valueOf), labels);

        arrayLengths.forEach(length -> {

            System.out.println("Running sort test for array length " + length);
            double[] array1 = new double[length];
            Array<Double> array2 = Array.of(Double.class, length);

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
                tasks.put("Native(Seq)", () -> { Arrays.sort(array1); return array1; });
                tasks.put("Morpheus(Seq)", () -> array2.sort(true) );
                tasks.put("Native(Par)", () -> { Arrays.parallelSort(array1); return array1; });
                tasks.put("Morpheus(Par)", () -> array2.parallel().sort(true));
                tasks.beforeEach(() -> {
                    array2.applyDoubles(v -> Math.random());
                    array2.forEachValue(v -> array1[v.index()] = v.getDouble());
                });
            });

            String label = String.valueOf(length);
            results.setDouble(label, "Native(Seq)", timing.getDouble("Mean", "Native(Seq)"));
            results.setDouble(label, "Morpheus(Seq)", timing.getDouble("Mean", "Morpheus(Seq)"));
            results.setDouble(label, "Native(Par)", timing.getDouble("Mean", "Native(Par)"));
            results.setDouble(label, "Morpheus(Par)", timing.getDouble("Mean", "Morpheus(Par)"));
        });

        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText("Sorting Performance for Array of Random Doubles (Sample " + sample + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 16));
            chart.subtitle().withText("Dual-Pivot Quick Sort (Native) vs Single-Pivot Quick Sort (FastUtil)");
            chart.subtitle().withFont(new Font("Verdana", Font.PLAIN, 14));
            chart.plot().axes().domain().label().withText("Array Length");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./docs/images/array-sort-native-vs-morpheus-1.png"), 845, 400, true);
            chart.show();
        });
    }



    private static void testWithLocalDatesTimes(int sample) {

        Range<Integer> arrayLengths = Range.of(1, 11).map(i -> i * 100000);
        Array<String> labels = Array.of("Native(Seq)", "Morpheus(Seq)", "Native(Par)", "Morpheus(Par)");
        DataFrame<String,String> results = DataFrame.ofDoubles(arrayLengths.map(String::valueOf), labels);

        arrayLengths.forEach(length -> {

            System.out.println("Running sort test for array length " + length);
            final LocalDateTime start = LocalDateTime.now();
            final LocalDateTime[] array1 = new LocalDateTime[length];
            final Array<LocalDateTime> array2 = Range.of(0, length.intValue()).map(start::plusSeconds).toArray();

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
                tasks.put("Native(Seq)", () -> { Arrays.sort(array1); return array1; });
                tasks.put("Morpheus(Seq)", () -> array2.sort(true) );
                tasks.put("Native(Par)", () -> { Arrays.parallelSort(array1); return array1; });
                tasks.put("Morpheus(Par)", () -> array2.parallel().sort(true));
                tasks.beforeEach(() -> {
                    array2.shuffle(2);
                    array2.forEachValue(v -> array1[v.index()] = v.getValue());
                });
            });

            String label = String.valueOf(length);
            results.setDouble(label, "Native(Seq)", timing.getDouble("Mean", "Native(Seq)"));
            results.setDouble(label, "Morpheus(Seq)", timing.getDouble("Mean", "Morpheus(Seq)"));
            results.setDouble(label, "Native(Par)", timing.getDouble("Mean", "Native(Par)"));
            results.setDouble(label, "Morpheus(Par)", timing.getDouble("Mean", "Morpheus(Par)"));
        });

        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText("Sorting Performance for Array of Random LocalDateTimes (Sample " + sample + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 16));
            chart.subtitle().withText("Dual-Pivot Quick Sort (Native) vs Single-Pivot Quick Sort (FastUtil)");
            chart.subtitle().withFont(new Font("Verdana", Font.PLAIN, 14));
            chart.plot().axes().domain().label().withText("Array Length");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./docs/images/array-sort-native-vs-morpheus-2.png"), 845, 400, true);
            chart.show();
        });
    }


}
