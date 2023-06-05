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
package com.d3x.morpheus.filter;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.D3xVectorView;

import lombok.NonNull;

/**
 * Defines one-sided, backward-looking linear convolution filters for
 * univariate time series.
 *
 * <p><b>Filter coefficients:</b> Time-series filters provide a vector
 * of filter coefficients {@code c[0], c[1], ..., c[W - 1]} ordered by
 * time lag, where {@code W} is the window length of the filter.  Given
 * a time series {@code x[0], x[1], ..., x[n]}, the filtered values are
 * {@code y[i] = c[0] * x[i] + c[1] * x[i-1] + ... + c[W-1] * x[i-W+1]}
 * for {@code W - 1 <= i <= n} and {@code y[i] = Double.NaN} for the
 * first {@code 0 <= i < W - 1}.
 *
 * As an example, to implement an exponentially-weighted moving average
 * with a half-life of one day and a lookback window of four days, the
 * filter coefficients should be {@code [1.0, 0.5, 0.25, 0.125]}.
 *
 * <p><b>Bilinear application.</b></p> Time-series filters may also be
 * applied in a bilinear fashion: given two time series {@code x} and
 * {@code y}, the filter generates a new time series {@code z} with
 * {@code z[i] = c[0] * x[i] * y[i] + c[1] * x[i-1] * y[i-1] + ...}.
 *
 * @author Scott Shaffer
 */
public interface TimeSeriesFilter {
    /**
     * Returns a filter coefficient for a given (non-negative) lag.
     *
     * @param lag the non-negative index of the time lag.
     *
     * @return the filter coefficient for the specified lag.
     *
     * @throws RuntimeException if the lag is negative or greater than
     * or equal to the window length.
     */
    double getCoefficient(int lag);

    /**
     * Returns the filter coefficients ordered by time lag.
     * @return the filter coefficients ordered by time lag.
     */
    D3xVectorView getCoefficients();

    /**
     * Returns the length of the filter window (number of coefficients).
     * @return the length of the filter window.
     */
    int getWindowLength();

    /**
     * Identifies filters whose coefficients are positive and sum to one,
     * e.g., for weighted averaging filters.
     *
     * @return {@code true} iff the coefficients of this filter sum to one.
     */
    boolean isNormalized();

    /**
     * Specifies whether to renormalize the normalized coefficients in the
     * presence of missing values.
     *
     * <p>For example, consider a simple moving average filter with length
     * five. The coefficients are {@code [0.2, 0.2, 0.2, 0.2, 0.2]} and the
     * filter is normalized.  When operating on the vector {@code [1.0, 2.0,
     * Double.NaN, 3.0, 4.0]}, the filter produces the result {@code 2.5}
     * instead of {@code Double.NaN} or {@code 2.0} because the coefficients
     * are renormalized to {@code [0.25, 0.25, Double.NaN, 0.25, 0.25].}
     *
     * @return {@code true} iff the coefficients are renormalized in the
     * presence of missing values.
     */
    boolean renormalize();

    /**
     * Applies a filter to a single observation in a univariate time series.
     *
     * @param series a univariate time series (unchanged).
     * @param index  the index of the observation to filter.
     *
     * @return the filtered observation.
     *
     * @throws RuntimeException unless the index is valid given the length
     * of the time series and the size of the filter window.
     */
    double apply(D3xVectorView series, int index);

    /**
     * Applies a filter to a single observation in a univariate time series.
     *
     * @param series  a univariate time series (unchanged).
     * @param index   the index of the observation to filter.
     * @param missing a default value to replace missing values.
     *
     * @return the filtered observation.
     *
     * @throws RuntimeException unless the index is valid given the length
     * of the time series and the size of the filter window.
     */
    double apply(D3xVectorView series, int index, double missing);

    /**
     * Applies this filter to a univariate time series and returns the
     * result in a new vector; the original series is unchanged.
     *
     * @param original the original univariate time series (unchanged).
     * @param truncate whether to remove the first {@code W - 1} missing
     *                 elements from the filtered result, where {@code W}
     *                 is the length of the filter window.
     *
     * @return the filtered time series in a new vector object.
     *
     * @throws RuntimeException unless the length of the original series
     * is greater than or equal to the length of the filter window and all
     * filter coefficients are valid.
     */
    D3xVector apply(D3xVectorView original, boolean truncate);

