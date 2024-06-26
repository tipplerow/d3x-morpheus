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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.d3x.morpheus.viz.chart.Chart;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.stats.StatType;
import com.d3x.morpheus.util.PerfStat;

public class ArrayPerf6 {

    public static void main(String[] args) {

        final int count = 5;
        final int size = 5000000;

        final List<Class<?>> types = Arrays.asList(int.class, long.class, double.class, Date.class, LocalDate.class, String.class, ZonedDateTime.class);
        final Array<String> colKeys = Array.of("Morpheus (sequential)", "Morpheus (parallel)");
        final List<String> rowKeys = types.stream().map(Class::getSimpleName).collect(Collectors.toList());
        final DataFrame<String,String> memory = DataFrame.ofDoubles(rowKeys, colKeys);
        final DataFrame<String,String> times = DataFrame.ofDoubles(rowKeys, colKeys);
        types.forEach(type -> {
            for (int style : new int[] {0, 1}) {
                System.out.println("Running tests for " + type);
                final String key = type.getSimpleName();
                final Callable<Object> callable = createCallable(style, type, size);
                final PerfStat stats = PerfStat.run(key, count, TimeUnit.MILLISECONDS, callable);
                final double runTime = stats.getCallTime(StatType.MEDIAN);
                times.rows().setDouble(key, style, runTime);
                memory.rows().setDouble(key, style, stats.getUsedMemory(StatType.MEDIAN));
            }
        });

        Chart.create().withBarPlot(times, false, chart -> {
            chart.title().withText("Morpheus (Sequential vs Parallel) Median Iteration Times, 5 Million Entries (Sample " + count + ")");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Data Type");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.plot().orient().horizontal();
            chart.legend().on();
            chart.writerPng(new File("./morpheus-docs/docs/images/morpheus-iteration-times.png"), 845, 400, true);
            chart.show();
        });
    }

    private static Callable<Object> createCallable(int style, Class<?> type, int size) {
        switch (style) {
            case 0: return createMorpheusCallable(type, size, false);
            case 1: return createMorpheusCallable(type, size, true);
            default:  throw new IllegalArgumentException("Unsupported style: " + style);
        }
    }


    /**
     * Returns a newly created to test Morpheus Array iteration performance
     * @param type  the array element type
     * @param size  the size for the array
     * @return      the newly created Callable
     */
    static Callable<Object> createMorpheusCallable(Class<?> type, int size, boolean parallel) {
        if (type == boolean.class) {
            final AtomicInteger count = new AtomicInteger();
            final Array<Boolean> array = parallel ? Array.of(Boolean.class, size).parallel() : Array.of(Boolean.class, size);
            array.applyBooleans(v -> Math.random() > 0.5);
            return () -> {
                array.forEachBoolean(value -> {
                    if (!value) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == int.class) {
            final AtomicInteger count = new AtomicInteger();
            final Random random = new Random();
            final Array<Integer> array = parallel ? Array.of(Integer.class, size).parallel() : Array.of(Integer.class, size);
            final int toFind = random.nextInt();
            array.applyInts(v -> random.nextInt());
            return () -> {
                array.forEachInt(value -> {
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == long.class) {
            final AtomicInteger count = new AtomicInteger();
            final Random random = new Random();
            final Array<Long> array = parallel ? Array.of(Long.class, size).parallel() : Array.of(Long.class, size);
            final long toFind = random.nextLong();
            array.applyLongs(v -> random.nextLong());
            return () -> {
                array.forEachLong(value -> {
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == double.class) {
            final AtomicInteger count = new AtomicInteger();
            final Array<Double> array = parallel ? Array.of(Double.class, size).parallel() : Array.of(Double.class, size);
            final double toFind = Math.random();
            array.applyDoubles(v -> Math.random());
            return () -> {
                array.forEachDouble(value -> {
                    if (value == toFind) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == Date.class) {
            final AtomicInteger count = new AtomicInteger();
            final long now = System.currentTimeMillis();
            final Array<Date> array = parallel ? Array.of(Date.class, size).parallel() : Array.of(Date.class, size);
            final Date toFind = new Date((long) (now * Math.random()));
            array.applyValues(v -> new Date((long) (now * Math.random())));
            return () -> {
                array.forEach(value -> {
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == String.class) {
            final AtomicInteger count = new AtomicInteger();
            final Array<String> array = parallel ? Array.of(String.class, size).parallel() : Array.of(String.class, size);
            final String toFind = String.valueOf(Math.random());
            array.applyValues(v -> String.valueOf(Math.random()));
            return () -> {
                array.forEach(value -> {
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == LocalDate.class) {
            final AtomicInteger count = new AtomicInteger();
            final long now = LocalDate.now().toEpochDay();
            final Array<LocalDate> array = parallel ? Array.of(LocalDate.class, size).parallel() : Array.of(LocalDate.class, size);
            final LocalDate toFind = LocalDate.ofEpochDay((long)(now * Math.random()));
            array.applyValues(v -> LocalDate.ofEpochDay((long)(now * Math.random())));
            return () -> {
                array.forEach(value -> {
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else if (type == ZonedDateTime.class) {
            final AtomicInteger count = new AtomicInteger();
            final ZonedDateTime now = ZonedDateTime.now();
            final Array<ZonedDateTime> array = parallel ? Array.of(ZonedDateTime.class, size).parallel() : Array.of(ZonedDateTime.class, size);
            final ZonedDateTime toFind = now.plusDays(2);
            array.applyValues(v -> now.plusSeconds(v.index()));
            return () -> {
                array.forEach(value -> {
                    if (value.equals(toFind)) {
                        count.incrementAndGet();
                    }
                });
                System.out.println("Found " + count + " matches");
                return array;
            };
        } else {
            throw new IllegalArgumentException("Unsupported type specified: " + type);
        }
    }
}
