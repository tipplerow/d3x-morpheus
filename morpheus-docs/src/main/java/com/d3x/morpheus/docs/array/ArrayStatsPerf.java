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
import java.util.concurrent.TimeUnit;

import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.PerfStat;

public class ArrayStatsPerf {

    public static void main(String[] args) {
        var count = 10;
        var size = 10000000;
        var array = Array.of(Double.class, size).applyDoubles(v -> Math.random() * 100);

        var times = PerfStat.run(count, TimeUnit.MILLISECONDS, true, tasks -> {
            tasks.put("Min", () -> array.stats().min());
            tasks.put("Max", () -> array.stats().max());
            tasks.put("Mean", () -> array.stats().mean());
            tasks.put("Count", () -> array.stats().count());
            tasks.put("Variance", () -> array.stats().variance());
            tasks.put("StdDev", () -> array.stats().stdDev());
            tasks.put("Sum", () -> array.stats().sum());
            tasks.put("Skew", () -> array.stats().skew());
            tasks.put("Kurtosis", () -> array.stats().kurtosis());
            tasks.put("Median", () -> array.stats().median());
            tasks.put("95th Percentile", () -> array.stats().percentile(0.95));
            tasks.put("AutCorrelation(20)", () -> array.stats().autocorr(20));
        });

        Chart.create().withBarPlot(times.rows().select("Mean").transpose(), false, chart -> {
            chart.title().withText("Morpheus Array Statistic Calculation Times, 10 Million Entries (Sample 10)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Stat Type");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.plot().orient().horizontal();
            chart.legend().off();
            chart.writerPng(new File("./docs/images/morpheus-stat-times.png"), 845, 400, true);
            chart.show();
        });
    }

}
