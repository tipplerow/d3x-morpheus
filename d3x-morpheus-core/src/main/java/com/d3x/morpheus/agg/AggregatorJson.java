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

/**
 * Defines attribute names for the JSON representation of vector aggregators.
 *
 * @author Scott Shaffer
 */
public interface AggregatorJson {
    /**
     * The JSON key for the type of the aggregator.
     */
    String AGG_TYPE = "aggType";

    /**
     * The JSON key for the components of a composite aggregator.
     */
    String COMPONENTS = "components";

    /**
     * The JSON key for the compositor of a composite aggregator.
     */
    String COMPOSITOR = "compositor";

    /**
     * The JSON key for the half-life of the EWMA aggregator.
     */
    String HALF_LIFE = "halfLife";

    /**
     * The JSON key for the missing value policy of the aggregator.
     */
    String NAN_POLICY = "nanPolicy";

    /**
     * The JSON key for the renormalize parameter of the aggregator.
     */
    String RENORMALIZE = "renormalize";

    /**
     * The JSON key for the window length of the aggregator.
     */
    String WINDOW_LEN = "windowLen";
}
