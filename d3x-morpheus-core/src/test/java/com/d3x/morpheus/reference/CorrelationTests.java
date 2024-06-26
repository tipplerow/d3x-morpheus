/**
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

import java.io.IOException;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.util.text.parser.Parser;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests of correlation estimator in both the row and column dimensions of a DataFrame
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class CorrelationTests {

    @DataProvider(name="style")
    public Object[][] style() {
        return new Object[][] { {false}, {true} };
    }

    private DataFrame<Integer,String> loadSourceData() throws IOException {
        return DataFrame.read("/stats-correl/source-data.csv").csv();
    }

    private DataFrame<Integer,Integer> loadExpectedRowCorr() throws IOException {
        return DataFrame.read("/stats-correl/row-correl.csv").csv(Integer.class, options -> {
            options.setRowKeyColumnName("Index");
            options.setMaxColumns(2000);
            options.setParser("Index", Parser.ofInteger());
        }).cols().mapKeys(col -> Integer.parseInt(col.key()));
    }

    private DataFrame<String,String> loadExpectedColumnCorr() throws IOException {
        return DataFrame.read("/stats-correl/col-correl.csv").csv(String.class, options -> {
            options.setRowKeyColumnName("Index");
            options.setMaxColumns(2000);
        });
    }


    @Test(dataProvider="style")
    public void correlationOfRows(boolean parallel) throws IOException {
        var source = loadSourceData();
        var rows = parallel ? source.rows().parallel() : source.rows().sequential();
        var corrActual = rows.stats().correlation();
        var corrExpected = loadExpectedRowCorr();

        corrActual.out().print();
        corrExpected.out().print();

        DataFrameAsserts.assertEqualsByIndex(corrExpected, corrActual);
        corrExpected.cols().keys().forEach(key1 -> corrExpected.cols().keys().forEach(key2 -> {
            var expected = corrExpected.getDouble(key1, key2);
            var actual = source.rows().stats().correlation(key1, key2);
            Assert.assertEquals(actual, expected, 0.0000001, "Correlation match for " + key1 + ", " + key2);
        }));
    }


    @Test(dataProvider="style")
    public void correlationOfColumns(boolean parallel) throws IOException {
        var source = loadSourceData();
        var columns = parallel ? source.cols().parallel() : source.cols().sequential();
        var corrActual = columns.stats().correlation();
        var corrExpected = loadExpectedColumnCorr();
        DataFrameAsserts.assertEqualsByIndex(corrExpected, corrActual);
        corrExpected.cols().keys().forEach(key1 -> corrExpected.cols().keys().forEach(key2 -> {
            var expected = corrExpected.getDouble(key1, key2);
            var actual = source.cols().stats().correlation(key1, key2);
            Assert.assertEquals(actual, expected, 0.0000001, "Correlation match for " + key1 + ", " + key2);
        }));
    }


    @Test(dataProvider = "style")
    public void testCorrelationWithNonNumericColumns(boolean parallel) throws IOException {
        var source = loadSourceData();
        source.cols().add("NonNumeric", String.class, v -> "Value:" + v.rowOrdinal());
        var columns = parallel ? source.cols().parallel() : source.cols().sequential();
        var corrActual = columns.stats().correlation();
        var corrExpected = loadExpectedColumnCorr();
        DataFrameAsserts.assertEqualsByIndex(corrExpected, corrActual);
        corrExpected.cols().keys().forEach(key1 -> corrExpected.cols().keys().forEach(key2 -> {
            var expected = corrExpected.getDouble(key1, key2);
            var actual = source.cols().stats().correlation(key1, key2);
            Assert.assertEquals(actual, expected, 0.0000001, "Correlation match for " + key1 + ", " + key2);
        }));
    }



}
