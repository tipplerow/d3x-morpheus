/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
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

import org.testng.Assert;
import org.testng.annotations.Test;

public class NumberUnitTest {
    private void assertConvert(double fromValue, NumberUnit fromUnit, NumberUnit toUnit, double toValue) {
        Assert.assertEquals(NumberUnit.convert(fromValue, fromUnit, toUnit), toValue, 1.0E-12);
    }

    private void assertConvertTo(NumberUnit fromUnit, double fromValue, NumberUnit toUnit, double toValue) {
        Assert.assertEquals(fromUnit.convertTo(toUnit, fromValue), toValue, 1.0E-12);
    }

    @Test
    public void testConvert() {
        assertConvert(1.23, NumberUnit.BASIS_POINTS, NumberUnit.BASIS_POINTS, 1.23);
        assertConvert(1.23, NumberUnit.BASIS_POINTS, NumberUnit.PERCENTAGE,   1.23E-02);
        assertConvert(1.23, NumberUnit.BASIS_POINTS, NumberUnit.ONES,         1.23E-04);
        assertConvert(1.23, NumberUnit.BASIS_POINTS, NumberUnit.THOUSANDS,    1.23E-07);
        assertConvert(1.23, NumberUnit.BASIS_POINTS, NumberUnit.MILLIONS,     1.23E-10);

        assertConvert(1.23, NumberUnit.PERCENTAGE, NumberUnit.BASIS_POINTS, 1.23E2);
        assertConvert(1.23, NumberUnit.PERCENTAGE, NumberUnit.PERCENTAGE,   1.23);
        assertConvert(1.23, NumberUnit.PERCENTAGE, NumberUnit.ONES,         1.23E-02);
        assertConvert(1.23, NumberUnit.PERCENTAGE, NumberUnit.THOUSANDS,    1.23E-05);
        assertConvert(1.23, NumberUnit.PERCENTAGE, NumberUnit.MILLIONS,     1.23E-08);

        assertConvert(1.23, NumberUnit.ONES, NumberUnit.BASIS_POINTS, 1.23E4);
        assertConvert(1.23, NumberUnit.ONES, NumberUnit.PERCENTAGE,   1.23E2);
        assertConvert(1.23, NumberUnit.ONES, NumberUnit.ONES,         1.23);
        assertConvert(1.23, NumberUnit.ONES, NumberUnit.THOUSANDS,    1.23E-03);
        assertConvert(1.23, NumberUnit.ONES, NumberUnit.MILLIONS,     1.23E-06);
        assertConvert(1.23, NumberUnit.ONES, NumberUnit.BILLIONS,     1.23E-09);

        assertConvert(1.23, NumberUnit.THOUSANDS, NumberUnit.BASIS_POINTS, 1.23E7);
        assertConvert(1.23, NumberUnit.THOUSANDS, NumberUnit.PERCENTAGE,   1.23E5);
        assertConvert(1.23, NumberUnit.THOUSANDS, NumberUnit.ONES,         1.23E3);
        assertConvert(1.23, NumberUnit.THOUSANDS, NumberUnit.THOUSANDS,    1.23);
        assertConvert(1.23, NumberUnit.THOUSANDS, NumberUnit.MILLIONS,     1.23E-03);
        assertConvert(1.23, NumberUnit.THOUSANDS, NumberUnit.BILLIONS,     1.23E-06);

        assertConvert(1.23, NumberUnit.MILLIONS, NumberUnit.PERCENTAGE, 1.23E8);
        assertConvert(1.23, NumberUnit.MILLIONS, NumberUnit.ONES,       1.23E6);
        assertConvert(1.23, NumberUnit.MILLIONS, NumberUnit.THOUSANDS,  1.23E3);
        assertConvert(1.23, NumberUnit.MILLIONS, NumberUnit.MILLIONS,   1.23);
        assertConvert(1.23, NumberUnit.MILLIONS, NumberUnit.BILLIONS,   1.23E-03);

        assertConvert(1.23, NumberUnit.BILLIONS, NumberUnit.ONES,      1.23E9);
        assertConvert(1.23, NumberUnit.BILLIONS, NumberUnit.THOUSANDS, 1.23E6);
        assertConvert(1.23, NumberUnit.BILLIONS, NumberUnit.MILLIONS,  1.23E3);
        assertConvert(1.23, NumberUnit.BILLIONS, NumberUnit.BILLIONS,  1.23);
    }

