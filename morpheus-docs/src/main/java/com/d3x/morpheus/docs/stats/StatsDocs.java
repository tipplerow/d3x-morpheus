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
package com.d3x.morpheus.docs.stats;

import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import com.d3x.morpheus.util.IO;
import org.testng.annotations.Test;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameRow;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.stats.StatType;
import com.d3x.morpheus.stats.Stats;
import com.d3x.morpheus.util.PerfStat;
import com.d3x.morpheus.viz.chart.Chart;

public class StatsDocs {


    /**
     * Returns a DataFrame of motor vehicles features
     * @return  the frame of motor vehicle features
     */
    static DataFrame<Integer,String> loadCarDataset() {
        return DataFrame.read().csv(options -> {
            options.setResource("https://www.d3xsystems.com/public/data/samples/cars93.csv");
            options.setExcludeColumnIndexes(0);
        });
    }


    /**
     * Returns a frame of random double precision values
     * @param rowCount  the row count for frame
     * @param columns   the column keys
     * @return          the newly created frame
     */
    static DataFrame<LocalDate,String> random(int rowCount, String... columns) {
        var start = LocalDate.now().minusDays(rowCount);
        return DataFrame.ofDoubles(
            Range.of(0, rowCount).map(start::plusDays),
            Array.of(columns),
            value -> Math.random() * 100d
        );
    }


    @Test()
    public void frameStats() {

        //Create 100x5 DataFrame of random doubles
        var frame = random(100, "A", "B", "C", "D", "E");

        IO.println("\n");
        IO.printf("Count = %.4f\n", frame.stats().count());
        IO.printf("Minimum = %.4f\n", frame.stats().min());
        IO.printf("Maximum = %.4f\n", frame.stats().max());
        IO.printf("Mean = %.4f\n", frame.stats().mean());
        IO.printf("Median = %.4f\n", frame.stats().median());
        IO.printf("Variance = %.4f\n", frame.stats().variance());
        IO.printf("StdDev = %.4f\n", frame.stats().stdDev());
        IO.printf("Skew = %.4f\n", frame.stats().skew());
        IO.printf("Kurtosis = %.4f\n", frame.stats().kurtosis());
        IO.printf("Mean Abs Deviation = %.4f\n", frame.stats().mad());
        IO.printf("Sum = %.4f\n", frame.stats().sum());
        IO.printf("Sum of Squares = %.4f\n", frame.stats().sumSquares());
        IO.printf("Std Error of Mean = %.4f\n", frame.stats().sem());
        IO.printf("Geometric Mean = %.4f\n", frame.stats().geoMean());
        IO.printf("Percentile(75th) = %.4f\n", frame.stats().percentile(0.75d));
        IO.printf("Autocorrelation(2) = %.4f\n", frame.stats().autocorr(2));

        var count = frame.cols().stats().count();
        var min = frame.cols().stats().min();
        var max = frame.cols().stats().max();
        var mean = frame.cols().stats().mean();
        var median = frame.cols().stats().median();
        var variance = frame.cols().stats().variance();
        var stdDev = frame.cols().stats().stdDev();
        var kurtosis = frame.cols().stats().kurtosis();
        var mad = frame.cols().stats().mad();
        var sum = frame.cols().stats().sum();
        var sumSquares = frame.cols().stats().sumSquares();
        var sem = frame.cols().stats().sem();
        var geoMean = frame.cols().stats().geoMean();

        frame.cols().describe(
            StatType.COUNT,
            StatType.MIN,
            StatType.MAX,
            StatType.MEAN,
            StatType.VARIANCE,
            StatType.STD_DEV,
            StatType.KURTOSIS
        ).out().print();
    }

    @Test()
    public void rowStats1() {

        //Create 100x5 DataFrame of random doubles
        var frame = random(100, "A", "B", "C", "D", "E");
        var date = frame.rows().key(3);
        var stats1 = frame.rowAt(3).stats();
        var stats2 = frame.row(date).stats();

        StatType.univariate().forEach(statType -> {
            switch (statType) {
                case COUNT:         assert(stats1.count().doubleValue() == stats2.count());                     break;
                case MIN:           assert(stats1.min().doubleValue() == stats2.min());                         break;
                case MAX:           assert(stats1.max().doubleValue() == stats2.max());                         break;
                case MEAN:          assert(stats1.mean().doubleValue() == stats2.mean());                       break;
                case MEDIAN:        assert(stats1.median().doubleValue() == stats2.median());                   break;
                case VARIANCE:      assert(stats1.variance().doubleValue() == stats2.variance());               break;
                case STD_DEV:       assert(stats1.stdDev().doubleValue() == stats2.stdDev());                   break;
                case KURTOSIS:      assert(stats1.kurtosis().doubleValue() == stats2.kurtosis());               break;
                case MAD:           assert(stats1.mad().doubleValue() == stats2.mad());                         break;
                case SEM:           assert(stats1.sem().doubleValue() == stats2.sem());                         break;
                case GEO_MEAN:      assert(stats1.geoMean().doubleValue() == stats2.geoMean());                 break;
                case SUM:           assert(stats1.sum().doubleValue() == stats2.sum());                         break;
                case SUM_SQUARES:   assert(stats1.sumSquares().doubleValue() == stats2.sumSquares());           break;
                case AUTO_CORREL:   assert(stats1.autocorr(2).doubleValue() == stats2.autocorr(2));             break;
                case PERCENTILE:    assert(stats1.percentile(0.75).doubleValue() == stats2.percentile(0.75));   break;
            }
        });
    }


