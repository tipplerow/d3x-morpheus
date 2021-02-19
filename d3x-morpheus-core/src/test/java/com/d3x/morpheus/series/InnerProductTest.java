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
package com.d3x.morpheus.series;

import java.util.List;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class InnerProductTest {
    private static final double TOLERANCE = 1.0E-12;

    private static final DoubleSeries<String> s1 =
            DoubleSeries.build(String.class, List.of("A", "B", "C", "D", "E"), List.of(1.0, 2.0, 3.0, 4.0, 5.0));

    private static final DoubleSeries<String> s2 =
            DoubleSeries.build(String.class, List.of("B", "C", "D", "F", "G", "H"), List.of(10.0, 20.0, 30.0, 40.0, 50.0, 60.0));

    private static final DoubleSeries<String > wt =
            DoubleSeries.build(String.class, List.of("C", "D"), List.of(0.25, 0.75));

    @Test
    public void testEqualWeight() {
        assertEquals(DoubleSeries.innerProduct(s1, s1), 55.0, TOLERANCE);
        assertEquals(DoubleSeries.innerProduct(s1, s2), 2.0 * 10.0 + 3.0 * 20.0 + 4.0 * 30.0, TOLERANCE);
        assertEquals(DoubleSeries.innerProduct(s2, s1), 2.0 * 10.0 + 3.0 * 20.0 + 4.0 * 30.0, TOLERANCE);
    }

    @Test
    public void testWeighted() {
        assertEquals(DoubleSeries.innerProduct(s1, s1, wt), 14.25, TOLERANCE);
        assertEquals(DoubleSeries.innerProduct(s1, s2, wt), 0.25 * 3.0 * 20.0 + 0.75 * 4.0 * 30.0, TOLERANCE);
        assertEquals(DoubleSeries.innerProduct(s2, s1, wt), 0.25 * 3.0 * 20.0 + 0.75 * 4.0 * 30.0, TOLERANCE);
    }
}
