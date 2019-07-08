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
package com.d3x.morpheus.reference;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.range.Range;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for the various data frame cursor interfaces
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class CursorTests {


    public enum Scenario { Scenario1, Scenario2, Scenario3, Scenario4, Scenario5, Scenario6 }


    private DataFrame<String,String> testFrame(int rowCount, int colCount) {
        var rows = Range.of(0, rowCount).map(i -> "R" + i);
        var cols = Range.of(0, colCount).map(i -> "C" + i);
        return DataFrame.ofDoubles(rows, cols, v -> 0d);
    }


    @DataProvider(name="scenarios")
    public Object[][] scenarios() {
        return new Object[][] {
            { Scenario.Scenario1 },
            { Scenario.Scenario2 },
            { Scenario.Scenario3 },
            { Scenario.Scenario4 },
            { Scenario.Scenario5 },
            { Scenario.Scenario6 },
        };
    }


    @Test()
    public void rowCursor() {
        var frame = testFrame(10, 10);
        var row = frame.rows().cursor();
        Assert.assertTrue(row.tryKey("R0"));
        Assert.assertTrue(row.tryKey("R9"));
        Assert.assertTrue(row.tryOrdinal(0));
        Assert.assertTrue(row.tryOrdinal(9));
        Assert.assertFalse(row.tryKey("R20"));
        Assert.assertFalse(row.tryOrdinal(-1));
        Assert.assertFalse(row.tryOrdinal(20));
        for (int i=0; i<frame.rowCount(); ++i) {
            var key = frame.rows().key(i);
            var copy = frame.copy();
            copy.rowAt(i).applyDoubles(v -> Math.random());
            row.atOrdinal(i).applyDoubles(v -> copy.getDouble(v.rowKey(), v.colKey()));
            frame.out().print();
            DataFrameAsserts.assertEqualsByIndex(frame, copy);
            copy.row(key).applyDoubles(v -> Math.random());
            row.atKey(key).applyDoubles(v -> copy.getDouble(v.rowKey(), v.colKey()));
            DataFrameAsserts.assertEqualsByIndex(frame, copy);
        }
    }


    @Test()
    public void colCursor() {
        var frame = testFrame(10, 10);
        var column = frame.cols().cursor();
        Assert.assertTrue(column.tryKey("C0"));
        Assert.assertTrue(column.tryKey("C9"));
        Assert.assertTrue(column.tryOrdinal(0));
        Assert.assertTrue(column.tryOrdinal(9));
        Assert.assertFalse(column.tryKey("C20"));
        Assert.assertFalse(column.tryOrdinal(-1));
        Assert.assertFalse(column.tryOrdinal(20));
        for (int i=0; i<frame.colCount(); ++i) {
            var key = frame.cols().key(i);
            var copy = frame.copy();
            copy.colAt(i).applyDoubles(v -> Math.random());
            column.atOrdinal(i).applyDoubles(v -> copy.getDouble(v.rowKey(), v.colKey()));
            frame.out().print();
            DataFrameAsserts.assertEqualsByIndex(frame, copy);
            copy.col(key).applyDoubles(v -> Math.random());
            column.atKey(key).applyDoubles(v -> copy.getDouble(v.rowKey(), v.colKey()));
            DataFrameAsserts.assertEqualsByIndex(frame, copy);
        }
    }


    @Test(dataProvider="scenarios", expectedExceptions= DataFrameException.class)
    public void rowCursorFail(Scenario scenario) {
        var frame = testFrame(5,5 );
        switch (scenario) {
            case Scenario1: frame.row("R22");               break;
            case Scenario2: frame.rowAt(-20);             break;
            case Scenario3: frame.rowAt(100);             break;
            case Scenario4: frame.rows().cursor().atKey("R23");     break;
            case Scenario5: frame.rows().cursor().atOrdinal(-1);    break;
            case Scenario6: frame.rows().cursor().atOrdinal(100);   break;
            default:    throw new IllegalArgumentException("Not supported");
        }
    }


    @Test(dataProvider="scenarios", expectedExceptions= DataFrameException.class)
    public void colCursorFail(Scenario scenario) {
        var frame = testFrame(5,5 );
        switch (scenario) {
            case Scenario1: frame.col("C22");               break;
            case Scenario2: frame.colAt(-20);             break;
            case Scenario3: frame.colAt(100);             break;
            case Scenario4: frame.cols().cursor().atKey("C23");     break;
            case Scenario5: frame.cols().cursor().atOrdinal(-1);    break;
            case Scenario6: frame.cols().cursor().atOrdinal(100);   break;
            default:    throw new IllegalArgumentException("Not supported");
        }
    }


}
