/*
 * Copyright (C) 2014-2022 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.filter;

import java.util.regex.Pattern;

import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVectorView;

import lombok.Getter;
import lombok.NonNull;

/**
 * Parses strings that encode time-series filters.
 *
 * @author Scott Shaffer
 */
public final class FilterParser {
    /**
     * The string that encodes a time-series.
     */
    @Getter
    private final String encoded;

    private String filterName;
    private String[] filterArgs;

    private static final Pattern FILTER_PATTERN = Pattern.compile("(\\w+)\\((.*)\\)");

    /**
     * The delimiter for filter constructor arguments.
     */
    public static final char ARG_DELIM = ',';

    /**
     * Creates a new parser for a given encoded string.
     *
     * @param encoded the encoded string.
     */
    public FilterParser(String encoded) {
        this.encoded = encoded;
    }

    /**
     * Parses the encoded string.
     *
     * @return the time-series filter encoded by the string.
     */
    public TimeSeriesFilter parse() {
        match();
        return build();
    }

    private void match() {
        var matcher = FILTER_PATTERN.matcher(encoded.strip());

        if (!matcher.matches())
            throw invalidException();

        if (matcher.groupCount() != 2)
            throw invalidException();

        filterName = matcher.group(1);
        filterArgs = matcher.group(2).split(String.valueOf(ARG_DELIM));
    }

    private TimeSeriesFilter build() {
        switch (filterName) {
            case CustomFilter.NAME:
                return parseCustom();

            case DifferenceFilter.NAME:
                return parseDifference();

            case EWMAFilter.NAME:
                return parseEWMA();

            case MovingAverageFilter.NAME:
                return parseMovingAverage();

            default:
                throw new MorpheusException("Unknown filter name: [%s].", filterName);
        }
    }

    private RuntimeException invalidException() {
        return new MorpheusException("Invalid time-series filter: [%s].", encoded);
    }

    private TimeSeriesFilter parseCustom() {
        if (filterArgs.length < 1)
            throw invalidException();

        var coeffs = new double[filterArgs.length];

        for (int index = 0; index < filterArgs.length; ++index)
            coeffs[index] = parseDouble(filterArgs[index]);

        return new CustomFilter(D3xVectorView.of(coeffs));
    }

    private TimeSeriesFilter parseDifference() {
        if (filterArgs.length != 1)
            throw invalidException();

        var order = parseInt(filterArgs[0]);
        return DifferenceFilter.of(order);
    }

    private TimeSeriesFilter parseEWMA() {
        if (filterArgs.length != 2)
            throw invalidException();

        var halfLife = parseDouble(filterArgs[0]);
        var window = parseInt(filterArgs[1]);
        return new EWMAFilter(halfLife, window);
    }

    private TimeSeriesFilter parseMovingAverage() {
        if (filterArgs.length != 1)
            throw invalidException();

        var window = parseInt(filterArgs[0]);
        return new MovingAverageFilter(window);
    }

    private double parseDouble(String str) {
        str = str.strip();

        try {
            return Double.parseDouble(str);
        }
        catch (RuntimeException ex) {
            throw new MorpheusException("Invalid double [%s] in time-series filter [%s].", str, encoded);
        }
    }

    private int parseInt(String str) {
        str = str.strip();

        try {
            return Integer.parseInt(str);
        }
        catch (RuntimeException ex) {
            throw new MorpheusException("Invalid integer [%s] in time-series filter [%s].", str, encoded);
        }
    }
}
