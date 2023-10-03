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

import com.d3x.morpheus.stats.StatSummary;

import lombok.NonNull;

/**
 * Enumerates kernel density functions.
 *
 * @author Scott Shaffer
 */
public enum KernelType {
    /**
     * The uniform (boxcar) kernel.
     */
    UNIFORM {
        @Override
        public RealDistribution kernel() {
            return UniformDistribution.KERNEL;
        }
    },

    /**
     * The triangular kernel.
     */
    TRIANGULAR {
        @Override
        public RealDistribution kernel() {
            return TriangularDistribution.KERNEL;
        }
    },

    /**
     * The Epanechnikov (parabolic) kernel.
     */
    EPANECHNIKOV {
        @Override
        public RealDistribution kernel() {
            return EpanechnikovKernel.INSTANCE;
        }
    },

    /**
     * The biweight (quartic) kernel.
     */
    BIWEIGHT {
        @Override
        public RealDistribution kernel() {
            return BiweightKernel.INSTANCE;
        }
    },

    /**
     * The cosine kernel.
     */
    COSINE {
        @Override
        public RealDistribution kernel() {
            return CosineKernel.INSTANCE;
        }
    },

    /**
     * The normal (Gaussian) kernel.
     */
    GAUSSIAN {
        @Override
        public RealDistribution kernel() {
            return NormalDistribution.STANDARD;
        }
    };

    /**
     * Returns the kernel distribution.
     * @return the kernel distribution.
     */
    public abstract RealDistribution kernel();

    /**
     * Computes the default bandwidth for a given sample.
     *
     * @param sample the empirical data sample.
     *
     * @return the default bandwidth for the given sample.
     */
    public double bandwidth(double[] sample) {
        return bandwidth(StatSummary.of(sample));
    }

    /**
     * Computes the default bandwidth for a given sample.
     *
     * @param summary the empirical data summary.
     *
     * @return the default bandwidth for the given sample.
     */
    public double bandwidth(@NonNull StatSummary summary) {
        // Silverman's rule of thumb...
        var SD = summary.getSD();
        var IQR = summary.getIQR();
        return 0.9 * Math.min(SD, IQR / 1.34) * Math.pow(summary.getCount(), -0.2);
    }
}
