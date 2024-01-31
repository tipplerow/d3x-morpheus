/*
 * Copyright 2018-2024, Talos Trading - All Rights Reserved
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
package com.d3x.morpheus.agg;

import com.d3x.morpheus.stats.Statistic1;

import lombok.NonNull;

import java.util.Iterator;

/**
 * Aggregates numeric data using a univariate statistic.
 *
 * @author Scott Shaffer
 */
public final class StatisticAggregator {
    /**
     * Aggregates observations using a univariate statistic.
     *
     * @param iterator  an iterator over the observations to aggregate.
     * @param statistic the univariate statistic to apply.
     * @param nanPolicy the missing value policy to apply.
     *
     * @return the aggregated value.
     */
    public static double aggregate(@NonNull Iterator<Double> iterator,
                                   @NonNull Statistic1 statistic,
                                   @NonNull NanPolicy nanPolicy) {
        var failOnNaN = nanPolicy.getType().equals(NanPolicy.Type.NAN);

        while (iterator.hasNext ()) {
            var value = nanPolicy.apply(iterator.next());

            if (!Double.isNaN(value)) {
                statistic.add(value);
            }
            else if (failOnNaN) {
                // Any missing value causes the entire aggregate to be missing...
                return Double.NaN;
            }
        }

        if (statistic.getN() < nanPolicy.getMinObs()) {
            return Double.NaN;
        }
        else {
            return statistic.getValue();
        }
    }
}
