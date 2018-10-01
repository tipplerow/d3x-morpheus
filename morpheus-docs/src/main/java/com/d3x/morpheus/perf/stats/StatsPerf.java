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

package com.d3x.morpheus.perf.stats;


import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.d3x.morpheus.viz.chart.Chart;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.stats.Median;
import com.d3x.morpheus.util.PerfStat;

public class StatsPerf {

    public static void main(String[] args) {
        median();
        //medianPerf();
    }


    private static void median() {

        final Median median = new Median();
        for (int i=0; i<1000; ++i) {
            median.add(Math.random());
        }
        System.out.println(median.getValue());

    }


    private static void medianPerf() {

        final double[] values = ThreadLocalRandom.current().doubles(10000000).toArray();

        final DataFrame<String,String> results = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.put("Morpheus", () -> {
                final Median median = new Median();
                for (double value : values) {
                    median.add(value);
                }
                return median.getValue();
            });

            tasks.put("Apache", () -> {
                final DescriptiveStatistics stats = new DescriptiveStatistics();
                for (double value : values) {
                    stats.addValue(value);
                }
                return stats.getPercentile(50d);
            });
        });

        //Plot timing statistics as a bar chart
        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText("Median Calculation Times for Random Array of 10 Million elements");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Total Time in Milliseconds");
            chart.legend().on();
            chart.show();
        });


    }
}
