/*
 * Copyright (C) 2018-2019 D3X Systems - All Rights Reserved
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

package com.d3x.morpheus.perf;

import java.util.stream.IntStream;

import com.d3x.core.util.StopWatch;
import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.util.IO;
import org.testng.annotations.Test;

public class SortingPerf {


    @Test(enabled = false)
    public void arraySeq() {
        var array = Array.of(Double.class, 10000000).applyDoubles(v -> Math.random() * 100);
        for (int i=0; i<10; ++i) {
            var source = array.copy();
            var time = StopWatch.time(() -> source.sort(true));
            IO.println("Sorted Array in " + time.getMillis() + " millis");
        }
    }


    @Test(enabled = false)
    public void arrayPar() {
        var array = Array.of(Double.class, 10000000).applyDoubles(v -> Math.random() * 100);
        for (int i=0; i<10; ++i) {
            var source = array.copy();
            var time = StopWatch.time(() -> source.parallel().sort(0, source.length(), (v1, v2) -> {
                var d1 = v1.getDouble();
                var d2 = v2.getDouble();
                return Double.compare(d1, d2);
            }));
            IO.println("Sorted Array in " + time.getMillis() + " millis");
        }
    }


    @Test(enabled = false)
    public void seriesSeq() {
        var size = 10000000;
        var builder = DoubleSeries.builder(Integer.class).capacity(size);
        IntStream.range(0, size).forEach(i -> builder.putDouble(i, Math.random() * 100d));
        var series = builder.build();
        for (int i=0; i<10; ++i) {
            var input = series.copy();
            var time2 = StopWatch.time(() -> input.sort((i1, i2) -> {
                var d1 = input.getDoubleAt(i1);
                var d2 = input.getDoubleAt(i2);
                return Double.compare(d1, d2);
            }));
            IO.println("Series Sort " + size + " entries in " + time2 + " millis");
        }
    }


    @Test(enabled = false)
    public void seriesPar() {
        var size = 10000000;
        var builder = DoubleSeries.builder(Integer.class).capacity(size);
        IntStream.range(0, size).forEach(i -> builder.putDouble(i, Math.random() * 100d));
        var series = builder.build().parallel();
        for (int i=0; i<10; ++i) {
            var input = series.copy();
            var time2 = StopWatch.time(() -> input.sort((i1, i2) -> {
                var d1 = input.getDoubleAt(i1);
                var d2 = input.getDoubleAt(i2);
                return Double.compare(d1, d2);
            }));
            IO.println("Series Sort " + size + " entries in " + time2 + " millis");
        }
    }



    @Test(enabled = false)
    public void frameSeq() {
        var rows = Range.of(0, 10000000);
        var cols = Range.of(0, 5).map(i -> String.format("Column-%s", i));
        var frame = DataFrame.ofDoubles(rows, cols, v -> Math.random() * 100);
        for (int i=0; i<10; ++i) {
            var source = frame.copy();
            var time = StopWatch.time(() -> source.rows().sort(true, "Column-1"));
            IO.println("Sorted DataFrame in " + time.getMillis() + " millis");
        }
    }


    @Test(enabled = false)
    public void framePar() {
        var rows = Range.of(0, 10000000);
        var cols = Range.of(0, 5).map(i -> String.format("Column-%s", i));
        var frame = DataFrame.ofDoubles(rows, cols, v -> Math.random() * 100);
        for (int i=0; i<10; ++i) {
            var source = frame.copy();
            var time = StopWatch.time(() -> source.rows().parallel().sort((row1, row2) -> {
                var d1 = row1.getDoubleAt(2);
                var d2 = row2.getDoubleAt(2);
                return Double.compare(d1, d2);
            }));
            IO.println("Sorted DataFrame in " + time.getMillis() + " millis");
        }
    }


}
