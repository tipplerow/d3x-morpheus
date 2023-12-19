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
package com.d3x.morpheus.dist;

import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.MorpheusException;

import lombok.Value;

import java.util.function.IntPredicate;

/**
 * Represents a contiguous range of support for a discrete probability
 * distribution.
 *
 * @author Scott Shaffer
 */
@Value
public class IntSupport implements IntPredicate {
    /**
     * The inclusive lower bound of this range.
     */
    int lower;

    /**
     * The inclusive upper bound of this range.
     */
    int upper;

    private IntSupport(int lower, int upper) {
        if (lower > upper)
            throw new MorpheusException("Invalid interval: [%d, %d].", lower, upper);

        this.lower = lower;
        this.upper = upper;
    }

    /**
     * Creates a new inclusive support range.
     *
     * @param lower the inclusive lower bound of the range.
     * @param upper the inclusive upper bound of the range.
     *
     * @return the new support range {@code [lower, upper]}.
     *
     * @throws RuntimeException unless {@code lower <= upper}.
     */
    public static IntSupport over(int lower, int upper) {
        return new IntSupport(lower, upper);
    }

    /**
     * The range containing all integers.
     */
    public static IntSupport ALL = over(Integer.MIN_VALUE, Integer.MAX_VALUE);

    /**
     * The range containing all negative integers.
     */
    public static IntSupport NEGATIVE = over(Integer.MIN_VALUE, -1);

    /**
     * The range containing all non-negative integers.
     */
    public static IntSupport NON_NEGATIVE = over(0, Integer.MAX_VALUE);

    /**
     * The range containing all non-positive integers.
     */
    public static IntSupport NON_POSITIVE = over(Integer.MIN_VALUE, 0);

    /**
     * The range containing all positive integers.
     */
    public static IntSupport POSITIVE = over(1, Integer.MAX_VALUE);

    /**
     * Parses the string representation of a support range: {@code [lower, upper]}.
     *
     * @param str the string representation of a range, in the canonical format.
     *
     * @return the range represented by the specified string.
     *
     * @throws RuntimeException unless the string is a properly formatted
     * representation of a valid range.
     */
    public static IntSupport parse(String str) {
        var stripped = str.strip();

        if (stripped.isEmpty())
            throw new MorpheusException("Invalid integer range: %s", str);

        if (stripped.charAt(0) != '[')
            throw new MorpheusException("Invalid integer range: %s", str);

        if (stripped.charAt(stripped.length() - 1) != ']')
            throw new MorpheusException("Invalid integer range: %s", str);

        stripped = stripped.substring(1, stripped.length() - 1);
        var fields = stripped.split(",");

        if (fields.length != 2)
            throw new MorpheusException("Invalid integer range: %s", str);

        var lower = Integer.parseInt(fields[0]);
        var upper = Integer.parseInt(fields[1]);

        return over(lower, upper);
    }

    /**
     * Determines whether this range contains an integer value.
     *
     * @param value the value to test.
     *
     * @return {@code true} iff this range contains the specified value.
     */
    public boolean contains(int value) {
        return lower <= value && value <= upper;
    }

    /**
     * Determines whether this range entirely contains another range.
     *
     * @param that the other range to test.
     *
     * @return {@code true} iff this range entirely contains the input
     * range.
     */
    public boolean contains(IntSupport that) {
        return contains(that.lower) && contains(that.upper);
    }

    /**
     * Writes this range in a canonical format to a string.
     *
     * @return the canonical representation of this range.
     */
    public String format() {
        return '[' + lower + ", " + upper + ']';
    }

    /**
     * Returns the size of this range.
     * @return the size of this range.
     */
    public long size() {
        return ((long) upper) - ((long) lower) + 1;
    }

    /**
     * Returns a range containing the same integers as this support range.
     * @return a range containing the same integers as this support range.
     */
    public Range<Integer> toRange() {
        return Range.of(lower, upper + 1);
    }

    @Override
    public boolean test(int value) {
        return contains(value);
    }

    @Override public String toString() {
        return format();
    }
}