    /**
     * Applies this filter to a bivariate time series and returns the
     * result in a new vector; the original series are unchanged.
     *
     * @param series1  the first original univariate time series.
     * @param series2  the second original univariate time series.
     * @param truncate whether to remove the first {@code W - 1} missing
     *                 elements from the filtered result, where {@code W}
     *                 is the length of the filter window.
     *
     * @return the filtered time series in a new vector object.
     *
     * @throws RuntimeException unless all filter coefficients are valid
     * and the lengths of the original series are equal to each other and
     * greater than or equal to the length of the filter window.
     */
    D3xVector apply(D3xVectorView series1, D3xVectorView series2, boolean truncate);

    /**
     * Applies this filter to each row of a data frame and returns the
     * result in a new data frame of the same shape. The original data
     * frame is unchanged.
     *
     * @param original a data frame containing a multivariate time series
     *                 organized by row (unchanged).
     * @param truncate whether to remove the first {@code W - 1} missing
     *                 columns from the filtered result, where {@code W}
     *                 is the length of the filter window.
     *
     * @return the filtered time series in a new data frame.
     *
     * @throws RuntimeException unless the lengths of the original series
     * (the number of columns in the data frame) are greater than or equal
     * to the length of the filter window and all filter coefficients are
     * valid.
     */
    <R,C> DataFrame<R,C> byrow(DataFrame<R,C> original, boolean truncate);

    /**
     * Encodes this filter in a string.
     *
     * @return a string encoding of this filter which may be decoded by
     * the filter parser.
     */
    String encode();

    /**
     * Validates the coefficients of this filter.
     *
     * @throws RuntimeException unless this filter is properly defined.
     */
    void validate();

    /**
     * Returns a linear time-series filter with arbitrary coefficients.
     *
     * @param coefficients the filter coefficients.
     *
     * @return a linear filter with the specified coefficients.
     *
     * @throws RuntimeException unless the coefficients are valid.
     */
    static TimeSeriesFilter of(double... coefficients) {
        return new CustomFilter(D3xVectorView.of(coefficients));
    }

    /**
     * Returns a linear time-series filter with arbitrary coefficients.
     *
     * @param coefficients the filter coefficients.
     *
     * @return a linear filter with the specified coefficients.
     *
     * @throws RuntimeException unless the coefficients are valid.
     */
    static TimeSeriesFilter of(@NonNull D3xVectorView coefficients) {
        return new CustomFilter(coefficients);
    }

    /**
     * Returns the finite-difference filter of a given order.
     *
     * @return the finite-difference filter of the specified order.
     *
     * @throws RuntimeException unless the order is supported.
     */
    static TimeSeriesFilter difference(int order) {
        return DifferenceFilter.of(order);
    }

    /**
     * Returns an exponentially-weighted moving-average time-series filter.
     *
     * @param halfLife the half-life for the exponential weight decay.
     * @param window   the number of observations in the moving average.
     *
     * @return an EWMA filter with the specified half-life and window length.
     *
     * @throws RuntimeException unless the window length is positive.
     */
    static TimeSeriesFilter EWMA(double halfLife, int window) {
        return new EWMAFilter(halfLife, window);
    }

    /**
     * Returns a linearly-weighted moving-average time-series filter.
     *
     * @param window the number of observations in the moving average.
     *
     * @return an LWMA filter with the specified window length.
     *
     * @throws RuntimeException unless the window length is positive.
     */
    static TimeSeriesFilter LWMA(int window) {
        return new LWMAFilter(window);
    }

    /**
     * Returns a moving-average time-series filter.
     *
     * @param window the number of observations in the moving average.
     *
     * @return a moving-average time-series filter with the specified
     * window length.
     *
     * @throws RuntimeException unless the window length is positive.
     */
    static TimeSeriesFilter movingAverage(int window) {
        return new MovingAverageFilter(window);
    }

    /**
     * Parses a string containing an encoded time-series filter.
     *
     * @param encoded the encoded string.
     *
     * @return the time-series filter encoded in the given string.
     *
     * @throws RuntimeException unless the string contains a properly
     * formatted time-series filter.
     */
    static TimeSeriesFilter parse(@NonNull String encoded) {
        var parser = new FilterParser(encoded);
        return parser.parse();
    }
}
