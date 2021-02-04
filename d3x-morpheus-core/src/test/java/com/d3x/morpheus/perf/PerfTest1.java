package com.d3x.morpheus.perf;


import java.util.stream.IntStream;

import com.d3x.morpheus.util.StopWatch;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.IO;
import org.testng.annotations.Test;

public class PerfTest1 {


    @Test()
    public void iterate1() {
        var rows = Range.of(0, 1000000);
        var cols = Range.of(0, 5).map(i -> String.format("Column-%s", i));
        var frame = DataFrame.ofDoubles(rows, cols, v -> Math.random());
        IntStream.range(0, 5).forEach(c -> {
            var result = StopWatch.time(() -> {
                var sum = 0d;
                var count = 0;
                for (int i=0; i<frame.rowCount(); ++i) {
                    for (int j=0; j<frame.colCount(); ++j) {
                        sum += frame.getDoubleAt(i, j);
                        ++count;
                    }
                }
                return count;
            });
            IO.println("Index access in " + result.getMillis() + " millis, count: " + result.getValue());
        });
    }


    @Test()
    public void iterate2() {
        var rows = Range.of(0, 1000000);
        var cols = Range.of(0, 5).map(i -> String.format("Column-%s", i));
        var frame = DataFrame.ofDoubles(rows, cols, v -> Math.random());
        IntStream.range(0, 5).forEach(c -> {
            var result = StopWatch.time(() -> {
                var sum = 0d;
                var count = 0;
                var row = frame.rows().cursor();
                for (int i=0; i<frame.rowCount(); ++i) {
                    row.atOrdinal(i);
                    for (int j=0; j<frame.colCount(); ++j) {
                        sum += row.getDoubleAt(j);
                        ++count;
                    }
                }
                return count;
            });
            IO.println("Index access in " + result.getMillis() + " millis, count: " + result.getValue());
        });
    }



    @Test()
    public void iterate3() {
        var rows = Range.of(0, 1000000);
        var cols = Range.of(0, 5).map(i -> String.format("Column-%s", i));
        var frame = DataFrame.ofDoubles(rows, cols, v -> Math.random());
        IntStream.range(0, 5).forEach(c -> {
            var result = StopWatch.time(() -> {
                var sum = 0d;
                var count = 0;
                for (Integer row : rows) {
                    for (String column : cols) {
                        sum += frame.getDouble(row, column);
                        ++count;
                    }
                }
                return count;
            });
            IO.println("Key access in " + result.getMillis() + " millis, count: " + result.getValue());
        });
    }



}
