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
package com.d3x.morpheus.util;

import lombok.Getter;

/**
 * Enumerates the units in which numerical values are reported.
 *
 * @author Scott Shaffer
 */
public enum NumberUnit {
    BASIS_POINTS("bp", 0.0001),
    PERCENTAGE("%", 0.01),
    ONES("", 1.0),
    THOUSANDS("(000's)",1.0E03),
    MILLIONS("M", 1.0E06),
    BILLIONS("B", 1.0E09);

    /**
     * Converts a numerical value from one unit to another.
     *
     * @param value    the value to convert.
     * @param fromUnit the original value units.
     * @param toUnit   the desired value units.
     *
     * @return the equivalent value in the desired units.
     */
    public static double convert (double value, NumberUnit fromUnit, NumberUnit toUnit) {
        return fromUnit.convertTo(toUnit, value);
    }

    /**
     * Converts a numerical value from this unit to another.
     *
     * @param toUnit    the unit to convert to.
     * @param fromValue the value in this unit.
     *
     * @return the value in the specified units.
     */
    public double convertTo(NumberUnit toUnit, double fromValue) {
        return fromValue * this.perUnit / toUnit.perUnit;
    }

    /**
     * Returns the number of unit (ones) values in one value of this unit
     * (e.g., 1000 for the {@code THOUSANDS} unit).
     *
     * @return the number of unit (ones) values in one value of this unit.
     */
    public double perUnit() {
        return perUnit;
    }

    /**
     * The symbol used to identify this unit in display strings.
     */
    @Getter
    private final String symbol;

    private final double perUnit;

    NumberUnit(String symbol, double perUnit) {
        this.symbol =symbol;
        this.perUnit = perUnit;
    }
}
