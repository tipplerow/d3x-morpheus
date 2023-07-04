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

import com.d3x.morpheus.stats.Mean;
import com.d3x.morpheus.stats.Statistic1;
import com.d3x.morpheus.stats.StdDev;

/**
 * Implements a non-local, size-preserving pipeline that performs signal
 * clipping via Winsorization: the center of the sample is measured by the
 * mean and the width by the standard deviation.
 *
 * @author Scott Shaffer
 */
public class WinsorPipeline extends ClipPipeline {
    /**
     * Creates a new Winsorization pipeline with a given constant.
     *
     * @param clip the (positive) Winsorization clipping constant.
     */
    public WinsorPipeline(double clip) {
        super(clip);
    }

    @Override
    public Statistic1 getCenter() {
        return new Mean();
    }

    @Override
    public Statistic1 getWidth() {
        return new StdDev();
    }

    @Override
    public String encode() {
        return "winsor(" + clip + ")";
    }
}
