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
package com.d3x.morpheus.filter;

import com.d3x.morpheus.util.DoubleUtil;
import com.d3x.morpheus.vector.D3xVector;

/**
 * Implements a linearly-weighted moving average as a time-series filter:
 * observation weights decrease linearly with time.
 *
 * @author Scott Shaffer
 */
public class LWMAFilter extends CustomFilter {
    /**
     * Creates an LWMA filter with a given window length.
     *
     * @param window the number of observations in the averaging period.
     *
     * @throws RuntimeException unless the window length is positive.
     */
    public LWMAFilter(int window) {
        super(computeWeights(window));
    }

    /**
     * The time-series name for string encoding.
     */
    public static final String NAME = "lwma";

    /**
     * Computes the observation weights for a linearly-weighted moving
     * average.
     *
     * @param window the number of observations in the averaging period.
     *
     * @return the observation weights for the specified window length.
     */
    public static D3xVector computeWeights(int window) {
        validateWindow(window);
        var weights = D3xVector.dense(window);

        for (int lag = 0; lag < window; ++lag)
            weights.set(lag, computeWeight(window, lag));

        weights.normalize();
        return weights;
    }

    private static double computeWeight(int window, int lag) {
        return 1.0 - DoubleUtil.ratio(lag, window);
    }

    @Override
    public String encodeArgs() {
        return Integer.toString(getWindowLength());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
