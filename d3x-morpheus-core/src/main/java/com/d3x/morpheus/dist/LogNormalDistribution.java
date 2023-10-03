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

import lombok.Getter;
import org.apache.commons.math3.special.Erf;

import java.util.random.RandomGenerator;

/**
 * Implements the log-normal probability distribution.
 *
 * @author Scott Shaffer
 */
public class LogNormalDistribution extends AbstractRealDistribution {
    /**
     * The mean of the underlying normal distribution.
     */
    @Getter
    private final double mu;

    /**
     * The standard deviation of the underlying normal distribution.
     */
    @Getter
    private final double sigma;

    private final double mean;
    private final double mode;
    private final double sdev;
    private final double median;
    private final double variance;

    private final double Q; // Pre-computed constant in the quantile function
    private final double C; // Pre-computed constant in the CDF
    private final double P1; // Pre-computed constant term in the PDF
    private final double P2; // Pre-computed constant term in the PDF

    /**
     * Creates a new log-normal distribution with fixed parameters.
     *
     * @param mu    the mean of the underlying normal distribution.
     * @param sigma the standard deviation of the underlying normal distribution.
     */
    public LogNormalDistribution(double mu, double sigma) {
        validateSD(sigma);
        this.mu = mu;
        this.sigma = sigma;

        var sigma2 = sigma * sigma;
        this.mean = Math.exp(mu + 0.5 * sigma2);
        this.mode = Math.exp(mu - sigma2);
        this.median = Math.exp(mu);
        this.variance = (Math.exp(sigma2) - 1.0) * Math.exp(2.0 * mu + sigma2);
        this.sdev = Math.sqrt(variance);

        this.Q = sigma * Math.sqrt(2.0);
        this.C = 1.0 / Q;
        this.P1 = 1.0 / (sigma * Math.sqrt(2.0 * Math.PI));
        this.P2 = -0.5 / sigma2;
    }

    @Override
    public double cdf(double x) {
        return 0.5 * (1.0 + Erf.erf(C * (Math.log(x) - mu)));
    }

    @Override
    public double pdf(double x) {
        if (x <= 0.0) {
            return 0.0;
        }
        else {
            var y = Math.log(x) - mu;
            return P1 * Math.exp(P2 * y * y) / x;
        }
    }

    @Override
    public double quantile(double F) {
        return Math.exp(mu + Q * Erf.erfInv(2.0 * F - 1.0));
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double median() {
        return median;
    }

    @Override
    public double mode() {
        return mode;
    }

    @Override
    public double sdev() {
        return sdev;
    }

    @Override
    public double sample(RandomGenerator generator) {
        return Math.exp(generator.nextGaussian(mu, sigma));
    }

    @Override
    public DoubleInterval support() {
        return DoubleInterval.POSITIVE;
    }

    @Override
    public double variance() {
        return variance;
    }
}
