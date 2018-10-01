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

import com.d3x.morpheus.viz.chart.Chart;
import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.PerfStat;

public class DataFrameRowIteration {

    public static void main(String[] args) {

        //Sample size for timing statistics
        int sample = 10;

        //Create frame with 50 million rows of Random doubles
        Range<Integer> rowKeys = Range.of(0, 10000000);
        Array<String> colKeys = Array.of("A", "B", "C", "D", "E", "F", "H");
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(rowKeys, colKeys).applyDoubles(v -> Math.random());

        //Time sequential and parallel computation of mean over all rows
        DataFrame<String,String> timing = PerfStat.run(sample, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.put("Sequential", () -> {
                frame.sequential().rows().forEach(row -> row.stats().mean());
                return frame;
            });
            tasks.put("Parallel", () -> {
                frame.parallel().rows().forEach(row -> row.stats().mean());
                return frame;
            });
        });

        //Plot timing statistics as a bar chart
        Chart.create().withBarPlot(timing, false, chart -> {
            chart.title().withText("Time to Compute Arithmetic Mean of 50 Million rows (Sample 10 times)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.writerPng(new File("./docs/images/frame/data-frame-row-iteration.png"), 845, 400, true);
            chart.show();
        });
    }
}
