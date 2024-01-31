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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class D3xVectorViewTest {
    private static final double NAN = Double.NaN;
    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testAnyAll() {
        D3xVectorView view1 = D3xVectorView.of(1.0, 2.0, 3.0);
        D3xVectorView view2 = D3xVectorView.of(1.0, NAN, 3.0);

        assertTrue(view1.all(Double::isFinite));
        assertTrue(view1.all(x -> x > 0.0));
        assertFalse(view1.all(x -> x > 1.1));
        assertFalse(view2.all(Double::isFinite));

        assertFalse(view1.any(Double::isNaN));
        assertFalse(view1.any(x -> x < 0.0));
        assertTrue(view1.any(x -> x < 1.1));
        assertTrue(view2.any(Double::isNaN));
    }

    @Test
    public void testArrayView() {
        assertEquals(D3xVectorView.of().length(), 0);

        D3xVectorView view = D3xVectorView.of(1.0, 2.0, 3.0);
        assertEquals(view.length(), 3);
        assertEquals(view.get(0), 1.0, TOLERANCE);
        assertEquals(view.get(1), 2.0, TOLERANCE);
        assertEquals(view.get(2), 3.0, TOLERANCE);
    }

    @Test
    public void testListView() {
        assertEquals(D3xVectorView.of(List.of()).length(), 0);

        D3xVectorView view = D3xVectorView.of(List.of(1.0, 2.0, 3.0));
        assertEquals(view.length(), 3);
        assertEquals(view.get(0), 1.0, TOLERANCE);
        assertEquals(view.get(1), 2.0, TOLERANCE);
        assertEquals(view.get(2), 3.0, TOLERANCE);
    }

    @Test
    public void testCumSum() {
        var vec1 = D3xVectorView.of(1.0, 2.0, 3.0, -4.0);
        var vec2 = D3xVectorView.of(1.0, 3.0, 6.0, 2.0);
        var vec3 = vec1.cumsum();
        assertTrue(vec3.equalsView(vec2));
    }

    @Test
    public void testCumProd() {
        var vec1 = D3xVectorView.of(2.0, -3.0, 3.0, -0.5);
        var vec2 = D3xVectorView.of(2.0, -6.0, -18.0, 9.0);
        var vec3 = vec1.cumprod();
        assertTrue(vec3.equalsView(vec2));
    }

    @Test
    public void testDiff() {
        var vec1 = D3xVectorView.of(1.0, 4.0, 9.0, 16.0, 25.0, 36.0);
        var vec2 = vec1.diff();
        var vec3 = vec1.diff(3);

        assertTrue(vec2.equalsView(D3xVectorView.of(NAN, 3.0, 5.0, 7.0, 9.0, 11.0)));
        assertTrue(vec3.equalsView(D3xVectorView.of(NAN, NAN, NAN, 15.0, 21.0, 27.0)));
    }

    @Test
    public void testDivideEBE() {
        var vec1 = D3xVectorView.of(1.0, 2.0, 3.0);
        var vec2 = D3xVectorView.of(5.0, 4.0, 2.0);
        var vec3 = D3xVectorView.of(0.2, 0.5, 1.5);

        assertTrue(D3xVectorView.divideEBE(vec1, vec2).equalsView(vec3));
    }

    @Test
    public void testEqualsView() {
        D3xVectorView view1 = D3xVectorView.of(1.0, 2.0, 3.0);
        D3xVectorView view2 = D3xVectorView.of(List.of(1.0, 2.0, 3.0));
        D3xVectorView view3 = D3xVectorView.of(1.0, 2.0);
        D3xVectorView view4 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0);

        assertTrue(view1.equalsView(view1));
        assertTrue(view1.equalsView(view2));
        assertTrue(view2.equalsView(view1));
        assertFalse(view1.equalsView(view3));
        assertFalse(view1.equalsView(view4));
    }

    @Test
    public void testIterator() {
        assertFalse(D3xVectorView.of().iterator().hasNext());

        Iterator<Double> iterator = D3xVectorView.of(1.0, 2.0, 3.0).iterator();

        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), 1.0, TOLERANCE);

        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), 2.0, TOLERANCE);

        assertTrue(iterator.hasNext());
        assertEquals(iterator.next(), 3.0, TOLERANCE);

        assertFalse(iterator.hasNext());
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void testIteratorException() {
        D3xVectorView.of().iterator().next();
    }

    @Test
    public void testMultiplyEBE() {
        var vec1 = D3xVectorView.of(1.0, 2.0, 3.0);
        var vec2 = D3xVectorView.of(5.0, 4.0, 2.0);
        var vec3 = D3xVectorView.of(5.0, 8.0, 6.0);

        assertTrue(D3xVectorView.multiplyEBE(vec1, vec2).equalsView(vec3));
    }

    @Test
    public void testReverse() {
        D3xVectorView view1 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0);
        D3xVectorView view2 = D3xVectorView.of(4.0, 3.0, 2.0, 1.0);

        assertEquals(view1.reverse(), view2);
    }

    @Test
    public void testSubVectorView() {
        D3xVectorView view1 = D3xVectorView.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        D3xVectorView view2 = view1.subVectorView(2, 3);
        D3xVectorView view3 = view1.subVectorView(3, 2);

        assertTrue(view2.equalsView(D3xVectorView.of(3.0, 4.0, 5.0)));
        assertTrue(view3.equalsView(D3xVectorView.of(4.0, 5.0)));
    }

    @Test
    public void testStream() {
        var vec1 = D3xVectorView.of();
        var vec2 = D3xVectorView.of(5.0, 4.0, 8.0);

        assertEquals(vec1.stream().count(), 0);
        assertEquals(vec1.stream().sum(), 0.0, TOLERANCE);
        assertTrue(vec1.stream().max().isEmpty());
        assertTrue(vec1.stream().min().isEmpty());

        assertEquals(vec2.stream().count(), 3);
        assertEquals(vec2.stream().sum(), 17.0, TOLERANCE);
        assertEquals(vec2.stream().max().orElseThrow(), 8.0, TOLERANCE);
        assertEquals(vec2.stream().min().orElseThrow(), 4.0, TOLERANCE);
    }
}