    @Test()
    public void rowStats() {

        var frame = random(10, "A", "B", "C", "D", "E");

        frame.out().print();

        var count = frame.rows().stats().count();
        var min = frame.rows().stats().min();
        var max = frame.rows().stats().max();
        var mean = frame.rows().stats().mean();
        var median = frame.rows().stats().median();
        var variance = frame.rows().stats().variance();
        var stdDev = frame.rows().stats().stdDev();
        var kurtosis = frame.rows().stats().kurtosis();
        var mad = frame.rows().stats().mad();
        var sum = frame.rows().stats().sum();
        var sumLogs = frame.rows().stats().sumLogs();
        var sumSquares = frame.rows().stats().sumSquares();
        var sem = frame.rows().stats().sem();
        var geoMean = frame.rows().stats().geoMean();
        var autocorr = frame.rows().stats().autocorr(1);
        var percentile = frame.rows().stats().percentile(0.75d);

        var rowStats = frame.rows().describe(
            StatType.COUNT, StatType.MEAN, StatType.VARIANCE, StatType.SKEWNESS, StatType.SUM
        );
        rowStats.out().print();
    }

    @Test()
    public void rowDemean() throws Exception {

        //Create a 1,000,000x10 DataFrame of random double precision values
        var frame = random(1000000, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J");

        //Run 10 performance samples, randomizing the frame before each test
        var timing = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {

            tasks.beforeEach(() -> frame.applyDoubles(v -> Math.random() * 100d));

            tasks.put("Bad", () -> {
                for (int i=0; i<frame.rowCount(); ++i) {
                    var row = frame.rowAt(i);
                    var mean = row.stats().mean();
                    row.applyDoubles(v -> v.getDouble() - mean);
                }
                return frame;
            });

            tasks.put("Good(sequential)", () -> {
                frame.rows().forEach(row -> {
                    var mean = row.stats().mean();
                    row.applyDoubles(v -> v.getDouble() - mean);
                });
                return frame;
            });

            tasks.put("Good(parallel)", () -> {
                frame.rows().parallel().forEach(row -> {
                    var mean = row.stats().mean();
                    row.applyDoubles(v -> v.getDouble() - mean);
                });
                return frame;
            });
        });

        //Plot a chart of the results
        Chart.create().withBarPlot(timing, false, chart -> {
            chart.title().withText("DataFrame Row Demeaning Performance (10 Samples)");
            chart.subtitle().withText("DataFrame Dimension: 1 Million x 10");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-row-demean.png"), 845, 400, true);
            chart.show();
        });

        Thread.currentThread().join();

    }

    @Test()
    public void colStats() {
        var frame = random(100, "A", "B", "C", "D", "E");

        frame.out().print();

        var count = frame.cols().stats().count();
        var min = frame.cols().stats().min();
        var max = frame.cols().stats().max();
        var mean = frame.cols().stats().mean();
        var median = frame.cols().stats().median();
        var variance = frame.cols().stats().variance();
        var stdDev = frame.cols().stats().stdDev();
        var kurtosis = frame.cols().stats().kurtosis();
        var mad = frame.cols().stats().mad();
        var sum = frame.cols().stats().sum();
        var sumLogs = frame.cols().stats().sumLogs();
        var sumSquares = frame.cols().stats().sumSquares();
        var sem = frame.cols().stats().sem();
        var geoMean = frame.cols().stats().geoMean();
        var autocorr = frame.cols().stats().autocorr(1);
        var percentile = frame.cols().stats().percentile(0.75d);

        percentile.out().print();

        var colStats = frame.cols().describe(
            StatType.COUNT, StatType.MEAN, StatType.VARIANCE, StatType.SKEWNESS, StatType.SUM
        );
        colStats.out().print();

    }


