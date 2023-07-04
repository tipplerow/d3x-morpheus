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
package com.d3x.morpheus.pipeline;

import com.d3x.morpheus.stats.Statistic1;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;
import com.d3x.morpheus.vector.DataVector;
import lombok.Getter;

/**
 * Implements non-local, size-preserving pipelines that perform signal
 * <em>clipping</em>.
 *
 * <p>Elements are bound between {@code center - clip * width} and
 * {@code center + clip * width}, where {@code center} is a measure
 * of the sample center (mean or median), {@code width} is a measure
 * of the sample width (standard or median absolute deviation) and
 * {@code clip} is the clipping constant.</p>
 *
 * @author Scott Shaffer
 */
public abstract class ClipPipeline extends AbstractDataPipeline {
    /**
     * The clipping constant.
     */
    @Getter
    protected final double clip;

    /**
     * Creates a new clipping pipeline with a given constant.
     *
     * @param clip the (positive) clipping constant.
     */
    protected ClipPipeline(double clip) {
        if (clip <= 0.0)
            throw new MorpheusException("Clipping constant must be positive.");

        this.clip = clip;
    }

    /**
     * Returns the statistic that measures the center of the sample.
     * @return the statistic that measures the center of the sample.
     */
    public abstract Statistic1 getCenter();

    /**
     * Returns the statistic that measures the width of the sample.
     * @return the statistic that measures the width of the sample.
     */
    public abstract Statistic1 getWidth();

    @Override
    public <K> DataVector<K> apply(DataVector<K> vector) {
        var width = getWidth().compute(vector);
        var center = getCenter().compute(vector);
        return getBound(center, width).apply(vector);
    }

    @Override
    public D3xVector apply(D3xVector vector) {
        var width = getWidth().compute(vector);
        var center = getCenter().compute(vector);
        return getBound(center, width).apply(vector);
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isSizePreserving() {
        return true;
    }

    private DataPipeline getBound(double center, double width) {
        var lower = center - clip * width;
        var upper = center + clip * width;
        return DataPipeline.bound(lower, upper);
    }
}
