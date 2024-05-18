/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.stats;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVectorView;
import com.d3x.morpheus.vector.DataVectorView;

/**
 * An interface that defines an incremental calculation of a uni-variate statistic.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public interface Statistic1 extends Statistic {
    /**
     * Creates a new instance of a univariate statistic.
     *
     * @param className either the simple or fully qualified class name: either
     *                  {@code Median} or {@code com.d3x.morpheus.stats.Median}
     *                  are acceptable.
     *
     * @return a new instance of the specified statistic.
     */
    static Statistic1 newInstance(String className) {
        var packagePrefix = "com.d3x.morpheus.stats.";

        if (!className.startsWith(packagePrefix))
            className = packagePrefix + className;

        try {
            var classObj = Class.forName(className);
            return (Statistic1) classObj.getConstructor().newInstance();
        }
        catch (Exception ex) {
            throw new MorpheusException(ex.getMessage());
        }
    }

    /**
     * Adds a new value to the sample for this statistic
     * @param value     the value to add
     * @return          the sample size after adding value
     */
    long add(double value);

    /**
     * Adds new values to the sample for this statistic.
     *
     * @param values the values to add.
     *
     * @return the sample size after adding the values.
     */
    default long add(D3xVectorView values) {
        for (double value : values)
            add(value);

        return getN();
    }

    /**
     * Adds new values to the sample for this statistic.
     *
     * @param vector a vector of values to add.
     *
     * @return the sample size after adding the values.
     */
    default long add(DataVectorView<?> vector) {
        return add(vector.streamValues());
    }

    /**
     * Adds new values to the sample for this statistic.
     *
     * @param stream a stream of values to add.
     *
     * @return the sample size after adding the values.
     */
    default long add(DoubleStream stream) {
        stream.forEach(this::add);
        return getN();
    }

    /**
     * Adds new values to the sample for this statistic.
     *
     * @param values a list of values to add.
     *
     * @return the sample size after adding the values.
     */
    default long add(List<Double> values) {
        return add(values.stream().mapToDouble(x -> x));
    }

    /**
     * Adds new values to the sample for this statistic.
     *
     * @param frame a frame of values to add.
     *
     * @return the sample size after adding the values.
     */
    default long add(DataFrame<?, ?> frame) {
        return add(frame.values().mapToDouble(DataFrameValue::getDouble));
    }

    /**
     * Resets this statistic and computes the value for a given sample.
     *
     * @param sample the sample of values.
     *
     * @return the value of this statistic for the specified sample.
     */
    default double compute(D3xVectorView sample) {
        return compute(this, sample);
    }

    /**
     * Resets this statistic and computes the value for a given sample.
     *
     * @param sample the sample of values.
     *
     * @return the value of this statistic for the specified sample.
     */
    default double compute(DataVectorView<?> sample) {
        return compute(this, sample);
    }

    /**
     * Resets this statistic and computes the value for a given sample.
     *
     * @param sample the sample of values.
     *
     * @return the value of this statistic for the specified sample.
     */
    default double compute(DoubleStream sample) {
        return compute(this, sample);
    }

    /**
     * Resets this statistic and computes the value for a given sample.
     *
     * @param sample the sample of values.
     *
     * @return the value of this statistic for the specified sample.
     */
    default double compute(List<Double> sample) {
        return compute(this, sample);
    }

    /**
     * Resets this statistic and computes the value for a given sample.
     *
     * @param sample the sample of values.
     *
     * @return the value of this statistic for the specified sample.
     */
    default double compute(DataFrame<?, ?> sample) {
        return compute(this, sample);
    }

    /**
     * Returns a copy of this statistic
     * @return  a copy of this object
     */
    Statistic1 copy();

    /**
     * Resets this statistic back to initial state
     * @return  this statistic
     */
    Statistic1 reset();

    /**
     * Computes a univariate statistic over a given sample.
     *
     * @param stat      the statistic type
     * @param sample    the sample of values
     *
     * @return          the value of the statistic for the given sample.
     */
    static double compute(Statistic1 stat, D3xVectorView sample) {
        stat.reset();
        stat.add(sample);
        return stat.getValue();
    }

    /**
     * Computes a univariate statistic over a given sample.
     *
     * @param stat      the statistic type
     * @param sample    the sample of values
     *
     * @return          the value of the statistic for the given sample.
     */
    static double compute(Statistic1 stat, DataVectorView<?> sample) {
        stat.reset();
        stat.add(sample);
        return stat.getValue();
    }

    /**
     * Computes a univariate statistic over a given sample.
     *
     * @param stat      the statistic type
     * @param sample    the sample of values
     *
     * @return          the value of the statistic for the given sample.
     */
    static double compute(Statistic1 stat, DoubleStream sample) {
        stat.reset();
        stat.add(sample);
        return stat.getValue();
    }

    /**
     * Computes a univariate statistic over a given sample.
     *
     * @param stat      the statistic type
     * @param sample    the sample of values
     *
     * @return          the value of the statistic for the given sample.
     */
    static double compute(Statistic1 stat, List<Double> sample) {
        return compute(stat, sample.stream().mapToDouble(x -> x));
    }

    /**
     * Computes a univariate statistic over a given sample.
     *
     * @param stat      the statistic type
     * @param sample    the sample of values
     *
     * @return          the value of the statistic for the given sample.
     */
    static double compute(Statistic1 stat, DataFrame<?, ?> sample) {
        stat.reset();
        stat.add(sample);
        return stat.getValue();
    }

    /**
     * Computes a univariate statistic over a given sample.
     *
     * @param stat      a supplier of the statistic type
     * @param sample    the sample of values
     *
     * @return          the value of the statistic for the given sample.
     */
    static double compute(Supplier<Statistic1> stat, double... sample) {
        return compute(stat.get(), D3xVectorView.of(sample));
    }

    /**
     * Convenience function to compute a univariate statistic on some sample
     * @param stat      the statistic type
     * @param sample    the sample of values
     * @param offset    the offset in sample
     * @param length    the length from offset
     * @return          the stat value
     */
    static double compute(Statistic1 stat, Sample sample, int offset, int length) {
        stat.reset();
        for (int i=0; i<length; ++i) {
            final int index = offset + i;
            final double value = sample.getDoubleAt(index);
            stat.add(value);
        }
        return stat.getValue();
    }
}