    @Test()
    public void colStats1() {

        //Create 100x5 DataFrame of random doubles
        var frame = random(100, "A", "B", "C", "D", "E");
        var stats1 = frame.colAt(3).stats();
        var stats2 = frame.col("D").stats();

        StatType.univariate().forEach(statType -> {
            switch (statType) {
                case COUNT:         assert(stats1.count().doubleValue() == stats2.count());                     break;
                case MIN:           assert(stats1.min().doubleValue() == stats2.min());                         break;
                case MAX:           assert(stats1.max().doubleValue() == stats2.max());                         break;
                case MEAN:          assert(stats1.mean().doubleValue() == stats2.mean());                       break;
                case MEDIAN:        assert(stats1.median().doubleValue() == stats2.median());                   break;
                case VARIANCE:      assert(stats1.variance().doubleValue() == stats2.variance());               break;
                case STD_DEV:       assert(stats1.stdDev().doubleValue() == stats2.stdDev());                   break;
                case KURTOSIS:      assert(stats1.kurtosis().doubleValue() == stats2.kurtosis());               break;
                case MAD:           assert(stats1.mad().doubleValue() == stats2.mad());                         break;
                case SEM:           assert(stats1.sem().doubleValue() == stats2.sem());                         break;
                case GEO_MEAN:      assert(stats1.geoMean().doubleValue() == stats2.geoMean());                 break;
                case SUM:           assert(stats1.sum().doubleValue() == stats2.sum());                         break;
                case SUM_SQUARES:   assert(stats1.sumSquares().doubleValue() == stats2.sumSquares());           break;
                case AUTO_CORREL:   assert(stats1.autocorr(2).doubleValue() == stats2.autocorr(2));             break;
                case PERCENTILE:    assert(stats1.percentile(0.75).doubleValue() == stats2.percentile(0.75));   break;
            }
        });
    }


    @Test()
    public void expanding() {
        var frame = random(100, "A", "B", "C", "D", "E");
        var expandingMean = frame.cols().stats().expanding(5).mean();
        expandingMean.out().print(10);
    }


    @Test()
    public void rolling() {
        var frame = random(100, "A", "B", "C", "D", "E");
        var rollingMean = frame.cols().stats().rolling(5).mean();
        rollingMean.out().print(10);

    }




    @Test()
    public void testStats1() {
        var frame = loadCarDataset();
        frame.out().print();
        frame.cols().describe(
            StatType.MEAN,
            StatType.MIN,
            StatType.MAX
        ).out(). print();
    }

    @Test()
    public void testStats2() {
        var frame = loadCarDataset();
        frame.out().print();
        frame.transpose().rows().describe(
            StatType.MEAN,
            StatType.MIN,
            StatType.MAX
        ).out(). print();
    }

    @Test()
    public void testNonNumeric1() {
        var frame = loadCarDataset();
        var colStats = frame.cols().describe(
            StatType.COUNT, StatType.MEAN, StatType.VARIANCE, StatType.SKEWNESS, StatType.SUM
        );

        frame.out().print();
        colStats.out().print();

    }

    @Test()
    public void testCovariance1() {
        var frame = loadCarDataset();
        var covar1 = frame.cols().stats().covariance("Price", "Horsepower");
        var covar2 = frame.cols().stats().covariance("EngineSize", "MPG.city");
        IO.printf("\nCovariance between Price & Horsepower = %.2f", covar1);
        IO.printf("\nCovariance between EngineSize and MPG.city = %.2f", covar2);
    }

    @Test()
    public void testCovariance2() {
        var frame = loadCarDataset();
        var covMatrix = frame.cols().stats().covariance();
        covMatrix.out().print(100, formats -> {
            formats.setDecimalFormat("0.000;-0.000", 1);
        });
    }


    @Test()
    public void testCorrelation1() {
        var frame = loadCarDataset();
        var correl1 = frame.cols().stats().correlation("Price", "Horsepower");
        var correl2 = frame.cols().stats().correlation("EngineSize", "MPG.city");
        IO.printf("\nCorrelation between Price & Horsepower = %.2f", correl1);
        IO.printf("\nCorrelation between EngineSize and MPG.city = %.2f", correl2);
    }

    @Test()
    public void testCorrelation2() {
        var frame = loadCarDataset();
        var correlMatrix = frame.cols().stats().correlation();
        correlMatrix.out().print(100, formats -> {
            formats.setDecimalFormat("0.000;-0.000", 1);
        });
    }


    @Test()
    public void correlationPerformance() throws Exception {

        //Create a 1,000,000x10 DataFrame of random double precision values
        var frame = random(1000000, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J");

        //Run 10 performance samples, randomizing the frame before each test
        var timing = PerfStat.run(10, TimeUnit.MILLISECONDS, false, tasks -> {
            tasks.beforeEach(() -> frame.applyDoubles(v -> Math.random() * 100d));
            tasks.put("Sequential", () -> frame.cols().stats().correlation());
            tasks.put("Parallel", () -> frame.cols().parallel().stats().correlation());
        });

        //Plot a chart of the results
        Chart.create().withBarPlot(timing, false, chart -> {
            chart.title().withText("DataFrame Correlation Matrix Performance (10 Samples)");
            chart.subtitle().withText("DataFrame Dimension: 1 Million x 10");
            chart.title().withFont(new Font("Verdana", Font.PLAIN, 15));
            chart.plot().axes().domain().label().withText("Timing Statistic");
            chart.plot().axes().range(0).label().withText("Time (Milliseconds)");
            chart.legend().on().bottom();
            chart.writerPng(new File("./docs/images/frame/data-frame-column-correl.png"), 845, 400, true);
            chart.show();
        });

        Thread.currentThread().join();

    }

}
