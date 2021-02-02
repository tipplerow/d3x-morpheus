/*
 * Copyright (C) 2018-2021 D3X Systems - All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.d3x.morpheus.series;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
class InnerProduct<K> {
    private final DoubleSeries<K> s1;
    private final DoubleSeries<K> s2;
    private final DoubleSeries<K> wt;

    static <K> double compute(DoubleSeries<K> s1, DoubleSeries<K> s2) {
        return compute(s1, s2, null);
    }

    static <K> double compute(DoubleSeries<K> s1, DoubleSeries<K> s2, DoubleSeries<K> wt) {
        InnerProduct<K> product = new InnerProduct<>(s1, s2, wt);
        return product.compute();
    }

    private double compute() {
        //
        // Since missing values are replaced with zero, we can choose
        // the shorter series to process...
        //
        DoubleSeries<K> shorter = s1.size() < s2.size() ? s1 : s2;
        return shorter.keys().mapToDouble(this::getTerm).sum();
    }

    private double getTerm(K key) {
        return getWeight(key) * s1.getDoubleOrZero(key) * s2.getDoubleOrZero(key);
    }

    private double getWeight(K key) {
        if (wt != null)
            return wt.getDoubleOrZero(key);
        else
            return 1.0;
    }
}
