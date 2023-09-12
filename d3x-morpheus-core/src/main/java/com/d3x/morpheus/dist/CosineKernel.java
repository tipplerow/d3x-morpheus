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
package com.d3x.morpheus.dist;

import com.d3x.morpheus.util.DoubleInterval;

import lombok.NonNull;

import java.util.random.RandomGenerator;

/**
 * Implements the cosine kernel distribution.
 *
 * @author Scott Shaffer
 */
public final class CosineKernel extends AbstractRealDistribution {
    private final double variance = 1.0 - 8.0 / (Math.PI * Math.PI);
    private final double sdev = Math.sqrt(variance);

    private CosineKernel() {}

    /**
     * The single kernel instance.
     */
    public static final CosineKernel INSTANCE = new CosineKernel();

    @Override
    public double cdf(double x) {
        if (x <= -1.0) {
            return 0.0;
        }
        else if (x < 1.0) {
            return 0.5 * (Math.sin(0.5 * Math.PI * x) + 1.0);
        }
        else {
            return 1.0;
        }
    }

    @Override
    public double pdf(double x) {
        if (-1.0 < x && x < 1.0) {
            return 0.25 * Math.PI * Math.cos(0.5 * Math.PI * x);
        }
        else {
            return 0.0;
        }
    }

    @Override
    public double quantile(double F) {
        validateQuantile(F);
        return 2.0 * Math.asin(2.0 * F - 1.0) / Math.PI;
    }

    @Override
    public double mean() {
        return 0.0;
    }

    @Override
    public double median() {
        return 0.0;
    }

    @Override
    public double mode() {
        return 0.0;
    }

    @Override
    public double sdev() {
        return sdev;
    }

    @Override
    public double sample(@NonNull RandomGenerator generator) {
        return transform(generator);
    }

    @Override
    public DoubleInterval support() {
        return DoubleInterval.UNIT;
    }

    @Override
    public double variance() {
        return variance;
    }
}
