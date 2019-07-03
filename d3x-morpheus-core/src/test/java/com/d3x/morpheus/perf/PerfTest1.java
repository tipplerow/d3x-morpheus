package com.d3x.morpheus.perf;

import java.util.concurrent.TimeUnit;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameCursor;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.PerfStat;

public class PerfTest1 {


    public void testAccess() {

        Range<Integer> rows = Range.of(0, 1000000);
        Range<String> cols = Range.of(0, 10).map(i -> "Column-" + i);
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(rows, cols, v -> Math.random());

        final DataFrame<String,String> results = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.put("Task-1", () -> {
                for (int i=0; i<frame.rowCount(); ++i) {
                    for (int j=0; j<frame.colCount(); ++j) {
                        DataFrameCursor<Integer,String> cursor = frame.cursor();
                        cursor.toRowAt(i).toColAt(j).setDouble(1d);
                    }
                }
                return 0d;
            });

            tasks.put("Task-2", () -> {
                for (int i=0; i<frame.rowCount(); ++i) {
                    DataFrameCursor<Integer,String> cursor = frame.cursor();
                    for (int j=0; j<frame.colCount(); ++j) {
                        cursor.toRowAt(i).toColAt(j).setDouble(2d);
                    }
                }
                return 0d;
            });

            tasks.put("Task-3", () -> {
                for (int i=0; i<frame.rowCount(); ++i) {
                    DataFrameCursor<Integer,String> cursor = frame.cursor();
                    cursor.toRowAt(i);
                    for (int j=0; j<frame.colCount(); ++j) {
                        cursor.toColAt(j).setDouble(3d);
                    }
                }
                return 0d;
            });

            tasks.put("Task-4", () -> {
                DataFrameCursor<Integer,String> cursor = frame.cursor();
                for (int i=0; i<frame.rowCount(); ++i) {
                    cursor.toRowAt(i);
                    for (int j=0; j<frame.colCount(); ++j) {
                        cursor.toColAt(j).setDouble(4d);
                    }
                }
                return 0d;
            });

            tasks.put("Task-5", () -> {
                for (int i=0; i<frame.rowCount(); ++i) {
                    for (int j=0; j<frame.colCount(); ++j) {
                        frame.cursor().toRowAt(i).toColAt(j).setDouble(5d);
                    }
                }
                return 0d;
            });

            tasks.put("Task-6", () -> {
                for (int i=0; i<frame.rowCount(); ++i) {
                    for (int j=0; j<frame.colCount(); ++j) {
                        frame.setDoubleAt(i, j, 6d);
                    }
                }
                return 0d;
            });

            tasks.put("Task-7", () -> {
                frame.sequential().applyDoubles(v -> 7d);
                return 0d;
            });

            tasks.put("Task-8", () -> {
                frame.parallel().applyDoubles(v -> 8d);
                return 0d;
            });

            tasks.put("Task-9", () -> {
            for (int i=0; i<frame.colCount(); ++i) {
                DataFrameCursor<Integer,String> cursor = frame.cursor().toColAt(i);
                for (int j=0; j<frame.rowCount(); ++j) {
                    cursor.toRowAt(j).setDouble(9d);
                }
            }
            return 0d;
        });

    });

        results.out().print();

    }

    public void test2() {

        Range<Integer> rows = Range.of(0, 1000000);
        Range<String> cols = Range.of(0, 10).map(i -> "Column-" + i);
        DataFrame<Integer,String> frame = DataFrame.ofDoubles(rows, cols, v -> Math.random());

        final DataFrame<String,String> results = PerfStat.run(10, TimeUnit.MILLISECONDS, true, tasks -> {
            tasks.beforeEach(() -> frame.applyDoubles(v -> Math.random()));

            tasks.put("Task-1", () -> {
                for (int i=0; i<frame.rowCount(); ++i) {
                    for (int j=0; j<frame.colCount(); ++j) {
                        frame.setDoubleAt(i, j, 6d);
                    }
                }
                return 0d;
            });

            tasks.put("Task-2", () -> {
                for (int j=0; j<frame.colCount(); ++j) {
                    frame.colAt(j).forEachValue(v -> v.setDouble(23d));
                }
                return 0d;
            });

            tasks.put("Task-3", () -> {
                frame.sequential().applyDoubles(v -> 7d);
                return 0d;
            });

            tasks.put("Task-4", () -> {
                frame.parallel().applyDoubles(v -> 8d);
                return 0d;
            });
        });

        results.out().print();

    }
}
