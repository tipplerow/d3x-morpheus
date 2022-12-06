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
package com.d3x.morpheus.util;

import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * @author Scott Shaffer
 */
public class DoubleIntervalTest {
    private static final double LOWER = -2.0;
    private static final double UPPER =  1.0;

    private static final double SMALL = 1.0E-08;
    private static final double TINY  = 1.0E-15;

    private static final DoubleInterval CLOSED = DoubleInterval.closed(LOWER, UPPER);
    private static final DoubleInterval LEFT_CLOSED = DoubleInterval.leftClosed(LOWER, UPPER);
    private static final DoubleInterval LEFT_OPEN = DoubleInterval.leftOpen(LOWER, UPPER);
    private static final DoubleInterval OPEN = DoubleInterval.open(LOWER, UPPER);

    private static final double TOLERANCE = 1.0E-12;

    @Test
    public void testBound() {
        var intervals = List.of(CLOSED, LEFT_CLOSED, LEFT_OPEN, OPEN);
        var values = new double[] { -100.0, -2.1, -2.0, -1.9, 0.0, 0.9, 1.0, 1.1, 100.0 };

        for (var interval : intervals) {
            for (var value : values) {
                assertTrue(interval.contains(interval.bound(value)));
            }
        }

        assertEquals(CLOSED.bound(-100.0), -2.0, TOLERANCE);
        assertEquals(CLOSED.bound(-1.9), -1.9, TOLERANCE);
        assertEquals(CLOSED.bound(0.9), 0.9, TOLERANCE);
        assertEquals(CLOSED.bound(100.0), 1.0, TOLERANCE);

        assertEquals(LEFT_CLOSED.bound(-100.0), -2.0, TOLERANCE);
        assertEquals(LEFT_CLOSED.bound(-1.9), -1.9, TOLERANCE);
        assertEquals(LEFT_CLOSED.bound(0.9), 0.9, TOLERANCE);
        assertTrue(LEFT_CLOSED.bound(100.0) < 1.0);
        assertTrue(LEFT_CLOSED.bound(100.0) > 0.99999999);

        assertTrue(LEFT_OPEN.bound(-100.0) > -2.0);
        assertTrue(LEFT_OPEN.bound(-100.0) < -1.99999999);
        assertEquals(LEFT_OPEN.bound(-1.9), -1.9, TOLERANCE);
        assertEquals(LEFT_OPEN.bound(0.9), 0.9, TOLERANCE);
        assertEquals(LEFT_OPEN.bound(100.0), 1.0, TOLERANCE);

        assertTrue(OPEN.bound(-100.0) > -2.0);
        assertTrue(OPEN.bound(-100.0) < -1.99999999);
        assertEquals(OPEN.bound(-1.9), -1.9, TOLERANCE);
        assertEquals(OPEN.bound(0.9), 0.9, TOLERANCE);
        assertTrue(OPEN.bound(100.0) < 1.0);
        assertTrue(OPEN.bound(100.0) > 0.99999999);
    }

    @Test
    public void testClosed() {
        assertFalse(CLOSED.contains(LOWER - SMALL));
        assertTrue(CLOSED.contains(LOWER - TINY));
        assertTrue(CLOSED.contains(LOWER));
        assertTrue(CLOSED.contains(LOWER + TINY));
        assertTrue(CLOSED.contains(LOWER + SMALL));

        assertTrue(CLOSED.contains(UPPER - SMALL));
        assertTrue(CLOSED.contains(UPPER - TINY));
        assertTrue(CLOSED.contains(UPPER));
        assertTrue(CLOSED.contains(UPPER + TINY));
        assertFalse(CLOSED.contains(UPPER + SMALL));

        assertTrue(CLOSED.contains(0.0));
        assertFalse(CLOSED.contains(Double.NaN));
        assertFalse(CLOSED.contains(Double.NEGATIVE_INFINITY));
        assertFalse(CLOSED.contains(Double.POSITIVE_INFINITY));

        assertEquals(CLOSED.getWidth(), 3.0, TOLERANCE);
    }

