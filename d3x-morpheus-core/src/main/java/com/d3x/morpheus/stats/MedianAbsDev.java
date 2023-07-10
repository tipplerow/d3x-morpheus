/*
 * Copyright 2018-2023, Talos Trading - All Rights Reserved
 *
 * Licensed under a proprietary end-user agreement issued by D3X Systems.
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.d3xsystems.com/static/eula/quanthub-eula.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Computes the median absolute deviation from the sample median.
 *
 * @author Scott Shaffer
 */
public class MedianAbsDev implements Statistic1 {
    private final double constant;
    private final List<Double> data = new ArrayList<>();

    /**
     * The constant factor that makes the median absolute deviation
     * equal to the standard deviation for a normal distribution.
     */
    public static final double DEFAULT_CONSTANT = 1.4826;

    /**
     * Creates a new statistic with the default constant factor.
     */
    public MedianAbsDev() {
        this(DEFAULT_CONSTANT);
    }

    /**
     * Creates a new statistic with a given constant factor.
     *
     * @param constant the constant factor to multiply the sample median absolute deviation.
     */
    public MedianAbsDev(double constant) {
        this.constant = constant;
    }

    @Override
    public long getN() {
        return data.size();
    }

    @Override
    public double getValue() {
        var median = new Median().compute(data);
        var absdev = data.stream().mapToDouble(x -> Math.abs(x - median));
        return constant * (new Median().compute(absdev));
    }

    @Override
    public StatType getType() {
        return StatType.MEDIAN_ABS_DEV;
    }

    @Override
    public long add(double value) {
        data.add(value);
        return getN();
    }

    @Override
    public Statistic1 copy() {
        var copy = new MedianAbsDev(this.constant);
        copy.data.addAll(this.data);
        return copy;
    }

    @Override
    public Statistic1 reset() {
        data.clear();
        return this;
    }
}
