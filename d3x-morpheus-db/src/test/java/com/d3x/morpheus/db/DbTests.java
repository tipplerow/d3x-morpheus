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
package com.d3x.morpheus.db;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;

import com.d3x.morpheus.util.IO;
import org.apache.commons.dbcp2.BasicDataSource;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.functions.Function1;

/**
 * A unit test for database access
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class DbTests {

    private static final File testDir = new File(System.getProperty("user.home"), "temp/morpheus/databases");

    private enum DbType { H2, HSQL, SQLITE }

    private Map<String,DataSource> dataSourceMap = new LinkedHashMap<>();


    /**
     * Returns a newly created DataSource based on Apache Commons DBCP
     * @param dbType        the database type
     * @param path          the path to the database file
     * @return              the newly created data source
     */
    private static DataSource createDataSource(DbType dbType, File path) {
        System.out.println("Creating DataSource for " + path.getAbsolutePath());
        path.getParentFile().mkdirs();
        var dataSource = new BasicDataSource();
        dataSource.setDefaultAutoCommit(true);
        switch (dbType) {
            case H2:
                var h2Url = "jdbc:h2://" + path.getAbsolutePath();
                IO.println(h2Url);
                dataSource.setDriverClassName("org.h2.Driver");
                dataSource.setUrl(h2Url);
                dataSource.setUsername("sa");
                return dataSource;
            case HSQL:
                var hsqlUrl = "jdbc:hsqldb:" + path.getAbsolutePath();
                IO.println(hsqlUrl);
                dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
                dataSource.setUrl(hsqlUrl);
                dataSource.setUsername("sa");
                return dataSource;
            case SQLITE:
                var sqliteUrl = "jdbc:sqlite:" + path.getAbsolutePath();
                IO.println(sqliteUrl);
                dataSource.setDriverClassName("org.sqlite.JDBC");
                dataSource.setUrl(sqliteUrl);
                dataSource.setUsername("");
                dataSource.setPassword("");
                return dataSource;

        }
        return dataSource;
    }



    @DataProvider(name = "databases")
    public Object[][] databases() {
        return new Object[][] {
            { "h2-db" },
            { "hsql-db" },
            { "sqlite-db" }
        };
    }


    @AfterClass
    public void dispose() {
        for (String key : dataSourceMap.keySet()) {
            try {
                final DataSource source = dataSourceMap.get(key);
                try (final Connection conn = source.getConnection()) {
                    final Statement stmt = conn.createStatement();
                    stmt.execute("shutdown");
                }
            } catch (Throwable t) {
                System.out.println("Failed to dispose connection for " + key);
            }
        }
    }


    /**
     * Recursively deletes files given a directory
     * @param file      the file or directory to delete
     */
    private static void delete(File file) {
        try {
            if (file.isDirectory()) {
                final File[] children = file.listFiles();
                if (children != null) {
                    for (File child : children) {
                        delete(child);
                    }
                }
            }
            System.out.println("Deleting " + file.getAbsolutePath());
            if (file.exists() && !file.delete()) {
                System.err.println("WARN: Failed to delete file: " + file.getAbsolutePath());
            }
        } catch (Exception ex) {
            System.err.println("Failed to delete file: " + file.getAbsolutePath());
        }
    }


    @BeforeClass
    public void setup() {
        delete(testDir);
        dataSourceMap.put("h2-db", createDataSource(DbType.H2, new File(testDir, "h2-db/testDb")));
        dataSourceMap.put("hsql-db", createDataSource(DbType.HSQL, new File(testDir, "hsql-db/testDb")));
        dataSourceMap.put("sqlite-db", createDataSource(DbType.SQLITE, new File(testDir, "sqlite-db/testDb")));
    }



    @Test(dataProvider="databases", dependsOnMethods="testWriteProcessLog")
    public void testProcessLog1(String dbName) {
        var source = new DbSource(dataSourceMap.get(dbName));
        var frame = source.read(options -> {
            options.setSql("select * from ProcessLog");
            options.setColKeyMapper(v -> {
                switch (v.toLowerCase()) {
                    case "node":                    return "Node";
                    case "executablepath":          return "ExecutablePath";
                    case "terminationdate":         return "TerminationDate";
                    case "minimumworkingsetsize":   return "MinimumWorkingSetSize";
                    default:                        return v;
                }
            });
        });
        Assert.assertEquals(frame.colCount(), 46);
        Assert.assertTrue(frame.cols().containsAll(Arrays.asList("Node", "ExecutablePath", "TerminationDate", "MinimumWorkingSetSize")));
        Assert.assertEquals(frame.rowCount(), 43);
        Assert.assertTrue(frame.rows().containsAll(Arrays.asList(1, 2, 3)));
        Assert.assertTrue(frame.col("ExecutablePath").toValueStream().anyMatch("C:\\Windows\\system32\\taskhost.exe"::equals));
        Assert.assertEquals(frame.col("TerminationDate").typeInfo(), LocalDate.class);
        Assert.assertEquals(frame.col("MinimumWorkingSetSize").typeInfo(), Double.class);
        frame.out().print();
    }


    @Test(dataProvider="databases", dependsOnMethods="testEtfWrite")
    public void testEtfRead(String dbName) {
        var source = new DbSource(dataSourceMap.get(dbName));
        final DataFrame<String,String> frame = source.read(options -> {
            options.setSql("select Ticker, Fund_Name, Issuer, AUM, P_E from ETF");
            options.setColKeyMapper(v -> {
                switch (v.toLowerCase()) {
                    case "ticker":      return "Ticker";
                    case "fund_name":   return "Fund Name";
                    case "issuer":      return "Issuer";
                    case "aum":         return "AUM";
                    case "p_e":         return "P/E";
                    default:            return v;
                }
            });
        });
        Assert.assertEquals(frame.colCount(), 5);
        Assert.assertTrue(frame.cols().containsAll(Arrays.asList("Ticker", "Fund Name", "Issuer", "AUM", "P/E")));
        Assert.assertEquals(frame.rowCount(), 1685);
        Assert.assertTrue(frame.rows().containsAll(Arrays.asList("SPY", "QQQ", "IWD")));
        Assert.assertTrue(frame.col("Issuer").toValueStream().anyMatch("BlackRock"::equals));
        Assert.assertEquals(frame.col("Issuer").typeInfo(), String.class);
        Assert.assertEquals(frame.col("P/E").typeInfo(), Double.class);
        frame.out().print();
    }



    @Test(dataProvider = "databases")
    public void testEtfWrite(String dbName) {
        var source = dataSourceMap.get(dbName);
        var frame = DataFrame.read("/csv/etf.csv").csv(String.class, options -> {
            options.setRowKeyColumnName("Ticker");
            options.setColumnType("Geography", String.class);
        });

        var sink = new DbSink(source);
        frame.rows().select(row -> row.key().equalsIgnoreCase("TDV")).out().print();
        sink.write(frame, options -> {
            options.setTableName("ETF");
            options.setRowKeyMapping("Ticker", String.class, Function1.toValue(v -> v));
            options.setBatchSize(1000);
        });
        frame.out().print();
    }


    @Test(dataProvider = "databases")
    public void testWriteProcessLog(String dbName) {
        var path = "/csv/process.csv";
        var frame = DataFrame.read(path).csv(Integer.class, options -> {
            options.setRowKeyColumnName("ProcessId");
            options.setCharset(StandardCharsets.UTF_16);
            options.setColumnType("ExecutionState", String.class);
            options.setColumnType("InstallDate", LocalDate.class);
            options.setColumnType("Status", String.class);
            options.setColumnType("TerminationDate", LocalDate.class);
            options.setColumnType("KernelModeTime", Long.class);
        });

        frame.out().print();
        frame.cols().forEach(col -> {
            IO.println(col.key() + ", type: " + col.typeInfo());
        });

        var sink = new DbSink(dataSourceMap.get(dbName));
        sink.write(frame, options -> {
            options.setBatchSize(1000);
            options.setAutoIncrementColumnName("RecordId");
            options.setTableName("ProcessLog");
        });
    }


    @Test(dataProvider = "databases")
    public void testWriteFollowedByRead(String dbName) {
        var frame1 = createRandomFrame(10);
        var sink = new DbSink(dataSourceMap.get(dbName));
        sink.write(frame1, options -> {
            options.setBatchSize(1000);
            options.setTableName("RandomTable");
            options.setAutoIncrementColumnName("RecordId");
        });

        var counter = new AtomicInteger();
        var source = new DbSource(dataSourceMap.get(dbName));
        final DataFrame<Integer,String> frame2 = source.read(options -> {
            options.setSql("select * from RandomTable");
            options.setExcludeColumnSet(Set.of("RecordId"));
            options.setRowKeyMapper(rs -> counter.incrementAndGet());
        });

        frame1.out().print();
        frame2.out().print();
    }


    private DataFrame<Integer,String> createRandomFrame(int rowCount) {
        final Range<Integer> rowKeys = Range.of(0, rowCount);
        return DataFrame.of(rowKeys, String.class, columns -> {
            columns.add("Column-1", rowKeys.map(i -> i + 5));
            columns.add("Column-2", rowKeys.map(i -> LocalDate.now().plusDays(i)));
            columns.add("Column-3", rowKeys.map(i -> LocalDateTime.now().plusMinutes(i)));
            columns.add("Column-4", rowKeys.map(i -> "Value-" + i));
            columns.add("Column-5", rowKeys.map(i -> Math.random()));
            columns.add("Column-6", rowKeys.map(i -> Math.random() > 0.5));
            columns.add("Column-7", rowKeys.map(i -> new java.sql.Date(Instant.now().plusSeconds(i).toEpochMilli())));
            columns.add("Column-8", rowKeys.map(i -> new java.util.Date(Instant.now().plusSeconds(i).toEpochMilli())));
            columns.add("Column-9", rowKeys.map(i -> i * 10L));
        });
    }
}
