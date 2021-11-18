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

import java.util.List;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.util.DoubleComparator;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.D3xVectorView;

/**
 * Provides a partial implementation of the TimeSeriesFilter interface.
 * 
 * @author Scott Shaffer
 */
public abstract class AbstractFilter implements TimeSeriesFilter {
    /**
     * Ensures that a filter coefficient is valid.
     *
     * @param coefficient the filter coefficient to validate.
     *
     * @throws RuntimeException unless the coefficient is finite.
     */
    static void validateCoefficient(double coefficient) {
        if (!Double.isFinite(coefficient))
            throw new MorpheusException("Filter coefficients must be finite.");
    }

    /**
     * Ensures that filter coefficients are valid.
     *
     * @param coefficients the filter coefficients to validate.
     *
     * @throws RuntimeException unless the vector contains at least one
     * coefficient and all coefficients are finite.
     */
    static void validateCoefficients(D3xVectorView coefficients) {
        if (coefficients.length() < 1)
            throw new MorpheusException("At least one filter coefficient is required.");

        for (double coeff : coefficients)
            validateCoefficient(coeff);
    }

    /**
     * Ensures that a window length is positive.
     *
     * @param window the window length to validate.
     *
     * @throws RuntimeException unless the window length is positive.
     */
    static void validateWindow(int window) {
        if (window < 1)
            throw new MorpheusException("A time-series window length must be positive.");
    }

    /**
     * Ensures that a lag index is valid.
     *
     * @param lag the non-negative index of the time lag.
     *
     * @throws RuntimeException unless the lag is in the valid range
     * {@code 0 <= lag < W}, where {@code W} is the window length of
     * this filter.
     */
    public void validateLag(int lag) {
        if (lag < 0 || lag >= getWindowLength())
            throw new MorpheusException(
                    "Time-series lag [%d] is outside the valid range [0, %d].",
                    lag, getWindowLength());
    }

    /**
     * Ensures that an original time series has a length greater than or
     * equal to the size of the filter window.
     *
     * @param original the original time series to validate.
     *
     * @throws RuntimeException unless the length of the original series
     * is greater than or equal to the size of the filter window.
     */
    public void validateOriginal(D3xVectorView original) {
        if (original.length() < getWindowLength())
            throw new MorpheusException("The original time series is shorter than the filter length.");
    }

    /**
     * Returns the filter name for string encoding.
     * @return the filter name for string encoding.
     */
    public abstract String getName();

    /**
     * Encodes the arguments required to create this filter.
     *
     * @return the arguments required to create this filter encoded
     * into a string.
     */
    public abstract String encodeArgs();

    @Override
    public double apply(D3xVectorView series, int index) {
        return apply(series, index, Double.NaN);
    }

    @Override
    public double apply(D3xVectorView series, int index, double missing) {
        double result = 0.0;

        for (int lag = 0; lag < getWindowLength(); ++lag) {
            double coeff = getCoefficient(lag);
            double value = series.get(index - lag);

            if (Double.isNaN(value))
                value = missing;

            result += coeff * value;
        }

        return result;
    }

    @Override
    public D3xVector apply(D3xVectorView original, boolean truncate) {
        validateOriginal(original);

        var filtered = createFiltered(original.length(), truncate);
        applyInPlace(original, filtered, truncate);

        return filtered;
    }

    private D3xVector createFiltered(int origLen, boolean truncate) {
        var filteredLength = getFilteredLength(origLen, truncate);
        return D3xVector.dense(filteredLength);
    }

    private int getFilteredLength(int origLen, boolean truncate) {
        if (truncate)
            return origLen - getWindowLength() + 1;
        else
            return origLen;
    }

    private void applyInPlace(D3xVectorView original, D3xVector filtered, boolean truncate) {
        var filteredIndex = 0;
        var windowLength = getWindowLength();

        if (!truncate) {
            while (filteredIndex < windowLength - 1) {
                filtered.set(filteredIndex, Double.NaN);
                ++filteredIndex;
            }
        }

        var originalIndex = windowLength - 1;
        var originalLength = original.length();

        while (originalIndex < originalLength) {
            filtered.set(filteredIndex, apply(original, originalIndex));
            ++originalIndex;
            ++filteredIndex;
        }
    }

    @Override
    public D3xVector apply(D3xVectorView series1, D3xVectorView series2, boolean truncate) {
        if (series1.length() == series2.length())
            return apply(D3xVectorView.multiplyEBE(series1, series2), truncate);
        else
            throw new MorpheusException("The time series have different lengths.");
    }

    @Override
    public <R,C> DataFrame<R,C> byrow(DataFrame<R,C> original, boolean truncate) {
        var rowKeys = original.listRowKeys();
        var colKeys = getFilteredKeys(original.listColumnKeys(), truncate);
        var filtered = DataFrame.ofDoubles(rowKeys, colKeys);

        for (int rowIndex = 0; rowIndex < original.rowCount(); ++rowIndex) {
            var originalRow = D3xVector.wrap(original.rowAt(rowIndex));
            var filteredRow = D3xVector.wrap(filtered.rowAt(rowIndex));
            applyInPlace(originalRow, filteredRow, truncate);
        }

        return filtered;
    }

    private <K> List<K> getFilteredKeys(List<K> originalKeys, boolean truncate) {
        if (truncate)
            return originalKeys.subList(getWindowLength() - 1, originalKeys.size());
        else
            return originalKeys;
    }

    @Override
    public String encode() {
        return getName() + "(" + encodeArgs() + ")";
    }

    @Override
    public D3xVectorView getCoefficients() {
        var window = getWindowLength();
        var coeffs = D3xVector.dense(window);

        for (int lag = 0; lag < window; ++lag)
            coeffs.set(lag, getCoefficient(lag));

        return coeffs;
    }

    @Override
    public boolean isNormalized() {
        var coeffSum = getCoefficients().sum();
        return DoubleComparator.DEFAULT.equals(coeffSum, 1.0);
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other.getClass().equals(this.getClass()) && equalsFilter((TimeSeriesFilter) other);
    }

    private boolean equalsFilter(TimeSeriesFilter that) {
        return this.getCoefficients().equalsView(that.getCoefficients());
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Time-series filters may not be used as hash keys.");
    }

    @Override
    public String toString() {
        return encode();
    }
}
