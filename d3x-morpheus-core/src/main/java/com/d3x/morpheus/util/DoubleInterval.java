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

import java.util.function.DoublePredicate;

import lombok.Getter;
import lombok.NonNull;

/**
 * Represents an interval on the real number line.
 *
 * @author Scott Shaffer
 */
public final class DoubleInterval implements DoublePredicate {
    /**
     * The enumerated type for this interval.
     */
    @Getter
    @NonNull
    private final DoubleIntervalType type;

    /**
     * The lower bound of this interval.
     */
    @Getter
    private final double lower;

    /**
     * The upper bound of this interval.
     */
    @Getter
    private final double upper;

    private DoubleInterval(@NonNull DoubleIntervalType type, double lower, double upper) {
        this.type = type;
        this.lower = lower;
        this.upper = upper;
        validate();
    }

    private void validate() {
        if (lower > upper)
            throw new MorpheusException("Invalid interval: [%f, %f].", lower, upper);
    }

    /**
     * The empty range.
     */
    public static DoubleInterval EMPTY = open(0.0, 0.0);

    /**
     * The interval containing fractional real numbers: {@code [0.0, 1.0]}.
     */
    public static DoubleInterval FRACTIONAL = closed(0.0, 1.0);

    /**
     * The interval containing valid percentiles: {@code [0.0, 100.0]}.
     */
    public static DoubleInterval PERCENTILE = closed(0.0, 100.0);

    /**
     * The interval containing all real numbers: {@code [-Inf, +Inf]}.
     */
    public static DoubleInterval INFINITE = closed(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    /**
     * The interval containing all negative real numbers: {@code [-Inf, 0.0)}.
     */
    public static DoubleInterval NEGATIVE = leftClosed(Double.NEGATIVE_INFINITY, 0.0);

    /**
     * The interval containing all non-negative real numbers: {@code [0.0, +Inf]}.
     */
    public static DoubleInterval NON_NEGATIVE = closed(0.0, Double.POSITIVE_INFINITY);

    /**
     * The interval containing all non-positive real numbers: {@code [-Inf, 0.0]}.
     */
    public static DoubleInterval NON_POSITIVE = closed(Double.NEGATIVE_INFINITY, 0.0);

    /**
     * The interval containing all positive real numbers: {@code (0.0, +Inf]}.
     */
    public static DoubleInterval POSITIVE = leftOpen(0.0, Double.POSITIVE_INFINITY);

    /**
     * Creates a new open interval.
     *
     * @param lower the (exclusive) lower bound of the interval.
     *
     * @param upper the (exclusive) upper bound of the interval.
     *
     * @return the new open interval {@code (lower, upper)}.
     */
    public static DoubleInterval open(double lower, double upper) {
        return new DoubleInterval(DoubleIntervalType.OPEN, lower, upper);
    }

    /**
     * Creates a new left-open interval.
     *
     * @param lower the (exclusive) lower bound of the interval.
     *
     * @param upper the (inclusive) upper bound of the interval.
     *
     * @return the new left-open interval {@code (lower, upper]}.
     */
    public static DoubleInterval leftOpen(double lower, double upper) {
        return new DoubleInterval(DoubleIntervalType.LEFT_OPEN, lower, upper);
    }

    /**
     * Creates a new left-closed interval.
     *
     * @param lower the (inclusive) lower bound of the interval.
     *
     * @param upper the (exclusive) upper bound of the interval.
     *
     * @return the new closed interval {@code [lower, upper)}.
     */
    public static DoubleInterval leftClosed(double lower, double upper) {
        return new DoubleInterval(DoubleIntervalType.LEFT_CLOSED, lower, upper);
    }

    /**
     * Creates a new closed interval.
     *
     * @param lower the (inclusive) lower bound of the interval.
     *
     * @param upper the (inclusive) upper bound of the interval.
     *
     * @return the new closed interval {@code [lower, upper]}.
     */
    public static DoubleInterval closed(double lower, double upper) {
        return new DoubleInterval(DoubleIntervalType.CLOSED, lower, upper);
    }

    /**
     * Determines whether this interval contains a double value.
     *
     * @param value the value to test.
     *
     * @return {@code true} iff this interval contains the specified value.
     */
    public boolean contains(double value) {
        return type.contains(value, lower, upper);
    }

    /**
     * Returns the width of this interval.
     * @return the width of this interval.
     */
    public double getWidth() {
        return upper - lower;
    }

    /**
     * Ensures that a double value lies within this interval.
     *
     * @param value       the value to validate.
     * @param description a description for the exception message.
     *
     * @throws RuntimeException unless this interval contains the
     * specified value.
     */
    public void validate(double value, String description) {
        if (!contains(value))
            throw new MorpheusException("Invalid %s [%f].", description, value);
    }

    @Override
    public boolean test(double value) {
        return contains(value);
    }

    @Override public boolean equals(Object that) {
        return (that instanceof DoubleInterval) && equalsInterval((DoubleInterval) that);
    }

    private boolean equalsInterval(DoubleInterval that) {
        return this.type.equals(that.type)
                && DoubleComparator.DEFAULT.equals(this.lower, that.lower)
                && DoubleComparator.DEFAULT.equals(this.upper, that.upper);
    }

    @Override public String toString() {
        return type.format(lower, upper);
    }
}
