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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.range.Range;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests the various methods on the DataFrameAccess interface
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class AccessTests {


    @DataProvider(name="SparseOrDense")
    public Object[][] getSparseOrDense() {
        return new Object[][] { {false}, {true} };
    }


    @DataProvider(name="exceptions")
    public Object[][] getExceptions() {
        return new Object[][] {
            { 0 }, { 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 }, { 7 },
        };
    }

    @DataProvider(name="args2")
    public Object[][] getArgs2() {
        return new Object[][] {
            { 0 }, { 1 }, { 2 },
        };
    }

    @DataProvider(name="args3")
    public Object[][] getArgs3() {
        return new Object[][] {
            { 0 }, { 1 }
        };
    }

    @DataProvider(name="args4")
    public Object[][] getArgs4() {
        return new Object[][] {
            { boolean.class },
            { int.class },
            { long.class },
            { double.class },
            { Object.class },
        };
    }




    @Test()
    public void testBooleanReads() {
        final Random random = new Random(2344);
        final Map<Coordinate,Boolean> valueMap = new HashMap<>();
        final DataFrame<String,String> frame = TestDataFrames.random(boolean.class, 100, 100);
        frame.applyBooleans(v -> random.nextBoolean());
        frame.forEachValue(v -> valueMap.put(new Coordinate(v.rowOrdinal(), v.colOrdinal()), v.getBoolean()));
        for (int i=0; i<frame.rowCount(); ++i) {
            final String rowKey = frame.rows().key(i);
            for (int j = 0; j<frame.colCount(); ++j) {
                final String colKey = frame.cols().key(j);
                final boolean v1 =  frame.at(i,j).getBoolean();
                final boolean v2 =  frame.cursor().row(rowKey).colAt(j).getBoolean();
                final boolean v3 =  frame.cursor().rowAt(i).col(colKey).getBoolean();
                final boolean v4 =  frame.get(rowKey, colKey).getBoolean();
                final boolean v5 =  frame.row(rowKey).getBooleanAt(j);
                final boolean v6 =  frame.col(colKey).getBooleanAt(i);
                final boolean v7 =  frame.row(rowKey).getBoolean(colKey);
                final boolean v8 =  frame.col(colKey).getBoolean(rowKey);
                final boolean v9 =  frame.getBoolean(rowKey, colKey);
                final boolean v10 = frame.getBooleanAt(i, j);
                final boolean v11 = frame.rows().getBoolean(rowKey, j);
                final boolean v12 = frame.rows().getBooleanAt(i, colKey);
                final boolean v13 = frame.cols().getBoolean(colKey, i);
                final boolean v14 = frame.cols().getBooleanAt(j, rowKey);
                final Coordinate coordinate = new Coordinate(i,j);
                Assert.assertEquals(v1, v2, "V1 matches v2");
                Assert.assertEquals(v1, v3, "V1 matches v3");
                Assert.assertEquals(v1, v4, "V1 matches v4");
                Assert.assertEquals(v1, v5, "V1 matches v5");
                Assert.assertEquals(v1, v6, "V1 matches v6");
                Assert.assertEquals(v1, v7, "V1 matches v7");
                Assert.assertEquals(v1, v8, "V1 matches v8");
                Assert.assertEquals(v1, v9, "V1 matches v9");
                Assert.assertEquals(v1, v10, "V1 matches v10");
                Assert.assertEquals(v1, v11, "V1 matches v11");
                Assert.assertEquals(v1, v12, "V1 matches v12");
                Assert.assertEquals(v1, v13, "V1 matches v13");
                Assert.assertEquals(v1, v14, "V1 matches v14");
                Assert.assertEquals(v1, valueMap.get(coordinate).booleanValue(), "V1 matches apply value");
            }
        }
    }


    @Test()
    public void testIntReads() {
        final Random random = new Random(2344);
        final Map<Coordinate,Integer> valueMap = new HashMap<>();
        final DataFrame<String,String> frame = TestDataFrames.random(int.class, 100, 100);
        frame.applyInts(v -> random.nextInt() * 10);
        frame.forEachValue(v -> valueMap.put(new Coordinate(v.rowOrdinal(), v.colOrdinal()), v.getInt()));
        for (int i=0; i<frame.rowCount(); ++i) {
            final String rowKey = frame.rows().key(i);
            for (int j = 0; j<frame.colCount(); ++j) {
                final String colKey = frame.cols().key(j);
                final int v1 = frame.at(i,j).getInt();
                final int v2 = frame.cursor().row(rowKey).colAt(j).getInt();
                final int v3 = frame.cursor().rowAt(i).col(colKey).getInt();
                final int v4 = frame.get(rowKey, colKey).getInt();
                final int v5 = frame.row(rowKey).getIntAt(j);
                final int v6 = frame.col(colKey).getIntAt(i);
                final int v7 = frame.row(rowKey).getInt(colKey);
                final int v8 = frame.col(colKey).getInt(rowKey);
                final int v9 =  frame.getInt(rowKey, colKey);
                final int v10 = frame.getIntAt(i, j);
                final int v11 = frame.rows().getInt(rowKey, j);
                final int v12 = frame.rows().getIntAt(i, colKey);
                final int v13 = frame.cols().getInt(colKey, i);
                final int v14 = frame.cols().getIntAt(j, rowKey);
                final Coordinate coordinate = new Coordinate(i,j);
                Assert.assertEquals(v1, v2, "V1 matches v2");
                Assert.assertEquals(v1, v3, "V1 matches v3");
                Assert.assertEquals(v1, v4, "V1 matches v4");
                Assert.assertEquals(v1, v5, "V1 matches v5");
                Assert.assertEquals(v1, v6, "V1 matches v6");
                Assert.assertEquals(v1, v7, "V1 matches v7");
                Assert.assertEquals(v1, v8, "V1 matches v8");
                Assert.assertEquals(v1, v9, "V1 matches v9");
                Assert.assertEquals(v1, v10, "V1 matches v10");
                Assert.assertEquals(v1, v11, "V1 matches v11");
                Assert.assertEquals(v1, v12, "V1 matches v12");
                Assert.assertEquals(v1, v13, "V1 matches v13");
                Assert.assertEquals(v1, v14, "V1 matches v14");
                Assert.assertEquals(v1, valueMap.get(coordinate).intValue(), "V1 matches apply value");
            }
        }
    }


    @Test()
    public void testLongReads() {
        final Random random = new Random(2344);
        final Map<Coordinate,Long> valueMap = new HashMap<>();
        final DataFrame<String,String> frame = TestDataFrames.random(long.class, 100, 100);
        frame.applyLongs(v -> random.nextLong() * 10);
        frame.forEachValue(v -> valueMap.put(new Coordinate(v.rowOrdinal(), v.colOrdinal()), v.getLong()));
        for (int i=0; i<frame.rowCount(); ++i) {
            final String rowKey = frame.rows().key(i);
            for (int j = 0; j<frame.colCount(); ++j) {
                final String colKey = frame.cols().key(j);
                final long v1 = frame.at(i,j).getLong();
                final long v2 = frame.cursor().row(rowKey).colAt(j).getLong();
                final long v3 = frame.cursor().rowAt(i).col(colKey).getLong();
                final long v4 = frame.get(rowKey, colKey).getLong();
                final long v5 = frame.row(rowKey).getLongAt(j);
                final long v6 = frame.col(colKey).getLongAt(i);
                final long v7 = frame.row(rowKey).getLong(colKey);
                final long v8 = frame.col(colKey).getLong(rowKey);
                final long v9 =  frame.getLong(rowKey, colKey);
                final long v10 = frame.getLongAt(i, j);
                final long v11 = frame.rows().getLong(rowKey, j);
                final long v12 = frame.rows().getLongAt(i, colKey);
                final long v13 = frame.cols().getLong(colKey, i);
                final long v14 = frame.cols().getLongAt(j, rowKey);
                final Coordinate coordinate = new Coordinate(i,j);
                Assert.assertEquals(v1, v2, "V1 matches v2");
                Assert.assertEquals(v1, v3, "V1 matches v3");
                Assert.assertEquals(v1, v4, "V1 matches v4");
                Assert.assertEquals(v1, v5, "V1 matches v5");
                Assert.assertEquals(v1, v6, "V1 matches v6");
                Assert.assertEquals(v1, v7, "V1 matches v7");
                Assert.assertEquals(v1, v8, "V1 matches v8");
                Assert.assertEquals(v1, v9, "V1 matches v9");
                Assert.assertEquals(v1, v10, "V1 matches v10");
                Assert.assertEquals(v1, v11, "V1 matches v11");
                Assert.assertEquals(v1, v12, "V1 matches v12");
                Assert.assertEquals(v1, v13, "V1 matches v13");
                Assert.assertEquals(v1, v14, "V1 matches v14");
                Assert.assertEquals(v1, valueMap.get(coordinate).longValue(), "V1 matches apply value");
            }
        }
    }


    @Test()
    public void testDoubleReads() {
        final Random random = new Random(2344);
        final Map<Coordinate,Double> valueMap = new HashMap<>();
        final DataFrame<String,String> frame = TestDataFrames.random(double.class, 100, 100);
        frame.applyDoubles(v -> random.nextDouble() * 10);
        frame.forEachValue(v -> valueMap.put(new Coordinate(v.rowOrdinal(), v.colOrdinal()), v.getDouble()));
        for (int i=0; i<frame.rowCount(); ++i) {
            final String rowKey = frame.rows().key(i);
            for (int j = 0; j<frame.colCount(); ++j) {
                final String colKey = frame.cols().key(j);
                final double v1 = frame.at(i,j).getDouble();
                final double v2 = frame.cursor().row(rowKey).colAt(j).getDouble();
                final double v3 = frame.cursor().rowAt(i).col(colKey).getDouble();
                final double v4 = frame.get(rowKey, colKey).getDouble();
                final double v5 = frame.row(rowKey).getDoubleAt(j);
                final double v6 = frame.col(colKey).getDoubleAt(i);
                final double v7 = frame.row(rowKey).getDouble(colKey);
                final double v8 = frame.col(colKey).getDouble(rowKey);
                final double v9 =  frame.getDouble(rowKey, colKey);
                final double v10 = frame.getDoubleAt(i, j);
                final double v11 = frame.rows().getDouble(rowKey, j);
                final double v12 = frame.rows().getDoubleAt(i, colKey);
                final double v13 = frame.cols().getDouble(colKey, i);
                final double v14 = frame.cols().getDoubleAt(j, rowKey);
                final Coordinate coordinate = new Coordinate(i,j);
                Assert.assertEquals(v1, v2, "V1 matches v2");
                Assert.assertEquals(v1, v3, "V1 matches v3");
                Assert.assertEquals(v1, v4, "V1 matches v4");
                Assert.assertEquals(v1, v5, "V1 matches v5");
                Assert.assertEquals(v1, v6, "V1 matches v6");
                Assert.assertEquals(v1, v7, "V1 matches v7");
                Assert.assertEquals(v1, v8, "V1 matches v8");
                Assert.assertEquals(v1, v9, "V1 matches v9");
                Assert.assertEquals(v1, v10, "V1 matches v10");
                Assert.assertEquals(v1, v11, "V1 matches v11");
                Assert.assertEquals(v1, v12, "V1 matches v12");
                Assert.assertEquals(v1, v13, "V1 matches v13");
                Assert.assertEquals(v1, v14, "V1 matches v14");
                Assert.assertEquals(v1, valueMap.get(coordinate), "V1 matches apply value");
            }
        }
    }


    @Test()
    public void testValueReads() {
        final Random random = new Random(2344);
        final Map<Coordinate,String> valueMap = new HashMap<>();
        final DataFrame<String,String> frame = TestDataFrames.random(Object.class, 100, 100);
        frame.applyValues(v -> "x:" + (random.nextDouble() * 10));
        frame.forEachValue(v -> valueMap.put(new Coordinate(v.rowOrdinal(), v.colOrdinal()), v.getValue()));
        for (int i=0; i<frame.rowCount(); ++i) {
            final String rowKey = frame.rows().key(i);
            for (int j = 0; j<frame.colCount(); ++j) {
                final String colKey = frame.cols().key(j);
                final String v1 = frame.at(i,j).getValue();
                final String v2 = frame.cursor().row(rowKey).colAt(j).getValue();
                final String v3 = frame.cursor().rowAt(i).col(colKey).getValue();
                final String v4 = frame.get(rowKey, colKey).getValue();
                final String v5 = frame.row(rowKey).getValueAt(j);
                final String v6 = frame.col(colKey).getValueAt(i);
                final String v7 = frame.row(rowKey).getValue(colKey);
                final String v8 = frame.col(colKey).getValue(rowKey);
                final String v9 =  frame.getValue(rowKey, colKey);
                final String v10 = frame.getValueAt(i, j);
                final String v11 = frame.rows().getValue(rowKey, j);
                final String v12 = frame.rows().getValueAt(i, colKey);
                final String v13 = frame.cols().getValue(colKey, i);
                final String v14 = frame.cols().getValueAt(j, rowKey);
                final Coordinate coordinate = new Coordinate(i,j);
                Assert.assertEquals(v1, v2, "V1 matches v2");
                Assert.assertEquals(v1, v3, "V1 matches v3");
                Assert.assertEquals(v1, v4, "V1 matches v4");
                Assert.assertEquals(v1, v5, "V1 matches v5");
                Assert.assertEquals(v1, v6, "V1 matches v6");
                Assert.assertEquals(v1, v7, "V1 matches v7");
                Assert.assertEquals(v1, v8, "V1 matches v8");
                Assert.assertEquals(v1, v9, "V1 matches v9");
                Assert.assertEquals(v1, v10, "V1 matches v10");
                Assert.assertEquals(v1, v11, "V1 matches v11");
                Assert.assertEquals(v1, v12, "V1 matches v12");
                Assert.assertEquals(v1, v13, "V1 matches v13");
                Assert.assertEquals(v1, v14, "V1 matches v14");
                Assert.assertEquals(v1, valueMap.get(coordinate), "V1 matches apply value");
            }
        }
    }


    @Test()
    public void testBooleanWrites() {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame1 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame2 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame3 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame4 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame5 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame6 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame7 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame8 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame9 = DataFrame.ofBooleans(rows, columns);
        final DataFrame<String,String> frame10 = DataFrame.ofBooleans(rows, columns);
        source.applyBooleans(v -> random.nextBoolean());
        source.forEachValue(v -> {
            frame1.at(v.rowOrdinal(), v.colOrdinal()).setBoolean(source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame2.row(v.rowKey()).setBooleanAt(v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame3.rowAt(v.rowOrdinal()).setBoolean(v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame4.get(v.rowKey(), v.colKey()).setBoolean(source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame5.setBoolean(v.rowKey(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame6.setBooleanAt(v.rowOrdinal(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame7.rows().setBoolean(v.rowKey(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame8.rows().setBooleanAt(v.rowOrdinal(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame9.cols().setBoolean(v.colKey(), v.rowOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
            frame10.cols().setBooleanAt(v.colOrdinal(), v.rowKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getBoolean());
        });
        DataFrameAsserts.assertEqualsByIndex(source, frame1);
        DataFrameAsserts.assertEqualsByIndex(source, frame2);
        DataFrameAsserts.assertEqualsByIndex(source, frame3);
        DataFrameAsserts.assertEqualsByIndex(source, frame4);
    }


    @Test()
    public void testIntWrites() {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame1 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame2 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame3 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame4 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame5 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame6 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame7 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame8 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame9 = DataFrame.ofInts(rows, columns);
        final DataFrame<String,String> frame10 = DataFrame.ofInts(rows, columns);
        source.applyInts(v -> random.nextInt() * 10);
        source.forEachValue(v -> {
            frame1.at(v.rowOrdinal(), v.colOrdinal()).setInt(source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame2.row(v.rowKey()).setIntAt(v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame3.rowAt(v.rowOrdinal()).setInt(v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame4.get(v.rowKey(), v.colKey()).setInt(source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame5.setInt(v.rowKey(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame6.setIntAt(v.rowOrdinal(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame7.rows().setInt(v.rowKey(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame8.rows().setIntAt(v.rowOrdinal(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame9.cols().setInt(v.colKey(), v.rowOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
            frame10.cols().setIntAt(v.colOrdinal(), v.rowKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getInt());
        });
        DataFrameAsserts.assertEqualsByIndex(source, frame1);
        DataFrameAsserts.assertEqualsByIndex(source, frame2);
        DataFrameAsserts.assertEqualsByIndex(source, frame3);
        DataFrameAsserts.assertEqualsByIndex(source, frame4);
    }


    @Test()
    public void testLongWrites() {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame1 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame2 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame3 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame4 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame5 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame6 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame7 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame8 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame9 = DataFrame.ofLongs(rows, columns);
        final DataFrame<String,String> frame10 = DataFrame.ofLongs(rows, columns);
        source.applyLongs(v -> random.nextLong() * 10);
        source.forEachValue(v -> {
            frame1.at(v.rowOrdinal(), v.colOrdinal()).setLong(source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame2.row(v.rowKey()).setLongAt(v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame3.rowAt(v.rowOrdinal()).setLong(v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame4.get(v.rowKey(), v.colKey()).setLong(source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame5.setLong(v.rowKey(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame6.setLongAt(v.rowOrdinal(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame7.rows().setLong(v.rowKey(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame8.rows().setLongAt(v.rowOrdinal(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame9.cols().setLong(v.colKey(), v.rowOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
            frame10.cols().setLongAt(v.colOrdinal(), v.rowKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getLong());
        });
        DataFrameAsserts.assertEqualsByIndex(source, frame1);
        DataFrameAsserts.assertEqualsByIndex(source, frame2);
        DataFrameAsserts.assertEqualsByIndex(source, frame3);
        DataFrameAsserts.assertEqualsByIndex(source, frame4);
    }


    @Test()
    public void testDoubleWrites() {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame1 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame2 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame3 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame4 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame5 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame6 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame7 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame8 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame9 = DataFrame.ofDoubles(rows, columns);
        final DataFrame<String,String> frame10 = DataFrame.ofDoubles(rows, columns);
        source.applyDoubles(v -> random.nextDouble() * 10d);
        source.forEachValue(v -> {
            frame1.at(v.rowOrdinal(), v.colOrdinal()).setDouble(source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame2.row(v.rowKey()).setDoubleAt(v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame3.rowAt(v.rowOrdinal()).setDouble(v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame4.get(v.rowKey(), v.colKey()).setDouble(source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame5.setDouble(v.rowKey(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame6.setDoubleAt(v.rowOrdinal(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame7.rows().setDouble(v.rowKey(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame8.rows().setDoubleAt(v.rowOrdinal(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame9.cols().setDouble(v.colKey(), v.rowOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
            frame10.cols().setDoubleAt(v.colOrdinal(), v.rowKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getDouble());
        });
        DataFrameAsserts.assertEqualsByIndex(source, frame1);
        DataFrameAsserts.assertEqualsByIndex(source, frame2);
        DataFrameAsserts.assertEqualsByIndex(source, frame3);
        DataFrameAsserts.assertEqualsByIndex(source, frame4);
    }


    @Test()
    public void testStringWrites() {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame1 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame2 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame3 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame4 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame5 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame6 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame7 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame8 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame9 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame10 = DataFrame.ofStrings(rows, columns);
        source.applyValues(v -> "X: " + random.nextDouble() * 10d);
        source.forEachValue(v -> {
            frame1.at(v.rowOrdinal(), v.colOrdinal()).setValue(source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame2.row(v.rowKey()).setValueAt(v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame3.rowAt(v.rowOrdinal()).setValue(v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame4.get(v.rowKey(), v.colKey()).setValue(source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame5.setValue(v.rowKey(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame6.setValueAt(v.rowOrdinal(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame7.rows().setValue(v.rowKey(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame8.rows().setValueAt(v.rowOrdinal(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame9.cols().setValue(v.colKey(), v.rowOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame10.cols().setValueAt(v.colOrdinal(), v.rowKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
        });
        DataFrameAsserts.assertEqualsByIndex(source, frame1);
        DataFrameAsserts.assertEqualsByIndex(source, frame2);
        DataFrameAsserts.assertEqualsByIndex(source, frame3);
        DataFrameAsserts.assertEqualsByIndex(source, frame4);
    }


    @Test()
    public void testObjectWrites() {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame1 = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame2 = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame3 = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame4 = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame5 = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame6 = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame7 = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame8 = DataFrame.ofObjects(rows, columns);
        final DataFrame<String,String> frame9 = DataFrame.ofStrings(rows, columns);
        final DataFrame<String,String> frame10 = DataFrame.ofStrings(rows, columns);
        source.applyValues(v -> "X: " + random.nextDouble() * 10d);
        source.forEachValue(v -> {
            frame1.at(v.rowOrdinal(), v.colOrdinal()).setValue(source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame2.row(v.rowKey()).setValueAt(v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame3.rowAt(v.rowOrdinal()).setValue(v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame4.get(v.rowKey(), v.colKey()).setValue(source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame5.setValue(v.rowKey(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame6.setValueAt(v.rowOrdinal(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame7.rows().setValue(v.rowKey(), v.colOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame8.rows().setValueAt(v.rowOrdinal(), v.colKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame9.cols().setValue(v.colKey(), v.rowOrdinal(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
            frame10.cols().setValueAt(v.colOrdinal(), v.rowKey(), source.at(v.rowOrdinal(), v.colOrdinal()).getValue());
        });
        DataFrameAsserts.assertEqualsByIndex(source, frame1);
        DataFrameAsserts.assertEqualsByIndex(source, frame2);
        DataFrameAsserts.assertEqualsByIndex(source, frame3);
        DataFrameAsserts.assertEqualsByIndex(source, frame4);
    }



    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnBooleanRead(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofBooleans(rows, columns);
        source.applyBooleans(v -> random.nextBoolean());
        switch (scenario) {
            case 0: source.at(1000, 50).getBoolean();       break;
            case 1: source.at(50, 1000).getBoolean();       break;
            case 2: source.row("X").getBooleanAt(50);         break;
            case 3: source.row("R10").getBooleanAt(500);      break;
            case 4: source.rowAt(50).getBoolean("Y");       break;
            case 5: source.rowAt(500).getBoolean("C10");    break;
            case 6: source.get("R10", "Y").getBoolean();    break;
            case 7: source.get("X", "C10").getBoolean();    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnIntReads(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofInts(rows, columns);
        source.applyInts(v -> random.nextInt());
        switch (scenario) {
            case 0: source.at(1000, 50).getInt();       break;
            case 1: source.at(50, 1000).getInt();       break;
            case 2: source.row("X").getIntAt(50);         break;
            case 3: source.row("R10").getIntAt(500);      break;
            case 4: source.rowAt(50).getInt("Y");       break;
            case 5: source.rowAt(500).getInt("C10");    break;
            case 6: source.get("R10", "Y").getInt();    break;
            case 7: source.get("X", "C10").getInt();    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnLongReads(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofLongs(rows, columns);
        source.applyLongs(v -> random.nextLong());
        switch (scenario) {
            case 0: source.at(1000, 50).getLong();       break;
            case 1: source.at(50, 1000).getLong();       break;
            case 2: source.row("X").getLongAt(50);         break;
            case 3: source.row("R10").getLongAt(500);      break;
            case 4: source.rowAt(50).getLong("Y");       break;
            case 5: source.rowAt(500).getLong("C10");    break;
            case 6: source.get("R10", "Y").getLong();    break;
            case 7: source.get("X", "C10").getLong();    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnDoubleReads(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofDoubles(rows, columns);
        source.applyDoubles(v -> random.nextDouble());
        switch (scenario) {
            case 0: source.at(1000, 50).getDouble();       break;
            case 1: source.at(50, 1000).getDouble();       break;
            case 2: source.row("X").getDoubleAt(50);         break;
            case 3: source.row("R10").getDoubleAt(500);      break;
            case 4: source.rowAt(50).getDouble("Y");       break;
            case 5: source.rowAt(500).getDouble("C10");    break;
            case 6: source.get("R10", "Y").getDouble();    break;
            case 7: source.get("X", "C10").getDouble();    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnValueReads(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofStrings(rows, columns);
        source.applyValues(v -> "X:" + random.nextDouble());
        switch (scenario) {
            case 0: source.at(1000, 50).getValue();       break;
            case 1: source.at(50, 1000).getValue();       break;
            case 2: source.row("X").getValueAt(50);         break;
            case 3: source.row("R10").getValueAt(500);      break;
            case 4: source.rowAt(50).getValue("Y");       break;
            case 5: source.rowAt(500).getValue("C10");    break;
            case 6: source.get("R10", "Y").getValue();    break;
            case 7: source.get("X", "C10").getValue();    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnBooleanWrite(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofBooleans(rows, columns);
        source.applyBooleans(v -> random.nextBoolean());
        switch (scenario) {
            case 0: source.at(1000, 50).setBoolean(random.nextBoolean());       break;
            case 1: source.at(50, 1000).setBoolean(random.nextBoolean());       break;
            case 2: source.row("X").setBooleanAt(50, random.nextBoolean());       break;
            case 3: source.row("R10").setBooleanAt(500, random.nextBoolean());    break;
            case 4: source.rowAt(50).setBoolean("Y", random.nextBoolean());     break;
            case 5: source.rowAt(500).setBoolean("C10", random.nextBoolean());  break;
            case 6: source.get("R10", "Y").setBoolean(random.nextBoolean());    break;
            case 7: source.get("X", "C10").setBoolean(random.nextBoolean());    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnIntWrite(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofInts(rows, columns);
        source.applyInts(v -> random.nextInt());
        switch (scenario) {
            case 0: source.at(1000, 50).setInt(random.nextInt());       break;
            case 1: source.at(50, 1000).setInt(random.nextInt());       break;
            case 2: source.row("X").setIntAt(50, random.nextInt());       break;
            case 3: source.row("R10").setIntAt(500, random.nextInt());    break;
            case 4: source.rowAt(50).setInt("Y", random.nextInt());     break;
            case 5: source.rowAt(500).setInt("C10", random.nextInt());  break;
            case 6: source.get("R10", "Y").setInt(random.nextInt());    break;
            case 7: source.get("X", "C10").setInt(random.nextInt());    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnLongWrite(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofLongs(rows, columns);
        source.applyLongs(v -> random.nextLong());
        switch (scenario) {
            case 0: source.at(1000, 50).setLong(random.nextLong());       break;
            case 1: source.at(50, 1000).setLong(random.nextLong());       break;
            case 2: source.row("X").setLongAt(50, random.nextLong());       break;
            case 3: source.row("R10").setLongAt(500, random.nextLong());    break;
            case 4: source.rowAt(50).setLong("Y", random.nextLong());     break;
            case 5: source.rowAt(500).setLong("C10", random.nextLong());  break;
            case 6: source.get("R10", "Y").setLong(random.nextLong());    break;
            case 7: source.get("X", "C10").setLong(random.nextLong());    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnDoubleWrite(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofDoubles(rows, columns);
        source.applyDoubles(v -> random.nextDouble());
        switch (scenario) {
            case 0: source.at(1000, 50).setDouble(random.nextDouble());       break;
            case 1: source.at(50, 1000).setDouble(random.nextDouble());       break;
            case 2: source.row("X").setDoubleAt(50, random.nextDouble());       break;
            case 3: source.row("R10").setDoubleAt(500, random.nextDouble());    break;
            case 4: source.rowAt(50).setDouble("Y", random.nextDouble());     break;
            case 5: source.rowAt(500).setDouble("C10", random.nextDouble());  break;
            case 6: source.get("R10", "Y").setDouble(random.nextDouble());    break;
            case 7: source.get("X", "C10").setDouble(random.nextDouble());    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }


    @Test(dataProvider="exceptions", expectedExceptions={DataFrameException.class})
    public void testExceptionOnValueWrite(int scenario) {
        final Random random = new Random(2344);
        final Range<String> rows = Range.of(0, 100).map(i -> "R" + i);
        final Range<String> columns = Range.of(0, 100).map(i -> "C" + i);
        final DataFrame<String,String> source = DataFrame.ofStrings(rows, columns);
        source.applyValues(v -> "X:" + random.nextDouble());
        switch (scenario) {
            case 0: source.at(1000, 50).setValue("?");       break;
            case 1: source.at(50, 1000).setValue("?");       break;
            case 2: source.row("X").setValueAt(50, "?");       break;
            case 3: source.row("R10").setValueAt(500, "?");    break;
            case 4: source.rowAt(50).setValue("Y", "?");     break;
            case 5: source.rowAt(500).setValue("C10", "?");  break;
            case 6: source.get("R10", "Y").setValue("?");    break;
            case 7: source.get("X", "C10").setValue("?");    break;
            default:    throw new IllegalArgumentException("Unsupported scenario code: " + scenario);
        }
    }

    @Test(dataProvider="args2", expectedExceptions={DataFrameException.class})
    public void testExceptionOnReadWrongTypeWhenBoolean(int scenario) {
        final DataFrame<String,String> frame = TestDataFrames.random(boolean.class, 10, 10);
        switch (scenario) {
            case 0: frame.at(0,0).getDouble();  break;
            case 1: frame.at(0,0).getInt();     break;
            case 2: frame.at(0,0).getLong();    break;
        }
    }

    @Test(dataProvider="args3", expectedExceptions={DataFrameException.class})
    public void testExceptionOnReadWrongTypeWhenInt(int scenario) {
        final DataFrame<String,String> frame = TestDataFrames.random(int.class, 10, 10);
        switch (scenario) {
            case 0: frame.at(0,0).getBoolean();     break;
            case 1: frame.at(0,0).getBoolean();     break;
        }
    }

    @Test(dataProvider="args3", expectedExceptions={DataFrameException.class})
    public void testExceptionOnReadWrongTypeWhenLong(int scenario) {
        final DataFrame<String,String> frame = TestDataFrames.random(long.class, 10, 10);
        switch (scenario) {
            case 0: frame.at(0,0).getBoolean();  break;
            case 1: frame.at(0,0).getInt();      break;
        }
    }

    @Test(dataProvider="args2", expectedExceptions={DataFrameException.class})
    public void testExceptionOnReadWrongTypeWhenDouble(int scenario) {
        final DataFrame<String,String> frame = TestDataFrames.random(double.class, 10, 10);
        switch (scenario) {
            case 0: frame.at(0,0).getBoolean(); break;
            case 1: frame.at(0,0).getBoolean(); break;
            case 2: frame.at(0,0).getBoolean(); break;
        }
    }

    @Test(dataProvider="args2", expectedExceptions={DataFrameException.class})
    public void testExceptionOnWriteWrongTypeWhenBoolean(int scenario) {
        final DataFrame<String,String> frame = TestDataFrames.random(boolean.class, 10, 10);
        switch (scenario) {
            case 0: frame.at(0,0).setDouble(Double.NaN);    break;
            case 1: frame.at(0,0).setInt(1);                break;
            case 2: frame.at(0,0).setLong(2);               break;
        }
    }

    @Test(dataProvider="args3", expectedExceptions={DataFrameException.class})
    public void testExceptionOnWriteWrongTypeWhenInt(int scenario) {
        final DataFrame<String,String> frame = TestDataFrames.random(int.class, 10, 10);
        switch (scenario) {
            case 0: frame.at(0,0).setBoolean(false);      break;
            case 1: frame.at(0,0).setLong(0L);            break;
        }
    }

    @Test(dataProvider="args3", expectedExceptions={DataFrameException.class})
    public void testExceptionOnWriteWrongTypeWhenLong(int scenario) {
        final DataFrame<String,String> frame = TestDataFrames.random(long.class, 10, 10);
        switch (scenario) {
            case 0: frame.at(0,0).setBoolean(true);     break;
            case 1: frame.at(0,0).setInt(8);            break;
        }
    }

    @Test(dataProvider="args2", expectedExceptions={DataFrameException.class})
    public void testExceptionOnWriteWrongTypeWhenDouble(int scenario) {
        final DataFrame<String,String> frame = TestDataFrames.random(double.class, 10, 10);
        switch (scenario) {
            case 0: frame.at(0,0).setBoolean(true); break;
            case 1: frame.at(0,0).setInt(9);        break;
            case 2: frame.at(0,0).setLong(8L);      break;
        }
    }

    @Test(dataProvider= "args4")
    public void testDataFrameValueRead(Class<?> type) {
        final DataFrame<String,String> source = TestDataFrames.random(type, 100, 100);
        final DataFrame<String,String> target = source.copy().applyValues(v -> null);
        if (type == boolean.class) {
            source.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                target.at(rowOrdinal, colOrdinal).setBoolean(v.getBoolean());
            });
        } else if (type == int.class) {
            source.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                target.at(rowOrdinal, colOrdinal).setInt(v.getInt());
            });
        } else if (type == long.class) {
            source.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                target.at(rowOrdinal, colOrdinal).setLong(v.getLong());
            });
        } else if (type == double.class) {
            source.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                target.at(rowOrdinal, colOrdinal).setDouble(v.getDouble());
            });
        } else {
            source.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                target.at(rowOrdinal, colOrdinal).setValue(v.getValue());
            });
        }
        DataFrameAsserts.assertEqualsByIndex(source, target);
    }


    @Test(dataProvider= "args4")
    public void testDataFrameValueWrite(Class<?> type) {
        final DataFrame<String,String> source = TestDataFrames.random(type, 100, 100);
        final DataFrame<String,String> target = source.copy().applyValues(v -> null);
        if (type == boolean.class) {
            target.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                final boolean value = source.at(rowOrdinal, colOrdinal).getBoolean();
                v.setBoolean(value);
            });
        } else if (type == int.class) {
            target.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                final int value = source.at(rowOrdinal, colOrdinal).getInt();
                v.setInt(value);
            });
        } else if (type == long.class) {
            target.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                final long value = source.at(rowOrdinal, colOrdinal).getLong();
                v.setLong(value);
            });
        } else if (type == double.class) {
            target.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                final double value = source.at(rowOrdinal, colOrdinal).getDouble();
                v.setDouble(value);
            });
        } else {
            target.forEachValue(v -> {
                final int rowOrdinal = v.rowOrdinal();
                final int colOrdinal = v.colOrdinal();
                final Object value = source.at(rowOrdinal, colOrdinal).getValue();
                v.setValue(value);
            });
        }
        DataFrameAsserts.assertEqualsByIndex(source, target);
    }


    @Test()
    public void missingKeys() {
        var frame = TestDataFrames.createMixedRandomFrame(String.class, 10);
        Assert.assertFalse(frame.getBoolean("X", "Y"));
        Assert.assertEquals(frame.getInt("X", "Y"), 0);
        Assert.assertEquals(frame.getLong("X", "Y"), 0L);
        Assert.assertEquals(frame.getDouble("X", "Y"), Double.NaN);
        Assert.assertNull(frame.getValue("X", "Y"));
    }



    static class Coordinate {

        private int rowIndex;
        private int colIndex;

        Coordinate(int rowIndex, int colIndex) {
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
        }

        @Override()
        public int hashCode() {
            return Arrays.hashCode(new int[] {rowIndex, colIndex});
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Coordinate && (((Coordinate)other).rowIndex == this.rowIndex && ((Coordinate)other).colIndex == this.colIndex);
        }
    }
}
