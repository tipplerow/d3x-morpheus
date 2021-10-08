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

/**
 * Enumerates the types of real intervals represented by Morpheus.
 *
 * @author Scott Shaffer
 */
public enum DoubleIntervalType {
    /**
     * Both left and right limits are closed: {@code [a, b]}.
     */
    CLOSED {
        @Override
        public boolean contains(double value, double lower, double upper) {
            return comparator.compare(value, lower) >= 0
                && comparator.compare(value, upper) <= 0;
        }

        @Override
        public char lowerDelim() {
            return LOWER_CLOSED_DELIM;
        }

        @Override
        public char upperDelim() {
            return UPPER_CLOSED_DELIM;
        }
    },

    /**
     * The left limit is closed, the right is open: {@code [a, b)}.
     */
    LEFT_CLOSED  {
        @Override
        public boolean contains(double value, double lower, double upper) {
            return comparator.compare(value, lower) >= 0
                && comparator.compare(value, upper) <  0;
        }

        @Override
        public char lowerDelim() {
            return LOWER_CLOSED_DELIM;
        }

        @Override
        public char upperDelim() {
            return UPPER_OPEN_DELIM;
        }
    },

    /**
     * The left limit is open, the right is closed: {@code (a, b]}.
     */
    LEFT_OPEN {
        @Override
        public boolean contains(double value, double lower, double upper) {
            return comparator.compare(value, lower) >  0
                && comparator.compare(value, upper) <= 0;
        }

        @Override
        public char lowerDelim() {
            return LOWER_OPEN_DELIM;
        }

        @Override
        public char upperDelim() {
            return UPPER_CLOSED_DELIM;
        }
    },

    /**
     * Both left and right limits are open: {@code (a, b)}.
     */
    OPEN {
        @Override
        public boolean contains(double value, double lower, double upper) {
            return comparator.compare(value, lower) > 0
                && comparator.compare(value, upper) < 0;
        }

        @Override
        public char lowerDelim() {
            return LOWER_OPEN_DELIM;
        }

        @Override
        public char upperDelim() {
            return UPPER_OPEN_DELIM;
        }
    };

    private static final char LOWER_CLOSED_DELIM = '[';
    private static final char LOWER_OPEN_DELIM   = '(';
    private static final char UPPER_CLOSED_DELIM = ']';
    private static final char UPPER_OPEN_DELIM   = ')';

    private static final DoubleComparator comparator = DoubleComparator.DEFAULT;

    /**
     * Determines whether an interval of this type contains a given value.
     *
     * @param value the value to test.
     * @param lower the lower bound of the interval.
     * @param upper the upper bound of the interval.
     *
     * @return {@code true} iff an interval of this type contains the value.
     */
    public abstract boolean contains(double value, double lower, double upper);

    /**
     * Returns the delimiter for the lower limit on intervals of this type.
     * @return the delimiter for the lower limit on intervals of this type.
     */
    public abstract char lowerDelim();

    /**
     * Returns the delimiter for the upper limit on intervals of this type.
     * @return the delimiter for the upper limit on intervals of this type.
     */
    public abstract char upperDelim();

    /**
     * Constructs the string representation for an interval of this type.
     *
     * @param lower the lower bound of the interval.
     * @param upper the upper bound of the interval.
     *
     * @return the string representation of the interval.
     */
    public String format(double lower, double upper) {
        return String.format("%c%f, %f%c", lowerDelim(), lower, upper, upperDelim());
    }
}
