/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.vector;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ApacheDenseVectorTest {
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testCopyOfArray() {
        double[] array = new double[] { 1.0, 2.0, 3.0 };
        ApacheDenseVector vector = ApacheDenseVector.copyOf(array);

        assertEquals(vector.length(), 3);
        assertEquals(vector.get(0), 1.0, TOLERANCE);
        assertEquals(vector.get(1), 2.0, TOLERANCE);
        assertEquals(vector.get(2), 3.0, TOLERANCE);

        vector.set(0, 11.0);
        vector.set(1, 22.0);
        vector.set(2, 33.0);

        // The original array is not changed...
        assertEquals(array[0], 1.0, TOLERANCE);
        assertEquals(array[1], 2.0, TOLERANCE);
        assertEquals(array[2], 3.0, TOLERANCE);

        assertEquals(vector.get(0), 11.0, TOLERANCE);
        assertEquals(vector.get(1), 22.0, TOLERANCE);
        assertEquals(vector.get(2), 33.0, TOLERANCE);
    }

    @Test
    public void testCopyOfVector() {
        ApacheDenseVector orig = ApacheDenseVector.copyOf(1.0, 2.0, 3.0);
        ApacheDenseVector copy = ApacheDenseVector.copyOf(orig);

        assertEquals(copy.length(), 3);
        assertEquals(copy.get(0), 1.0, TOLERANCE);
        assertEquals(copy.get(1), 2.0, TOLERANCE);
        assertEquals(copy.get(2), 3.0, TOLERANCE);

        copy.set(0, 11.0);
        copy.set(1, 22.0);
        copy.set(2, 33.0);

        // The original vector is not changed...
        assertEquals(orig.get(0), 1.0, TOLERANCE);
        assertEquals(orig.get(1), 2.0, TOLERANCE);
        assertEquals(orig.get(2), 3.0, TOLERANCE);

        assertEquals(copy.get(0), 11.0, TOLERANCE);
        assertEquals(copy.get(1), 22.0, TOLERANCE);
        assertEquals(copy.get(2), 33.0, TOLERANCE);
    }

    @Test
    public void testOfGet() {
        ApacheDenseVector vector = ApacheDenseVector.of(1.0, 2.0, 3.0);
        assertEquals(vector.length(), 3);
        assertEquals(vector.get(0), 1.0, TOLERANCE);
        assertEquals(vector.get(1), 2.0, TOLERANCE);
        assertEquals(vector.get(2), 3.0, TOLERANCE);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testOfSet() {
        ApacheDenseVector vector = ApacheDenseVector.of(1.0, 2.0, 3.0);
        vector.set(0, 11.0);
    }

    @Test
    public void testOfLength() {
        ApacheDenseVector vector = ApacheDenseVector.ofLength(3);
        assertEquals(vector.length(), 3);

        assertEquals(vector.get(0), 0.0, TOLERANCE);
        assertEquals(vector.get(1), 0.0, TOLERANCE);
        assertEquals(vector.get(2), 0.0, TOLERANCE);

        vector.set(0, 11.0);
        vector.set(1, 22.0);
        vector.set(2, 33.0);

        assertEquals(vector.get(0), 11.0, TOLERANCE);
        assertEquals(vector.get(1), 22.0, TOLERANCE);
        assertEquals(vector.get(2), 33.0, TOLERANCE);
    }

    @Test
    public void testWrap() {
        double[] array = new double[] { 1.0, 2.0, 3.0 };
        ApacheDenseVector vector = ApacheDenseVector.wrap(array);

        assertEquals(vector.length(), 3);
        assertEquals(vector.get(0), 1.0, TOLERANCE);
        assertEquals(vector.get(1), 2.0, TOLERANCE);
        assertEquals(vector.get(2), 3.0, TOLERANCE);

        vector.set(0, 11.0);
        vector.set(1, 22.0);
        vector.set(2, 33.0);

        // The original array is also changed...
        assertEquals(array[0], 11.0, TOLERANCE);
        assertEquals(array[1], 22.0, TOLERANCE);
        assertEquals(array[2], 33.0, TOLERANCE);

        assertEquals(vector.get(0), 11.0, TOLERANCE);
        assertEquals(vector.get(1), 22.0, TOLERANCE);
        assertEquals(vector.get(2), 33.0, TOLERANCE);
    }
}
