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

import lombok.NonNull;

import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

/**
 * Provides a base class for discrete distribution implementations.
 *
 * @author Scott Shaffer
 */
public abstract class AbstractDiscreteDistribution implements DiscreteDistribution {
    @Override
    public double cdf(@NonNull IntSupport range) {
        return cdf(range.getUpper()) - cdf(range.getLower() - 1);
    }

    @Override
    public int[] sample(@NonNull RandomGenerator generator, int count) {
        var samples = new int[count];

        for (int index = 0; index < count; ++index)
            samples[index] = sample(generator);

        return samples;
    }

    @Override
    public IntStream stream(@NonNull RandomGenerator generator, int count) {
        return IntStream.generate(() -> sample(generator)).limit(count);
    }

    @Override
    public double variance() {
        var sd = sdev();
        return sd * sd;
    }
}
