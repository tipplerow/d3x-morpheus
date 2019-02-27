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

package com.d3x.morpheus.perf.frame;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.PerfStat;

public class DataFrameSorting {

    public static void main(String[] args) {

        int sample = 5;

        int[] lengths = IntStream.range(1, 11).map(i -> i * 1000000).toArray();

        DataFrame<String,String> results = DataFrame.ofDoubles(
            IntStream.of(lengths).mapToObj(String::valueOf).collect(Collectors.toList()),
            Arrays.asList("Sequential", "Parallel")
        );

        for (int length : lengths) {

            System.out.println("Running sort test for frame length " + length);
            Array<Integer> rowKeys = Range.of(0, length).toArray().shuffle(2);
            Array<String> colKeys = Array.of("A", "B", "C", "D");
            DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys).applyDoubles(v -> Math.random());

            DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
                tasks.beforeEach(() -> frame.rows().sort(null));
                tasks.put("Sequential", () -> frame.rows().sequential().sort(true, "A"));
                tasks.put("Parallel", () -> frame.rows().parallel().sort(true, "A"));
            });

            String label = String.valueOf(length);
            results.setDouble(label, "Sequential", timing.getDouble("Mean", "Sequential"));
            results.setDouble(label, "Parallel", timing.getDouble("Mean", "Parallel"));
        }

        //Plot timing statistics as a bar chart
        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText("Time to Sort Morpheus DataFrame of random doubles (Sample 5 times)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./morpheus-docs/docs/images/data-frame-sort.png"), 845, 400, true);
            chart.show();
        });
    }

}
