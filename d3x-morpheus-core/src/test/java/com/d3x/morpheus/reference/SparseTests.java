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

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.IO;
import org.eclipse.collections.impl.factory.primitive.IntDoubleMaps;
import org.testng.annotations.Test;

/**
 * Unit tests for sparse data frames
 *
 * @author  Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public class SparseTests {


    @Test()
    public void sparse() {
        var rowCount = 1000000;
        var rows = Range.of(0, rowCount).map(i -> "R" + i);
        var frame = DataFrame.of(rows, String.class, columns -> {
            columns.add("C1", Array.of(Double.class, rowCount, 0.01f));
            columns.add("C2", Array.of(Double.class, rowCount, 0.01f));
            columns.add("C3", Array.of(Double.class, rowCount, 0.01f));
            columns.add("C4", Array.of(Double.class, rowCount, 0.01f));
            columns.add("C5", Array.of(Double.class, rowCount, 0.01f));
        });
        frame.out().print();
    }


    @Test()
    public void eclipse() {

        var data = IntDoubleMaps.mutable.withInitialCapacity(10);
        data.put(25, 10000000d);
        data.put(45, 0.3434d);
        data.put(100000, 0.2344d);
        data.put(47, Math.random());
        data.put(1002, Math.random());
        data.put(12345, Math.random());
        data.put(854, Math.random());
        IO.println(data);

    }

}
