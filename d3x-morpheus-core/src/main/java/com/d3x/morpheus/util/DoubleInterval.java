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

import java.text.DecimalFormat;
import java.util.function.DoublePredicate;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents an interval on the real number line.
 *
 * @author Scott Shaffer
 */
@Builder
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

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.0#######");

    /**
     * Creates a new double interval with fixed type and limits.
     *
     * @param type  the enumerated interval type.
     * @param lower the lower bound of the interval.
     * @param upper the upper bound of the interval.
     *
     * @throws RuntimeException unless the parameters define a valid interval.
     */
    public DoubleInterval(@NonNull DoubleIntervalType type, double lower, double upper) {
        this.type = type;
        this.lower = lower;
        this.upper = upper;
        validate();
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
     * Parses the string representation of an interval.
     *
     * @param str the string representation of an interval, in the
     *            canonical format.
     *
     * @return the interval represented by the specified string.
     *
     * @throws RuntimeException unless the string is a properly formatted
     * representation of a valid interval.
     */
    public static DoubleInterval parse(String str) {
        var stripped = str.strip();

        if (stripped.isEmpty())
            throw new MorpheusException("Invalid double interval: %s", str);

        var type = parseType(stripped);

        stripped = stripped.substring(1, stripped.length() - 1);
        var fields = stripped.split(",");

        if (fields.length != 2)
            throw new MorpheusException("Invalid double interval: %s", str);

        var lower = Double.parseDouble(fields[0]);
        var upper = Double.parseDouble(fields[1]);

        return new DoubleInterval(type, lower, upper);
    }

    private static DoubleIntervalType parseType(String stripped) {
        char lower = stripped.charAt(0);
        char upper = stripped.charAt(stripped.length() - 1);

        for (var type : DoubleIntervalType.values())
            if (lower == type.lowerDelim() && upper == type.upperDelim())
                return type;

        throw new MorpheusException("No matching interval type for delimiters '%c' and '%c'.", lower, upper);
    }

    /**
     * Bounds a floating-point value within this interval.
     *
     * @param value the value to bound.
     *
     * @return the value nearest to the input value that lies within
     * this interval.
     */
    public double bound(double value) {
        return type.bound(value, lower, upper);
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
     * Determines whether this interval entirely contains another interval.
     *
     * @param that the other interval to test.
     *
     * @return {@code true} iff this interval entirely contains the input
     * interval.
     */
    public boolean contains(DoubleInterval that) {
        return containsLower(that) && containsUpper(that);
    }

    private boolean containsLower(DoubleInterval that) {
        // Open intervals do not contain their lower bounds,
        // so we must also test for equivalent lower bounds...
        return this.contains(that.lower) || this.equalsLower(that);
    }

    private boolean containsUpper(DoubleInterval that) {
        // Open intervals do not contain their upper bounds,
        // so we must also test for equivalent upper bounds...
        return this.contains(that.upper) || this.equalsUpper(that);
    }

    private boolean equalsLower(DoubleInterval that) {
        return this.type.equals(that.type) && DoubleComparator.DEFAULT.equals(this.lower, that.lower);
    }

    private boolean equalsUpper(DoubleInterval that) {
        return this.type.equals(that.type) && DoubleComparator.DEFAULT.equals(this.upper, that.upper);
    }

    /**
     * Writes this interval in a canonical format to a string.
     *
     * @return the canonical representation of this interval.
     */
    public String format() {
        return type.lowerDelim() +
                DECIMAL_FORMAT.format(lower) +
                ", " +
                DECIMAL_FORMAT.format(upper) +
                type.upperDelim();
    }

    /**
     * Returns the midpoint of this interval.
     * @return the midpoint of this interval.
     */
    public double getMidPoint() {
        return 0.5 * (lower + upper);
    }

    /**
     * Returns the width of this interval.
     * @return the width of this interval.
     */
    public double getWidth() {
        return upper - lower;
    }

    /**
     * Identifies intervals with at least one finite bound.
     *
     * @return {@code true} iff at least one bound is finite.
     */
    public boolean isFinite() {
        return Double.NEGATIVE_INFINITY < lower || upper < Double.POSITIVE_INFINITY;
    }

    /**
     * Ensures that this interval is valid.
     *
     * @return this object, for operator chaining.
     *
     * @throws RuntimeException if either bound is missing or if the lower
     * bound exceeds the upper.
     */
    public DoubleInterval validate() {
        if (Double.isNaN(lower) || Double.isNaN(upper) || lower > upper)
            throw new MorpheusException("Invalid interval: [%f, %f].", lower, upper);

        return this;
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
        return equalsLower(that) && equalsUpper(that);
    }

    @Override public String toString() {
        return format();
    }
}
