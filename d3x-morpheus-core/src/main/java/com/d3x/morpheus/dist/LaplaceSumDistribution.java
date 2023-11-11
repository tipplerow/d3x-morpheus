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
import lombok.NonNull;

import org.apache.commons.math3.special.Gamma;

import java.util.Arrays;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * Implements the probability distribution for the sum of independent and
 * identically distributed (IID) Laplace random variables.
 *
 * <p>The analytical formula for the PDF is given in "The Laplace Distribution
 * and Generalizations, A Revisit with New Applications", by Kotz, Kozubowski,
 * and Podgorski.
 *
 * @author Scott Shaffer
 */
public final class LaplaceSumDistribution extends NumericalRealDistribution {
    /**
     * The parent Laplace distribution.
     */
    @Getter
    @NonNull
    private final LaplaceDistribution parent;

    /**
     * The number of IID Laplace variables in the sum.
     */
    @Getter
    private final int count;

    // Pre-computed mean and standard deviation...
    private final double mean;
    private final double sdev;

    // PDF in the limit z -> 0...
    private final double smallZ;

    // Coefficients for the PDF series...
    private final double[] coeffs;

    public LaplaceSumDistribution(@NonNull LaplaceDistribution parent, int count) {
        if (count < 1)
            throw new IllegalArgumentException("Count must be positive.");

        this.count = count;
        this.parent = parent;

        this.mean = count * parent.mean();
        this.sdev = Math.sqrt(count) * parent.sdev();

        this.smallZ = smallZ();
        this.coeffs = IntStream.range(0, count).mapToDouble(this::coeff).toArray();
    }

    @Override
    public double pdf(double x) {
        double b = parent.getScale();
        double z = Math.abs(x - mean) / b;
        return pdfz(z) / b;
    }

    @Override
    public double quantile(double F) {
        return invertCDF(F);
    }

    @Override
    public double mean() {
        return mean;
    }

    @Override
    public double median() {
        return mean;
    }

    @Override
    public double mode() {
        return mean;
    }

    @Override
    public double sdev() {
        return sdev;
    }

    @Override
    public double sample(@NonNull RandomGenerator generator) {
        return Arrays.stream(parent.sample(generator, count)).sum();
    }

    @Override
    public DoubleInterval support() {
        return DoubleInterval.INFINITE;
    }

    private double pdfz(double z) {
        if (z < 1.0E-04) {
            return smallZ;
        }
        else {
            var sum = 0.0;

            for (int j = 0; j < count; ++j) {
                sum += coeffs[j] * Math.pow(z, count - 1 - j);
            }

            return sum * Math.exp(-z);
        }
    }

    private double coeff(int j) {
        // Coefficients of the PDF series defined by Equation (2.3.25) of
        // Kotz, Kozubowski, and Podgorski, with the components assembled
        // as logarithms to avoid over/underflow of the gamma function...
        var n = count;
        var logC = logFac(n - 1 + j)
                - logFac(n - 1)
                - logFac(n - 1 - j)
                - logFac(j)
                - (n + j) * Math.log(2.0);
        return Math.exp(logC);
    }

    private double smallZ() {
        // From Kotz, Kozubowski, and Podgorski, the PDF is:
        //
        //     p(z) = (z/2) ^ (n - 1/2) * K(z; n - 1/2) / (gamma(n) * sqrt(pi)),
        //
        // where "n" is the number of variables in the sum (count), gamma(.)
        // is the Gamma function, and K(.; n - 1/2) is the modified Bessel
        // function of the third kind of order n - 1/2.  Abromowitz and Stegun
        // give the small-z limit:
        //
        //     K(z, n) -> gamma(n) * (z/2)^(-n) / 2 as z -> 0.
        //
        // Upon substitution:
        //
        //     p(z) -> gamma(n - 1/2) / (2 * gamma(n) * sqrt(pi))
        //
        // Since gamma(n)
        //
        //     log(p(z)) -> logGamma(n - 1/2) - logGamma(n) - log(2 * sqrt(pi))
        //
        return Math.exp(Gamma.logGamma(count - 0.5) - Gamma.logGamma(count) - Math.log(2.0 * Math.sqrt(Math.PI)));
    }

    private static double logFac(int k) {
        // The logarithm of k factorial...
        return Gamma.logGamma(k + 1);
    }
}