    @Test public void testLeftClosed() {
        assertFalse(LEFT_CLOSED.contains(LOWER - SMALL));
        assertTrue(LEFT_CLOSED.contains(LOWER - TINY));
        assertTrue(LEFT_CLOSED.contains(LOWER));
        assertTrue(LEFT_CLOSED.contains(LOWER + TINY));
        assertTrue(LEFT_CLOSED.contains(LOWER + SMALL));

        assertTrue(LEFT_CLOSED.contains(UPPER - SMALL));
        assertFalse(LEFT_CLOSED.contains(UPPER - TINY));
        assertFalse(LEFT_CLOSED.contains(UPPER));
        assertFalse(LEFT_CLOSED.contains(UPPER + TINY));
        assertFalse(LEFT_CLOSED.contains(UPPER + SMALL));

        assertTrue(LEFT_CLOSED.contains(0.0));
        assertFalse(LEFT_CLOSED.contains(Double.NaN));
        assertFalse(LEFT_CLOSED.contains(Double.NEGATIVE_INFINITY));
        assertFalse(LEFT_CLOSED.contains(Double.POSITIVE_INFINITY));

        assertEquals(LEFT_CLOSED.getWidth(), 3.0, TOLERANCE);
    }

    @Test public void testLeftOpen() {
        assertFalse(LEFT_OPEN.contains(LOWER - SMALL));
        assertFalse(LEFT_OPEN.contains(LOWER - TINY));
        assertFalse(LEFT_OPEN.contains(LOWER));
        assertFalse(LEFT_OPEN.contains(LOWER + TINY));
        assertTrue(LEFT_OPEN.contains(LOWER + SMALL));

        assertTrue(LEFT_OPEN.contains(UPPER - SMALL));
        assertTrue(LEFT_OPEN.contains(UPPER - TINY));
        assertTrue(LEFT_OPEN.contains(UPPER));
        assertTrue(LEFT_OPEN.contains(UPPER + TINY));
        assertFalse(LEFT_OPEN.contains(UPPER + SMALL));

        assertTrue(LEFT_OPEN.contains(0.0));
        assertFalse(LEFT_OPEN.contains(Double.NaN));
        assertFalse(LEFT_OPEN.contains(Double.NEGATIVE_INFINITY));
        assertFalse(LEFT_OPEN.contains(Double.POSITIVE_INFINITY));

        assertEquals(LEFT_OPEN.getWidth(), 3.0, TOLERANCE);
    }

    @Test public void testOpen() {
        assertFalse(OPEN.contains(LOWER - SMALL));
        assertFalse(OPEN.contains(LOWER - TINY));
        assertFalse(OPEN.contains(LOWER));
        assertFalse(OPEN.contains(LOWER + TINY));
        assertTrue(OPEN.contains(LOWER + SMALL));

        assertTrue(OPEN.contains(UPPER - SMALL));
        assertFalse(OPEN.contains(UPPER - TINY));
        assertFalse(OPEN.contains(UPPER));
        assertFalse(OPEN.contains(UPPER + TINY));
        assertFalse(OPEN.contains(UPPER + SMALL));

        assertTrue(OPEN.contains(0.0));
        assertFalse(OPEN.contains(Double.NaN));
        assertFalse(OPEN.contains(Double.NEGATIVE_INFINITY));
        assertFalse(OPEN.contains(Double.POSITIVE_INFINITY));

        assertEquals(OPEN.getWidth(), 3.0, TOLERANCE);
    }

