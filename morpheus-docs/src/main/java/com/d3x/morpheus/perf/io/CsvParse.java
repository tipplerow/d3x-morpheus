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

package com.d3x.morpheus.perf.io;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import com.d3x.morpheus.viz.chart.Chart;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.PerfStat;

public class CsvParse {

    public static void main(String[] args) {

        final String path = "/Users/witdxav/Dropbox/data/fxcm/AUDUSD/2012/AUDUSD-2012.csv";
        final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        DataFrame<String,String> timingStats = PerfStat.run(5, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.put("Sequential", () -> DataFrame.read().<LocalDateTime>csv(options -> {
                options.setHeader(false);
                options.setParallel(false);
                options.setResource(path);
                options.setExcludeColumnIndexes(1);
                options.setRowKeyParser(LocalDateTime.class, row -> {
                    final LocalDate date = LocalDate.parse(row[0], dateFormat);
                    final LocalTime time = LocalTime.parse(row[1], timeFormat);
                    return LocalDateTime.of(date, time);
                });
            }));

            tasks.put("Parallel", () -> DataFrame.read().<LocalDateTime>csv(options -> {
                options.setHeader(false);
                options.setParallel(true);
                options.setResource(path);
                options.setExcludeColumnIndexes(1);
                options.setRowKeyParser(LocalDateTime.class, row -> {
                    final LocalDate date = LocalDate.parse(row[0], dateFormat);
                    final LocalTime time = LocalTime.parse(row[1], timeFormat);
                    return LocalDateTime.of(date, time);
                });
            }));

        });

        Chart.create().withBarPlot(timingStats, false, chart -> {
            chart.title().withText("CSV Parsing Performance (Sequential vs Parallel)");
            chart.subtitle().withText("File Size: 40MB, 760,000 lines, 6 columns");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 16));
            chart.plot().axes().domain().label().withText("Statistic");
            chart.plot().axes().range(0).label().withText("Time in Milliseconds");
            chart.legend().on();
            chart.show();
        });

    }

}