    @Test
    public void testConvertTo() {
        assertConvertTo(
                NumberUnit.BASIS_POINTS, 1.23,
                NumberUnit.BASIS_POINTS, 1.23);
        assertConvertTo(
                NumberUnit.BASIS_POINTS, 1.23,
                NumberUnit.PERCENTAGE, 1.23E-02);
        assertConvertTo(
                NumberUnit.BASIS_POINTS, 1.23,
                NumberUnit.ONES, 1.23E-04);
        assertConvertTo(
                NumberUnit.BASIS_POINTS, 1.23,
                NumberUnit.THOUSANDS, 1.23E-07);
        assertConvertTo(
                NumberUnit.BASIS_POINTS, 1.23,
                NumberUnit.MILLIONS, 1.23E-10);

        assertConvertTo(
                NumberUnit.PERCENTAGE, 1.23,
                NumberUnit.BASIS_POINTS, 1.23E2);
        assertConvertTo(
                NumberUnit.PERCENTAGE, 1.23,
                NumberUnit.PERCENTAGE, 1.23);
        assertConvertTo(
                NumberUnit.PERCENTAGE, 1.23,
                NumberUnit.ONES, 1.23E-02);
        assertConvertTo(
                NumberUnit.PERCENTAGE, 1.23,
                NumberUnit.THOUSANDS, 1.23E-05);
        assertConvertTo(
                NumberUnit.PERCENTAGE, 1.23,
                NumberUnit.MILLIONS, 1.23E-08);

        assertConvertTo(
                NumberUnit.ONES, 1.23,
                NumberUnit.BASIS_POINTS, 1.23E4);
        assertConvertTo(
                NumberUnit.ONES, 1.23,
                NumberUnit.PERCENTAGE, 1.23E2);
        assertConvertTo(
                NumberUnit.ONES, 1.23,
                NumberUnit.ONES, 1.23);
        assertConvertTo(
                NumberUnit.ONES, 1.23,
                NumberUnit.THOUSANDS, 1.23E-03);
        assertConvertTo(
                NumberUnit.ONES, 1.23,
                NumberUnit.MILLIONS, 1.23E-06);
        assertConvertTo(
                NumberUnit.ONES, 1.23,
                NumberUnit.BILLIONS, 1.23E-09);

        assertConvertTo(
                NumberUnit.THOUSANDS, 1.23,
                NumberUnit.BASIS_POINTS, 1.23E7);
        assertConvertTo(
                NumberUnit.THOUSANDS, 1.23,
                NumberUnit.PERCENTAGE, 1.23E5);
        assertConvertTo(
                NumberUnit.THOUSANDS, 1.23,
                NumberUnit.ONES, 1.23E3);
        assertConvertTo(
                NumberUnit.THOUSANDS, 1.23,
                NumberUnit.THOUSANDS, 1.23);
        assertConvertTo(
                NumberUnit.THOUSANDS, 1.23,
                NumberUnit.MILLIONS, 1.23E-03);
        assertConvertTo(
                NumberUnit.THOUSANDS, 1.23,
                NumberUnit.BILLIONS, 1.23E-06);

        assertConvertTo(
                NumberUnit.MILLIONS, 1.23,
                NumberUnit.PERCENTAGE, 1.23E8);
        assertConvertTo(
                NumberUnit.MILLIONS, 1.23,
                NumberUnit.ONES, 1.23E6);
        assertConvertTo(
                NumberUnit.MILLIONS, 1.23,
                NumberUnit.THOUSANDS, 1.23E3);
        assertConvertTo(
                NumberUnit.MILLIONS, 1.23,
                NumberUnit.MILLIONS, 1.23);
        assertConvertTo(
                NumberUnit.MILLIONS, 1.23,
                NumberUnit.BILLIONS, 1.23E-03);

        assertConvertTo(
                NumberUnit.BILLIONS, 1.23,
                NumberUnit.ONES, 1.23E9);
        assertConvertTo(
                NumberUnit.BILLIONS, 1.23,
                NumberUnit.THOUSANDS, 1.23E6);
        assertConvertTo(
                NumberUnit.BILLIONS, 1.23,
                NumberUnit.MILLIONS, 1.23E3);
        assertConvertTo(
                NumberUnit.BILLIONS, 1.23,
                NumberUnit.BILLIONS, 1.23);
    }
}
