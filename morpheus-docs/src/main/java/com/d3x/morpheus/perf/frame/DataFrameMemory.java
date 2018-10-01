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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.d3x.morpheus.util.MemoryEstimator;
import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;

public class DataFrameMemory {

    public static void main(String[] args) {

        final Array<String> groups = Array.of("Integer", "Long", "LocalDateTime");
        final Array<String> colKeys = Array.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
        final Array<Integer> counts = Array.of(
                500000, 1000000, 2500000, 5000000, 7500000, 10000000, 12500000, 15000000, 20000000
        );

        final MemoryEstimator memoryEstimator = new MemoryEstimator.DefaultMemoryEstimator();
        final List<String> rowKeys = counts.stream().values().map(Object::toString).collect(Collectors.toList());
        final DataFrame<String,String> results = DataFrame.ofDoubles(rowKeys, groups);

        counts.forEachValue(v -> {
            final String key = String.valueOf(v.getInt());
            final Range<Integer> range = Range.of(0, v.getInt());
            final DataFrame<Integer,String> frame = DataFrame.ofDoubles(range, colKeys);
            final long bytes = memoryEstimator.getObjectSize(frame);
            results.setDouble(key, "Integer", bytes / Math.pow(1024, 2));
        });

        counts.forEachValue(v -> {
            final String key = String.valueOf(v.getInt());
            final Range<Long> range = Range.of(0L, (long)v.getInt());
            final DataFrame<Long,String> frame = DataFrame.ofDoubles(range, colKeys);
            final long bytes = memoryEstimator.getObjectSize(frame);
            results.setDouble(key, "Long", bytes / Math.pow(1024, 2));
        });

        counts.forEachValue(v -> {
            final String key = String.valueOf(v.getInt());
            final LocalDateTime start = LocalDateTime.now();
            final Range<LocalDateTime> range = Range.of(start, start.plusSeconds(v.getInt()), Duration.ofSeconds(1));
            final DataFrame<LocalDateTime,String> frame = DataFrame.ofDoubles(range, colKeys);
            final long bytes = memoryEstimator.getObjectSize(frame);
            results.setDouble(key, "LocalDateTime", bytes / Math.pow(1024, 2));
        });

        Chart.create().withBarPlot(results, false, chart -> {
            chart.title().withText("DataFrame Memory Usage With Increasing Row Count (10 columns of doubles)");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Row Count");
            chart.plot().axes().range(0).label().withText("Memory Usage (MB)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-memory.png"), 845, 400, true);
            chart.show();
        });

    }


    public void gcTimes() {

    }

}
