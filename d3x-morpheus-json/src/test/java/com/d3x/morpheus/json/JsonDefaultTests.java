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
package com.d3x.morpheus.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAsserts;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.IO;
import com.d3x.morpheus.util.Resource;
import org.testng.annotations.Test;

/**
 * Unit tests for the default json serializer for Morpheus DataFrames
 *
 * @author Xavier Witdouck
 */
public class JsonDefaultTests {


    @SuppressWarnings("unchecked")
    private static <R,C> DataFrame<R,C> random(Array<R> rowKeys, Array<C> colKeys) {
        return DataFrame.of(rowKeys, (Class<C>)colKeys.getValue(0).getClass(), columns -> {
            var index = new AtomicInteger(-1);
            var random = new Random();
            colKeys.forEach(c -> {
                switch (index.incrementAndGet() % 10) {
                    case 0: columns.add(c, Double.class, v -> random.nextDouble() * 1000);                              break;
                    case 1: columns.add(c, Integer.class, v -> random.nextInt());                                       break;
                    case 2: columns.add(c, Long.class, v -> random.nextLong());                                         break;
                    case 3: columns.add(c, String.class, v -> String.valueOf("text-" + random.nextInt()));              break;
                    case 4: columns.add(c, LocalDate.class, v -> LocalDate.now().minusDays(v.rowOrdinal()));            break;
                    case 5: columns.add(c, LocalTime.class, v -> LocalTime.now().minusSeconds(v.rowOrdinal()));         break;
                    case 6: columns.add(c, LocalDateTime.class, v -> LocalDateTime.now().minusMinutes(v.rowOrdinal())); break;
                    case 7: columns.add(c, ZonedDateTime.class, v -> ZonedDateTime.now().minusMinutes(v.rowOrdinal())); break;
                }
            });
        });
    }



    @Test()
    public void case1() {
        var rows = Range.of(0, 100).toArray();
        var cols = Range.of(0, 10).map(v -> "Column-" + v).toArray();
        var frame = random(rows, cols);
        serialize(frame);
    }


    @Test()
    public void case2() {
        var rows = Range.of(0, 100).map(v -> "Row-" + v).toArray();
        var cols = Range.of(0, 10).map(v -> "Column-" + v).toArray();
        var frame = random(rows, cols);
        serialize(frame);
    }


    @Test()
    public void case3() {
        var rows = Range.of(0, 100).map(v -> LocalDate.now().plusDays(v)).toArray();
        var cols = Range.of(0, 10).map(v -> "Column-" + v).toArray();
        var frame = random(rows, cols);
        serialize(frame);
    }


    @Test()
    public void case4() {
        var rows = Range.of(0, 100).map(v -> LocalDateTime.now().plusMinutes(v)).toArray();
        var cols = Range.of(0, 10).map(v -> "Column-" + v).toArray();
        var frame = random(rows, cols);
        serialize(frame);
    }


    @Test()
    public void case5() {
        var rows = Range.of(0, 100).map(v -> ZonedDateTime.now().plusMinutes(v)).toArray();
        var cols = Range.of(0, 10).map(v -> "Column-" + v).toArray();
        var frame = random(rows, cols);
        serialize(frame);
    }


    @Test()
    public void case6() {
        var rows = Range.of(0, 100).map(v -> LocalDate.now().plusDays(v)).toArray();
        var cols = Range.of(0, 10).map(v -> LocalDate.now().plusDays(v)).toArray();
        var frame = random(rows, cols);
        serialize(frame);
    }




    private <R,C> void serialize(DataFrame<R,C> frame) {
        var sink = new JsonSinkDefault<R,C>();
        var os = new ByteArrayOutputStream(1024 * 10);
        sink.write(frame, o -> o.resource(Resource.of(os)).pretty(true));
        var json = new String(os.toByteArray());
        IO.println(json);
        var source = new JsonSourceDefault<R,C>();
        var result = source.read(o -> o.resource(Resource.of(new ByteArrayInputStream(os.toByteArray()))));
        DataFrameAsserts.assertEqualsByIndex(frame, result);
    }

}
