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

import java.util.Collections;
import java.util.List;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.IO;
import org.testng.annotations.Test;

/**
 * Unit tests on empty data frames
 */
public class EmptyTests {

    @Test()
    public void test() {
        DataFrame.empty().forEach(IO::println);
        DataFrame.empty().rows().forEach(IO::println);
        DataFrame.empty().cols().forEach(IO::println);
        DataFrame.empty().values().forEach(IO::println);
        DataFrame.empty().rows().mapKeys(v -> "1");
        DataFrame.empty().cols().mapKeys(v -> "1");
    }

    @Test()
    public void rows() {
        var frame = DataFrame.ofDoubles(List.of("1", "2"), Collections.emptyList(), v -> 0d);
        frame.rows().forEach(v -> v.forEach(IO::println));
        frame.cols().forEach(v -> v.forEach(IO::println));
    }

    @Test()
    public void columns() {
        var frame = DataFrame.ofDoubles(Collections.emptyList(), List.of("1", "2"), v -> 0d);
        frame.rows().forEach(v -> v.forEach(IO::println));
        frame.cols().forEach(v -> v.forEach(IO::println));
    }

}
