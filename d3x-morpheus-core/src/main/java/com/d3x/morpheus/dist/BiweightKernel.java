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

import java.util.random.RandomGenerator;

/**
 * Implements the biweight (quartic) kernel distribution.
 *
 * @author Scott Shaffer
 */
public final class BiweightKernel extends AbstractRealDistribution {
    private final double variance = 1.0 / 7.0;
    private final double sdev = Math.sqrt(variance);

    private BiweightKernel() {}

    /**
     * The single kernel instance.
     */
    public static final BiweightKernel INSTANCE = new BiweightKernel();

    @Override
    public double cdf(double x) {
        if (x <= -1.0) {
            return 0.0;
        }
        else if (x < 1.0) {
            var x2 = x * x;
            return 0.5 + x * (0.9375 - x2 * (0.625 - 0.1875 * x2));
        }
        else {
            return 1.0;
        }
    }

    @Override
    public double pdf(double x) {
        if (-1.0 < x && x < 1.0) {
            var weight = 1.0 - x * x;
            return 0.9375 * weight * weight;
        }
        else {
            return 0.0;
        }
    }

    @Override
    public double quantile(double F) {
        return invertCDF(F);
    }

    @Override
    public double mean() {
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
    public double sample(RandomGenerator generator) {
        return reject(generator);
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
