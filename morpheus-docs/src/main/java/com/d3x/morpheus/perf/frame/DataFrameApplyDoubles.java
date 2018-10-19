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

import java.awt.Font;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;

import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.util.PerfStat;
import com.d3x.morpheus.range.Range;

public class DataFrameApplyDoubles {

    public static void main(String[] args) {

        //Sample size for timing statistics
        var count = 10;

        //Create frame with 50 million rows of Random doubles
        var rowKeys = Range.of(0, 50000000);
        var colKeys = Array.of("A", "B", "C", "D");
        var frame = DataFrame.ofDoubles(rowKeys, colKeys).applyDoubles(v -> Math.random());

        //Time sequential and parallel capping of all elements in the DataFrame
        var timing = PerfStat.run(count, TimeUnit.MILLISECONDS, true, tasks -> {
            tasks.beforeEach(() -> frame.applyDoubles(v -> Math.random()));
            tasks.put("Sequential", () -> frame.sequential().applyDoubles(v -> v.getDouble() > 0.5 ? 0.5 : v.getDouble()));
            tasks.put("Parallel", () -> frame.parallel().applyDoubles(v -> v.getDouble() > 0.5 ? 0.5 : v.getDouble()));
        });

        //Plot timing statistics as a bar chart
        Chart.create().withBarPlot(timing, false, chart -> {
            chart.title().withText("Time to Cap 200 Million DataFrame Elements (Sample 10 times)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./docs/images/frame/data-frame-apply-doubles.png"), 845, 400, true);
            chart.show();
        });
    }
}
