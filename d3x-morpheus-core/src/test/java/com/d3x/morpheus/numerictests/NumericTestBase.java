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
package com.d3x.morpheus.numerictests;

import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.util.DoubleComparator;

import static org.testng.Assert.*;

/**
 * Provides a base class with methods that allow for floating-point
 * comparisons by a {@code DoubleComparator}.
 */
public abstract class NumericTestBase {
    /**
     * The DoubleComparator used by {@code assertDouble}.
     */
    protected final DoubleComparator myComparator;

    /**
     * Creates a new test class with the default comparator.
     */
    protected NumericTestBase() {
        this(DoubleComparator.DEFAULT);
    }

    /**
     * Creates a new test class with a specialized comparator.
     * @param comparator the DoubleComparator to use in {@code assertDouble}.
     */
    protected NumericTestBase(DoubleComparator comparator) {
        this.myComparator = comparator;
    }

    /**
     * Asserts that an actual value matches an expected value to
     * within the tolerance of the DoubleComparator for this class.
     *
     * @param actual   the actual floating-point value.
     * @param expected the expected floating-point value.
     */
    public void assertDouble(double actual, double expected) {
        assertDouble(actual, expected, myComparator);
    }

    /**
     * Asserts that an actual value matches an expected value to
     * within the tolerance of a particular DoubleComparator.
     *
     * @param actual     the actual floating-point value.
     * @param expected   the expected floating-point value.
     * @param comparator the comparator to use for equality tests.
     */
    public static void assertDouble(double actual, double expected, DoubleComparator comparator) {
        if (!comparator.equals(actual, expected))
            fail(String.format("Expected [%f] but found [%f].", expected, actual));
    }

    /**
     * Asserts that values from an actual DoubleSeries match those in an
     * expected DoubleSeries to within the tolerance of the DoubleComparator.
     *
     * @param actual     the actual DoubleSeries.
     * @param expected   the expected DoubleSeries.
     * @param keys       the keys of the values to examine.
     */
    public <K> void assertSeries(DoubleSeries<K> actual, DoubleSeries<K> expected, Iterable<K> keys) {
        assertSeries(actual, expected, keys, myComparator);
    }

    /**
     * Asserts that values from an actual DoubleSeries match those in an
     * expected DoubleSeries to within the tolerance of the DoubleComparator.
     *
     * @param actual   the actual DoubleSeries.
     * @param expected the expected DoubleSeries.
     * @param keys     the keys of the values to examine.
     * @param comparator the comparator to use for equality tests.
     */
    public static <K> void assertSeries(DoubleSeries<K> actual, DoubleSeries<K> expected, Iterable<K> keys, DoubleComparator comparator) {
        for (K key : keys) {
            double actualValue = actual.getDouble(key);
            double expectedValue = expected.getDouble(key);

            if (!comparator.equals(actualValue, expectedValue))
                fail(String.format("Expected [%s => %f] but found [%s => %f].",
                        key.toString(), expectedValue, key.toString(), actualValue));
        }
    }
}
