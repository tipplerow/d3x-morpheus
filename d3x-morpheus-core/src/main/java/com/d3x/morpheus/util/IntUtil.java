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
package com.d3x.morpheus.util;

/**
 * Provides static utility methods operating on integer values.
 *
 * @author Scott Shaffer
 */
public final class IntUtil {
    /**
     * Bounds an integer value between lower and upper bounds.
     *
     * @param value the value to bound.
     * @param lower the lower bound.
     * @param upper the upper bound.
     *
     * @return the bounded value.
     */
    public static int bound(int value, int lower, int upper) {
        if (value < lower) {
            return lower;
        }
        else if (value > upper) {
            return upper;
        }
        else {
            return value;
        }
    }
}
