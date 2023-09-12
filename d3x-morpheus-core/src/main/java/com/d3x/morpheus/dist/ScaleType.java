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

/**
 * Enumerates the methods to specify the width or scale of a probability
 * distribution.
 *
 * @author Scott Shaffer
 */
public enum ScaleType {
    /**
     * The scale is specified in the native form of the distribution
     * functions, e.g., the rate for an exponential distribution or
     * the {@code b} parameter for a Laplace distribution.
     */
    NATIVE,

    /**
     * The scale is specified as the standard deviation of the distribution
     * and the native scale factor is derived from the standard deviation.
     */
    SDEV
}