    @Test public void testEmpty() {
        DoubleInterval interval = DoubleInterval.EMPTY;

        assertFalse(interval.contains(-1.0E+20));
        assertFalse(interval.contains(0.0));
        assertFalse(interval.contains(+1.0E+20));

        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 0.0, TOLERANCE);
    }

    @Test public void testFractional() {
        DoubleInterval interval = DoubleInterval.FRACTIONAL;

        assertFalse(interval.contains(-0.00000001));
        assertTrue(interval.contains(0.0));
        assertTrue(interval.contains(1.0));
        assertFalse(interval.contains(1.00000001));

        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 1.0, TOLERANCE);
    }

    @Test public void testPercentile() {
        DoubleInterval interval = DoubleInterval.PERCENTILE;

        assertFalse(interval.contains(-0.000001));
        assertTrue(interval.contains(0.0));
        assertTrue(interval.contains(100.0));
        assertFalse(interval.contains(100.000001));

        assertFalse(interval.contains(Double.NaN));
        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertEquals(interval.getWidth(), 100.0, TOLERANCE);
    }

    @Test public void testInfinite() {
        DoubleInterval interval = DoubleInterval.INFINITE;

        assertTrue(interval.contains(Double.NEGATIVE_INFINITY));
        assertTrue(interval.contains(-1.0E+20));
        assertTrue(interval.contains( 0.0));
        assertTrue(interval.contains(+1.0E+20));
        assertTrue(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));
    }

    @Test public void testNegative() {
        DoubleInterval interval = DoubleInterval.NEGATIVE;

        assertTrue(interval.contains(Double.NEGATIVE_INFINITY));
        assertTrue(interval.contains(-1.0E+20));
        assertTrue(interval.contains(-1.0));
        assertFalse(interval.contains( 0.0));
        assertFalse(interval.contains(+1.0));
        assertFalse(interval.contains(+1.0E+20));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));
    }

    @Test public void testNonNegative() {
        DoubleInterval interval = DoubleInterval.NON_NEGATIVE;

        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(-1.0E+20));
        assertFalse(interval.contains(-1.0));
        assertTrue(interval.contains( 0.0));
        assertTrue(interval.contains(+1.0));
        assertTrue(interval.contains(+1.0E+20));
        assertTrue(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));

        assertTrue(interval.contains(interval));
        assertTrue(interval.contains(DoubleInterval.POSITIVE));
    }

    @Test public void testNonPositive() {
        DoubleInterval interval = DoubleInterval.NON_POSITIVE;

        assertTrue(interval.contains(Double.NEGATIVE_INFINITY));
        assertTrue(interval.contains(-1.0E+20));
        assertTrue(interval.contains(-1.0));
        assertTrue(interval.contains( 0.0));
        assertFalse(interval.contains(+1.0));
        assertFalse(interval.contains(+1.0E+20));
        assertFalse(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));
    }

    @Test public void testPositive() {
        DoubleInterval interval = DoubleInterval.POSITIVE;

        assertFalse(interval.contains(Double.NEGATIVE_INFINITY));
        assertFalse(interval.contains(-1.0E+20));
        assertFalse(interval.contains(-1.0));
        assertFalse(interval.contains( 0.0));
        assertTrue(interval.contains(+1.0));
        assertTrue(interval.contains(+1.0E+20));
        assertTrue(interval.contains(Double.POSITIVE_INFINITY));

        assertFalse(interval.contains(Double.NaN));
        assertTrue(Double.isInfinite(interval.getWidth()));

        assertTrue(interval.contains(interval));
        assertFalse(interval.contains(DoubleInterval.NON_NEGATIVE));
    }

    @Test public void testValidateOkay() {
        DoubleInterval.POSITIVE.validate(1.0, "test value");
        DoubleInterval.NEGATIVE.validate(-1.0, "test value");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testValidateBad1() {
        DoubleInterval.POSITIVE.validate(-1.0, "test value");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testValidateBad2() {
        DoubleInterval.NEGATIVE.validate(1.0, "test value");
    }

    @Test
    public void testParse() {
        assertEquals(DoubleInterval.parse("[-Infinity, Infinity]"), DoubleInterval.INFINITE);
        assertEquals(DoubleInterval.parse("[-1.0, 2.0]"), DoubleInterval.closed(-1.0, 2.0));
        assertEquals(DoubleInterval.parse("[-1.0, 2.0)"), DoubleInterval.leftClosed(-1.0, 2.0));
        assertEquals(DoubleInterval.parse("(-1.0, 2.0]"), DoubleInterval.leftOpen(-1.0, 2.0));
        assertEquals(DoubleInterval.parse("(-1.0, 2.0)"), DoubleInterval.open(-1.0, 2.0));
    }

    @Test
    public void testIsFinite() {
        assertTrue(DoubleInterval.EMPTY.isFinite());
        assertTrue(DoubleInterval.POSITIVE.isFinite());
        assertTrue(DoubleInterval.NEGATIVE.isFinite());
        assertTrue(DoubleInterval.NON_POSITIVE.isFinite());
        assertTrue(DoubleInterval.NON_NEGATIVE.isFinite());
        assertTrue(DoubleInterval.closed(-100.0, 100.0).isFinite());
        assertFalse(DoubleInterval.INFINITE.isFinite());
    }
}
