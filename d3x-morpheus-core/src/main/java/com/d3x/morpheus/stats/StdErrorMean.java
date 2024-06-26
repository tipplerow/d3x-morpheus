/*
 * Copyright (C) 2014-2018 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.stats;

/**
 * A Statistic implementation that supports incremental calculation of sample Standard Error of the Mean (SEM)
 *
 * @see <a href="https://en.wikipedia.org/wiki/Standard_error">Wikipedia</a>
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class StdErrorMean extends StdDev {

    /**
     * Constructor
     */
    public StdErrorMean() {
        super(true);
    }

    @Override
    public StatType getType() {
        return StatType.SEM;
    }

    @Override
    public double getValue() {
        final long n = getN();
        if (n == 0) {
            return Double.NaN;
        } else {
            final double stdDev = super.getValue();
            return stdDev / Math.sqrt(n);
        }
    }
}
