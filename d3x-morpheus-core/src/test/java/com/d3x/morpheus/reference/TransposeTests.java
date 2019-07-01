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

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for transpose functionality
 *
 * @author Xavier Witdouck
 */
public class TransposeTests {


    @Test()
    public void transpose() {
        var rows = Range.of(0, 100).map(i -> "Row-" + i);
        var cols = Range.of(0, 10).map(i -> "Col-" + i);
        var source = DataFrame.ofDoubles(rows, cols, v -> Math.random());
        var transpose = source.transpose();
        assertEquals(source, transpose);
    }


    @Test()
    public void transposeFilter() {
        var rows = Range.of(0, 100).map(i -> "Row-" + i);
        var cols = Range.of(0, 10).map(i -> "Col-" + i);
        var source = DataFrame.ofDoubles(rows, cols, v -> Math.random()).rows().select(v -> v.ordinal() % 2 == 0);
        var transpose = source.transpose();
        assertEquals(source, transpose);
    }


    @Test()
    public void transposeBeforeFilter() {
        var rows = Range.of(0, 100).map(i -> "Row-" + i);
        var cols = Range.of(0, 10).map(i -> "Col-" + i);
        var source = DataFrame.ofDoubles(rows, cols, v -> Math.random());
        var transpose = source.transpose().cols().select(v -> v.ordinal() % 2 == 0);
        source = source.rows().select(v -> v.ordinal() % 2 == 0);
        source.out().print();
        transpose.out().print();
        assertEquals(source, transpose);
    }


    /**
     * Asserts that all values match between the frame and its transpose
     * @param source        the source frame
     * @param transpose     the transpose frame
     */
    private void assertEquals(DataFrame<String,String> source, DataFrame<String,String> transpose) {
        var rows = source.rows().keyArray();
        var cols = source.cols().keyArray();
        rows.forEach(row -> cols.forEach(col -> {
            var v1 = source.getDouble(row, col);
            var v2 = transpose.getDouble(col, row);
            Assert.assertEquals(v1, v2, 0.00000001d);
        }));
        for (int i=0; i<source.rowCount(); ++i) {
            for (int j=0; j<source.colCount(); ++j) {
                var v1 = source.getDoubleAt(i, j);
                var v2 = transpose.getDoubleAt(j, i);
                Assert.assertEquals(v1, v2, 0.00000001d);
            }
        }
    }
}
