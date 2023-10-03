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
 * Implements the Epanechnikov (parabolic) kernel distribution.
 *
 * @author Scott Shaffer
 */
public final class EpanechnikovKernel extends AbstractRealDistribution {
    private final double sdev = Math.sqrt(0.2);

    private EpanechnikovKernel() {}

    /**
     * The single kernel instance.
     */
    public static final EpanechnikovKernel INSTANCE = new EpanechnikovKernel();

    @Override
    public double cdf(double x) {
        if (x <= -1.0) {
            return 0.0;
        }
        else if (x < 1.0) {
            return 0.5 + x * (0.75 - 0.25 * x * x);
        }
        else {
            return 1.0;
        }
    }

    @Override
    public double pdf(double x) {
        if (-1.0 < x && x < 1.0) {
            return 0.75 * (1.0 - x * x);
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
        // Devroye and Gyorfi, "Nonparametric Density Estimation: The L1 View"
        var U1 = generator.nextDouble(-1.0, 1.0);
        var U2 = generator.nextDouble(-1.0, 1.0);
        var U3 = generator.nextDouble(-1.0, 1.0);

        var absU1 = Math.abs(U1);
        var absU2 = Math.abs(U2);
        var absU3 = Math.abs(U3);

        return (absU3 >= absU1 && absU3 >= absU2) ? U2 : U3;
    }

    @Override
    public DoubleInterval support() {
        return DoubleInterval.UNIT;
    }

    @Override
    public double variance() {
        return 0.2;
    }
}
