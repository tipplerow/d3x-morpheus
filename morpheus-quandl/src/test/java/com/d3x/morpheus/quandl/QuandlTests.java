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
package com.d3x.morpheus.quandl;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import com.zavtech.morpheus.frame.DataFrame;
import com.zavtech.morpheus.frame.DataFrameValue;
import com.zavtech.morpheus.util.IO;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.d3x.morpheus.quandl.QuandlField.DATABASE_CODE;
import static com.d3x.morpheus.quandl.QuandlField.DATASET_CODE;
import static com.d3x.morpheus.quandl.QuandlField.DATASET_COUNT;
import static com.d3x.morpheus.quandl.QuandlField.DESCRIPTION;
import static com.d3x.morpheus.quandl.QuandlField.DOWNLOADS;
import static com.d3x.morpheus.quandl.QuandlField.END_DATE;
import static com.d3x.morpheus.quandl.QuandlField.IMAGE_URL;
import static com.d3x.morpheus.quandl.QuandlField.LAST_REFRESH_TIME;
import static com.d3x.morpheus.quandl.QuandlField.NAME;
import static com.d3x.morpheus.quandl.QuandlField.PREMIUM;
import static com.d3x.morpheus.quandl.QuandlField.START_DATE;

/**
 * A unit test for testing the Quandl download adapter
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class QuandlTests {

    private QuandlSource source = new QuandlSource("NSXspMMxn41-Y-9w_hw_");

    @DataProvider(name="wiki")
    public Object[][] wiki() {
        return new Object[][] {
            {"AAPL", 12},
            {"MSFT", 12},
            {"GE", 12}
        };
    }

    @DataProvider(name="metadata1")
    public Object[][] metadata1() {
        return new Object[][] {
                {"WIKI"},
                {"FRED"},
                {"CBOE"}
        };
    }


    @DataProvider(name="metadata2")
    public Object[][] metadata2() {
        return new Object[][] {
            {"WIKI", "AAPL"},
            {"FRED", "GBP3MTD156N"},
            {"CBOE", "VIX"}
        };
    }



    @DataProvider(name="libor")
    public Object[][] fredLibor() {
        return new Object[][] {
            {"CAD3MTD156N", 1},
            {"AUD3MTD156N", 1},
            {"GBP3MTD156N", 1}
        };
    }

    @Test()
    public void testDatabaseListing() {
        final DataFrame<Integer, QuandlField> frame = source.getDatabases();
        frame.out().print();
        Assert.assertTrue(frame.rowCount() > 0);
        Assert.assertEquals(frame.colCount(), 9);
        Assert.assertTrue(frame.cols().containsAll(Arrays.asList(NAME, DESCRIPTION, DATABASE_CODE, DATASET_COUNT, DOWNLOADS, PREMIUM, IMAGE_URL)));
        Assert.assertEquals(frame.cols().type(NAME), String.class);
        Assert.assertEquals(frame.cols().type(DESCRIPTION), String.class);
        Assert.assertEquals(frame.cols().type(DATABASE_CODE), String.class);
        Assert.assertEquals(frame.cols().type(DATASET_COUNT), Long.class);
        Assert.assertEquals(frame.cols().type(DOWNLOADS), Long.class);
        Assert.assertEquals(frame.cols().type(PREMIUM), Boolean.class);
        Assert.assertEquals(frame.cols().type(IMAGE_URL), String.class);
    }


    @Test()
    public void testDatasetListing() {
        final String databaseCode = "WIKI";
        final DataFrame<String,QuandlField> frame = source.getDatasets(databaseCode);
        frame.out().print();
        Assert.assertTrue(frame.rowCount() > 0);
        Assert.assertEquals(frame.colCount(), 4);
        Assert.assertTrue(frame.cols().containsAll(Arrays.asList(NAME, LAST_REFRESH_TIME, START_DATE, END_DATE)));
        Assert.assertEquals(frame.cols().type(NAME), String.class);
        Assert.assertEquals(frame.cols().type(LAST_REFRESH_TIME), ZonedDateTime.class);
        Assert.assertEquals(frame.cols().type(START_DATE), LocalDate.class);
        Assert.assertEquals(frame.cols().type(END_DATE), LocalDate.class);
        Assert.assertTrue(frame.rows().contains("AAPL"), "Contains data for Apple");
        Assert.assertTrue(frame.rows().contains("ORCL"), "Contains data for Oracle");
    }


    @Test()
    public void testDatasetSearch() {
        final DataFrame<Integer,QuandlField> frame = source.search("crude oil");
        frame.out().print();
        Assert.assertTrue(frame.rowCount() > 0);
        Assert.assertEquals(frame.colCount(), 12);
        Assert.assertTrue(frame.cols().containsAll(Arrays.asList(DATABASE_CODE, DATASET_CODE, NAME, DESCRIPTION, LAST_REFRESH_TIME, START_DATE, END_DATE)));
        Assert.assertEquals(frame.cols().type(DATABASE_CODE), String.class);
        Assert.assertEquals(frame.cols().type(DATASET_CODE), String.class);
        Assert.assertEquals(frame.cols().type(NAME), String.class);
        Assert.assertEquals(frame.cols().type(DESCRIPTION), String.class);
        Assert.assertEquals(frame.cols().type(LAST_REFRESH_TIME), ZonedDateTime.class);
        Assert.assertEquals(frame.cols().type(START_DATE), LocalDate.class);
        Assert.assertEquals(frame.cols().type(END_DATE), LocalDate.class);
        Assert.assertTrue(frame.col(DATABASE_CODE).count(v -> v.isEqualTo("UENG")) > 0, "Contains data for BP");
        Assert.assertTrue(frame.col(DATASET_CODE).count(v -> v.isEqualTo("CR_BLR")) > 0, "Contains data for BP");
    }


    @Test(dataProvider = "metadata1")
    public void testDatabaseMetaData(String database) {
        final QuandlDatabaseInfo metaData = source.getMetaData(database);
        IO.println(metaData);
        Assert.assertNotNull(metaData, "Meta-data is not null");
        Assert.assertEquals(metaData.getCode(), database, "Database coded match");
        Assert.assertNotNull(metaData.getName());
        Assert.assertNotNull(metaData.getDescription());
        Assert.assertTrue(metaData.getDatasetCount() > 0);
        Assert.assertTrue(metaData.getId() > 0);
    }



    @Test(dataProvider = "metadata2")
    public void testDatasetMetaData(String database, String dataset) {
        final QuandlDatasetInfo metaData = source.getMetaData(database, dataset);
        IO.println(metaData);
        Assert.assertNotNull(metaData, "Meta-data is not null");
        Assert.assertEquals(metaData.getDatabaseCode(), database, "Database codes match");
        Assert.assertEquals(metaData.getDatasetCode(), dataset, "Dataset codes match");
        Assert.assertNotNull(metaData.getName());
        Assert.assertNotNull(metaData.getDescription());
        Assert.assertNotNull(metaData.getFrequency());
        Assert.assertNotNull(metaData.getType());
        Assert.assertNotNull(metaData.getColumnNames());
        Assert.assertNotNull(metaData.getNewestAvailableDate());
        Assert.assertNotNull(metaData.getOldestAvailableDate());
        Assert.assertNotNull(metaData.getRefreshedAt());
        Assert.assertTrue(metaData.getColumnNames().size() > 0);
        Assert.assertTrue(metaData.getId() > 0);
    }


    @Test(dataProvider = "wiki")
    public void testTimeSeriesQuery(String dataset, int expectedColCount) {
        final DataFrame<LocalDate,String> frame = source.getTimeSeries(options -> {
            options.setDatabase("WIKI");
            options.setDataset(dataset);
            options.setStartDate(LocalDate.of(2014, 1, 6));
            options.setEndDate(LocalDate.of(2014, 2, 4));
        });
        frame.out().print();
        Assert.assertTrue(frame.rowCount() > 0);
        Assert.assertEquals(frame.colCount(), expectedColCount);
        Assert.assertTrue(frame.cols().containsAll(Arrays.asList("Open", "High", "Low", "Close", "Volume", "Ex-Dividend", "Split Ratio")));
        Assert.assertEquals(frame.cols().type("Open"), Double.class);
        Assert.assertEquals(frame.cols().type("High"), Double.class);
        Assert.assertEquals(frame.cols().type("Low"), Double.class);
        Assert.assertEquals(frame.cols().type("Close"), Double.class);
        Assert.assertEquals(frame.cols().type("Volume"), Double.class);
        Assert.assertEquals(frame.cols().type("Ex-Dividend"), Double.class);
        Assert.assertEquals(frame.cols().type("Split Ratio"), Double.class);
        Assert.assertEquals(frame.rows().firstKey().get(), LocalDate.of(2014, 1, 6));
        Assert.assertEquals(frame.rows().lastKey().get(), LocalDate.of(2014, 2, 4));
        frame.out().print();
    }


    @Test()
    public void testDataTableQuery() {
        final DataFrame<Integer,String> frame = source.getDataTable(options -> {
            options.setDatabase("FXCM");
            options.setDataset("H1");
            options.setPageSize(10000);
        });
        frame.out().print();
        Assert.assertTrue(frame.rowCount() > 10000);
        Assert.assertEquals(frame.colCount(), 12);
        Assert.assertEquals(frame.cols().type("symbol"), String.class);
        Assert.assertEquals(frame.cols().type("date"), LocalDate.class);
        Assert.assertEquals(frame.cols().type("hour"), Integer.class);
        Assert.assertEquals(frame.cols().type("openbid"), Double.class);
        Assert.assertEquals(frame.cols().type("highbid"), Double.class);
        Assert.assertEquals(frame.cols().type("lowbid"), Double.class);
        Assert.assertEquals(frame.cols().type("closebid"), Double.class);
        Assert.assertEquals(frame.cols().type("openask"), Double.class);
        Assert.assertEquals(frame.cols().type("highask"), Double.class);
        Assert.assertEquals(frame.cols().type("lowask"), Double.class);
        Assert.assertEquals(frame.cols().type("closeask"), Double.class);
        Assert.assertEquals(frame.cols().type("totalticks"), Integer.class);
        Assert.assertEquals(frame.col("openbid").count(DataFrameValue::isNull), 0);
        Assert.assertTrue(frame.col("totalticks").stats().sum() > 0d);
    }


    @Test(enabled = false)
    public void testDataTableQueryColumnSelection() {
        final DataFrame<Integer,String> frame = source.getDataTable(options -> {
            options.setDatabase("FXCM");
            options.setDataset("H1");
            options.addColumns("symbol", "date", "hour", "closebid", "askbid", "totalticks");
        });
        frame.out().print();
        Assert.assertTrue(frame.rowCount() > 0);
        Assert.assertEquals(frame.colCount(), 6);
        Assert.assertEquals(frame.cols().type("symbol"), String.class);
        Assert.assertEquals(frame.cols().type("date"), LocalDate.class);
        Assert.assertEquals(frame.cols().type("hour"), Integer.class);
        Assert.assertEquals(frame.cols().type("closebid"), Double.class);
        Assert.assertEquals(frame.cols().type("totalticks"), Integer.class);
        Assert.assertEquals(frame.col("openbid").count(DataFrameValue::isNull), 0);
        Assert.assertTrue(frame.col("totalticks").stats().sum() > 0d);
    }



    @Test(dataProvider = "libor")
    public void testDailyFredLibor(String dataset, int expectedColCount) {
        final DataFrame<LocalDate,String> frame = source.getTimeSeries(options -> {
            options.setDatabase("FRED");
            options.setDataset(dataset);
            options.startDate("2000-01-04");
            options.endDate("2000-02-02");
        });
        frame.out().print();
        Assert.assertTrue(frame.rowCount() > 0);
        Assert.assertEquals(frame.colCount(), expectedColCount);
        Assert.assertTrue(frame.cols().containsAll(Collections.singleton(("VALUE"))));
        Assert.assertEquals(frame.cols().type("VALUE"), Double.class);
        Assert.assertTrue(frame.rows().firstKey().map(v -> v.equals(LocalDate.of(2000, 1, 4))).orElse(false));
        Assert.assertTrue(frame.rows().lastKey().map(v -> v.equals(LocalDate.of(2000, 2, 2))).orElse(false));
    }


    @Test()
    public void ukGDP() {
        final DataFrame<LocalDate,String> frame = source.getTimeSeries(options -> {
            options.setDatabase("UKONS");
            options.setDataset("BKVT_A");
            options.startDate("2000-01-04");
            options.endDate("2016-01-01");
        });
        frame.out().print();
        Assert.assertTrue(frame.rowCount() > 0);
        Assert.assertEquals(frame.colCount(), 1);
        Assert.assertTrue(frame.rows().firstKey().map(v -> v.equals(LocalDate.of(2000, 12, 31))).orElse(false));
        Assert.assertTrue(frame.rows().lastKey().map(v -> v.equals(LocalDate.of(2015, 12, 31))).orElse(false));
        frame.out().print();
    }


    @Test(expectedExceptions = { QuandlException.class})
    public void testMissingDatabase() {
        source.getTimeSeries(options -> {
            options.setDataset("BKVT_A");
            options.startDate("2000-01-04");
            options.endDate("2016-01-01");
        });
    }


    @Test(expectedExceptions = { QuandlException.class})
    public void testMissingDataset() {
        source.getTimeSeries(options -> {
            options.setDatabase("UKONS");
            options.startDate("2000-01-04");
            options.endDate("2016-01-01");
        });
    }
}
