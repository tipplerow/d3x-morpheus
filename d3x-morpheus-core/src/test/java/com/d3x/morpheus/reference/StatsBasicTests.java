/*
 * Copyright (C) 2014-2017 Xavier Witdouck
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
package com.d3x.morpheus.reference;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.stats.StatType;
import com.d3x.morpheus.index.Index;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A Stat test that test various statistical functions and compares results to verified pre-computed datasets
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class StatsBasicTests {


    @DataProvider(name="style")
    public Object[][] style() {
        return new Object[][] { {false}, {true} };
    }

    private DataFrame<Integer,String> loadSourceData() {
        return DataFrame.read("/stats/source-data.csv").csv();
    }


    private DataFrame<Integer,StatType> loadExpectedRowStats(StatType stat) {
        switch (stat) {
            case MIN:               return DataFrame.read("/stats/row-min.csv").csv().cols().mapKeys(x -> stat);
            case MAX:               return DataFrame.read("/stats/row-max.csv").csv().cols().mapKeys(x -> stat);
            case SUM:               return DataFrame.read("/stats/row-sum.csv").csv().cols().mapKeys(x -> stat);
            case MEAN:              return DataFrame.read("/stats/row-mean.csv").csv().cols().mapKeys(x -> stat);
            case MAD:               return DataFrame.read("/stats/row-mad.csv").csv().cols().mapKeys(x -> stat);
            case SEM:               return DataFrame.read("/stats/row-sem.csv").csv().cols().mapKeys(x -> stat);
            case COUNT:             return DataFrame.read("/stats/row-count.csv").csv().cols().mapKeys(x -> stat);
            case SKEWNESS:          return DataFrame.read("/stats/row-skew.csv").csv().cols().mapKeys(x -> stat);
            case KURTOSIS:          return DataFrame.read("/stats/row-kurt.csv").csv().cols().mapKeys(x -> stat);
            case VARIANCE:          return DataFrame.read("/stats/row-var.csv").csv().cols().mapKeys(x -> stat);
            case STD_DEV:           return DataFrame.read("/stats/row-std.csv").csv().cols().mapKeys(x -> stat);
            case MEDIAN:            return DataFrame.read("/stats/row-median.csv").csv().cols().mapKeys(x -> stat);
            case PERCENTILE:        return DataFrame.read("/stats/row-percentile-80th.csv").csv().cols().mapKeys(x -> stat);
            case AUTO_CORREL:       return DataFrame.read("/stats/row-autocorr-5.csv").csv().cols().mapKeys(x -> stat);
            case SUM_SQUARES:       return DataFrame.read("/stats/row-sumSquares.csv").csv().cols().mapKeys(x -> stat);
            default:    throw new IllegalArgumentException("Unexpected stat type: " + stat);
        }
    }

    private DataFrame<StatType,String> loadExpectedColStats(StatType stat) {
        switch (stat) {
            case MIN:               return DataFrame.read("/stats/column-min.csv").csv().rows().mapKeys(x -> stat);
            case MAX:               return DataFrame.read("/stats/column-max.csv").csv().rows().mapKeys(x -> stat);
            case SUM:               return DataFrame.read("/stats/column-sum.csv").csv().rows().mapKeys(x -> stat);
            case MEAN:              return DataFrame.read("/stats/column-mean.csv").csv().rows().mapKeys(x -> stat);
            case MAD:               return DataFrame.read("/stats/column-mad.csv").csv().rows().mapKeys(x -> stat);
            case SEM:               return DataFrame.read("/stats/column-sem.csv").csv().rows().mapKeys(x -> stat);
            case COUNT:             return DataFrame.read("/stats/column-count.csv").csv().rows().mapKeys(x -> stat);
            case SKEWNESS:          return DataFrame.read("/stats/column-skew.csv").csv().rows().mapKeys(x -> stat);
            case KURTOSIS:          return DataFrame.read("/stats/column-kurt.csv").csv().rows().mapKeys(x -> stat);
            case VARIANCE:          return DataFrame.read("/stats/column-var.csv").csv().rows().mapKeys(x -> stat);
            case STD_DEV:           return DataFrame.read("/stats/column-std.csv").csv().rows().mapKeys(x -> stat);
            case MEDIAN:            return DataFrame.read("/stats/column-median.csv").csv().rows().mapKeys(x -> stat);
            case PERCENTILE:        return DataFrame.read("/stats/column-percentile-80th.csv").csv().rows().mapKeys(x -> stat);
            case AUTO_CORREL:       return DataFrame.read("/stats/column-autocorr-5.csv").csv().rows().mapKeys(x -> stat);
            case SUM_SQUARES:       return DataFrame.read("/stats/column-sumSquares.csv").csv().rows().mapKeys(x -> stat);
            default:    throw new IllegalArgumentException("Unexpected stat type: " + stat);
        }
    }


    @Test(dataProvider = "style")
    public void count(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> rawRowStats = loadExpectedRowStats(StatType.COUNT);
        final DataFrame<String,StatType> rawColStats = loadExpectedColStats(StatType.COUNT).transpose();
        final Index<Integer> rowKeys = Index.of(rawRowStats.rows().keyArray());
        final Index<String> colKeys = Index.of(rawColStats.rows().keyArray());
        final DataFrame<Integer,StatType> expectedRowStats = DataFrame.ofDoubles(rowKeys, StatType.COUNT);
        final DataFrame<String,StatType> expectedColStats = DataFrame.ofDoubles(colKeys, StatType.COUNT);
        expectedRowStats.applyDoubles(v -> rawRowStats.getDoubleAt(v.rowOrdinal(), v.colOrdinal()));
        expectedColStats.applyDoubles(v -> rawColStats.getDoubleAt(v.rowOrdinal(), v.colOrdinal()));
        source.rows().forEach(row -> Assert.assertEquals(row.stats().count(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().count(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().count();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().count();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().count();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().count();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void min(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.MIN);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.MIN).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().min(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().min(), expectedColStats.getDoubleAt(column.ordinal(), 0) , 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().min();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().min();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().min();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().min();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void max(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.MAX);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.MAX).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().max(), expectedRowStats.getDoubleAt(row.ordinal(), 0)));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().max(), expectedColStats.getDoubleAt(column.ordinal(), 0)));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().max();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().max();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().max();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().max();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void sum(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.SUM);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.SUM).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().sum(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().sum(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().sum();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().sum();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().sum();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().sum();
            rowStats.out().print();
            expectedRowStats.out().print();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void sumSquares(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.SUM_SQUARES);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.SUM_SQUARES).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().sumSquares(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().sumSquares(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().sumSquares();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().sumSquares();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().sumSquares();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().sumSquares();
            rowStats.out().print();
            expectedRowStats.out().print();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void mean(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.MEAN);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.MEAN).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().mean(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().mean(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().mean();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().mean();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().mean();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().mean();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void skew(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.SKEWNESS);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.SKEWNESS).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().skew(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().skew(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().skew();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().skew();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().skew();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().skew();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void kurtosis(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.KURTOSIS);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.KURTOSIS).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().kurtosis(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().kurtosis(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().kurtosis();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().kurtosis();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().kurtosis();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().kurtosis();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void variance(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.VARIANCE);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.VARIANCE).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().variance(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().variance(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().variance();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().variance();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().variance();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().variance();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void stdDev(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.STD_DEV);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.STD_DEV).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().stdDev(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().stdDev(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().stdDev();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().stdDev();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().stdDev();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().stdDev();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void median(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.MEDIAN);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.MEDIAN).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().median(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().median(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().median();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().median();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().median();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().median();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void percentile80th(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.PERCENTILE);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.PERCENTILE).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().percentile(0.8), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().percentile(0.8), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().percentile(0.8d);
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().percentile(0.8d);
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().percentile(0.8d);
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().percentile(0.8d);
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void autocorr(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.AUTO_CORREL);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.AUTO_CORREL).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().autocorr(5), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().autocorr(5), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().autocorr(5);
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().autocorr(5);
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().autocorr(5);
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().autocorr(5);
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void mad(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.MAD);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.MAD).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().mad(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().mad(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().mad();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().mad();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().mad();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().mad();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }


    @Test(dataProvider = "style")
    public void sem(boolean parallel) {
        final DataFrame<Integer,String> source = loadSourceData();
        final DataFrame<Integer,StatType> expectedRowStats = loadExpectedRowStats(StatType.SEM);
        final DataFrame<String,StatType> expectedColStats = loadExpectedColStats(StatType.SEM).transpose();
        source.rows().forEach(row -> Assert.assertEquals(row.stats().sem(), expectedRowStats.getDoubleAt(row.ordinal(), 0), 0.00000001));
        source.cols().forEach(column -> Assert.assertEquals(column.stats().sem(), expectedColStats.getDoubleAt(column.ordinal(), 0), 0.00000001));
        if (parallel) {
            final DataFrame<Integer,StatType> rowStats = source.rows().parallel().stats().sem();
            final DataFrame<String,StatType> colStats = source.cols().parallel().stats().sem();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        } else {
            final DataFrame<Integer,StatType> rowStats = source.rows().sequential().stats().sem();
            final DataFrame<String,StatType> colStats = source.cols().sequential().stats().sem();
            DataFrameAsserts.assertEqualsByIndex(expectedRowStats, rowStats);
            DataFrameAsserts.assertEqualsByIndex(expectedColStats, colStats);
        }
    }

}
