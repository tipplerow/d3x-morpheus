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

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.util.PerfStat;
import com.d3x.morpheus.range.Range;

public class IndexPerf1 {

    public static void main(String[] args) {

        final int size = 10000000;

        DataFrame<String,String> times = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.put("int", () -> {
                final Range<Integer> range = Range.of(0, size);
                return Index.of(range);
            });

            tasks.put("Date", () -> {
                final long now = System.currentTimeMillis();
                final Range<Date> range = Range.of(0, size).map(i -> new Date(now + (i * 1000)));
                return Index.of(range);
            });
            tasks.put("Instant", () -> {
                final long now = System.currentTimeMillis();
                final Range<Instant> range = Range.of(0, size).map(i -> Instant.ofEpochMilli(now + (i * 1000)));
                return Index.of(range);
            });

            tasks.put("LocalDateTime", () -> {
                final Duration step = Duration.ofSeconds(1);
                final LocalDateTime start = LocalDateTime.now().minusYears(10);
                final LocalDateTime end = start.plusSeconds(size);
                final Range<LocalDateTime> range = Range.of(start, end, step);
                return Index.of(range);
            });

            tasks.put("ZonedDateTime", () -> {
                final Duration step = Duration.ofSeconds(1);
                final ZonedDateTime start = ZonedDateTime.now();
                final ZonedDateTime end = start.plusSeconds(size);
                final Range<ZonedDateTime> range = Range.of(start, end, step);
                return Index.of(range);
            });
        });

        Chart.create().withBarPlot(times, false, chart -> {
            chart.title().withText("Median Index Creation Times, 10 million entries");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().bottom();
            //chart.writerPng(new File("./morpheus-docs/docs/images/data-frame-create-times.png"), 845, 400);
            chart.show();
        });

    }
}
