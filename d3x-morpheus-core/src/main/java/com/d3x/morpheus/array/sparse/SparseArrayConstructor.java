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
package com.d3x.morpheus.array.sparse;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Date;
import java.util.TimeZone;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.array.ArrayFactory;
import com.d3x.morpheus.array.coding.IntCoding;
import com.d3x.morpheus.array.coding.LongCoding;

/**
 * An ArrayFactory.Constructor implementation designed to manufacture sparse Morpheus Arrays based on Trove Collections.
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class SparseArrayConstructor implements ArrayFactory.Constructor {

    static final float DEFAULT_LOAD_FACTOR = 0.95f;

    private static final IntCoding<Year> yearCoding = new IntCoding.OfYear();
    private static final IntCoding<Currency> currencyCoding = new IntCoding.OfCurrency();
    private static final IntCoding<ZoneId> zoneIdCoding = IntCoding.ofZoneId();
    private static final IntCoding<TimeZone> timeZoneCoding = IntCoding.ofTimeZone();
    private static final LongCoding<Date> dateCoding = LongCoding.ofDate();
    private static final LongCoding<Instant> instantCoding = LongCoding.ofInstant();
    private static final LongCoding<LocalDate> localDateCoding = LongCoding.ofLocalDate();
    private static final LongCoding<LocalTime> localTimeCoding = LongCoding.ofLocalTime();
    private static final LongCoding<LocalDateTime> localDateTimeCoding = LongCoding.ofLocalDateTime();

    /**
     * Constructor
     */
    public SparseArrayConstructor() {
        super();
    }


    @Override()
    public final <T> Array<T> apply(Class<T> type, int length, T defaultValue) {
        return apply(type, length, defaultValue, null);
    }


    @Override
    public <T> Array<T> apply(Class<T> type, int length, T defaultValue, String path) {
        return apply(type, length, 0.2d, defaultValue);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <T> Array<T> apply(Class<T> type, int length, double fillPct, T defaultValue) {
        if (type.isEnum()) {
            var enumCoding = (IntCoding<T>)IntCoding.ofEnum((Class<Enum>)type);
            return new SparseArrayWithIntCoding<>(length, fillPct, defaultValue, enumCoding);
        } else {
            switch (ArrayType.of(type)) {
                case BOOLEAN:           return ArrayFactory.dense().apply(type, length, defaultValue, null);
                case INTEGER:           return (Array<T>)new SparseArrayOfInts(length, fillPct, (Integer)defaultValue);
                case LONG:              return (Array<T>)new SparseArrayOfLongs(length, fillPct, (Long)defaultValue);
                case DOUBLE:            return (Array<T>)new SparseArrayOfDoubles(length, fillPct, (Double)defaultValue);
                case OBJECT:            return (Array<T>)new SparseArrayOfObjects(type, length, fillPct, defaultValue);
                case STRING:            return (Array<T>)new SparseArrayOfObjects(type, length, fillPct, defaultValue);
                case LOCAL_DATE:        return (Array<T>)new SparseArrayWithLongCoding<>(length, fillPct, (LocalDate)defaultValue, localDateCoding);
                case LOCAL_TIME:        return (Array<T>)new SparseArrayWithLongCoding<>(length, fillPct, (LocalTime)defaultValue, localTimeCoding);
                case LOCAL_DATETIME:    return (Array<T>)new SparseArrayWithLongCoding<>(length, fillPct, (LocalDateTime)defaultValue, localDateTimeCoding);
                case YEAR:              return (Array<T>)new SparseArrayWithIntCoding<>(length, fillPct, (Year)defaultValue, yearCoding);
                case ZONE_ID:           return (Array<T>)new SparseArrayWithIntCoding<>(length, fillPct, (ZoneId)defaultValue, zoneIdCoding);
                case TIME_ZONE:         return (Array<T>)new SparseArrayWithIntCoding<>(length, fillPct, (TimeZone)defaultValue, timeZoneCoding);
                case DATE:              return (Array<T>)new SparseArrayWithLongCoding<>(length, fillPct, (Date)defaultValue, dateCoding);
                case INSTANT:           return (Array<T>)new SparseArrayWithLongCoding<>(length, fillPct, (Instant)defaultValue, instantCoding);
                case CURRENCY:          return (Array<T>)new SparseArrayWithIntCoding<>(length, fillPct, (Currency)defaultValue, currencyCoding);
                case ZONED_DATETIME:    return (Array<T>)new SparseArrayOfZonedDateTimes(length, fillPct, (ZonedDateTime) defaultValue);
                default:                return (Array<T>)new SparseArrayOfObjects(type, length, fillPct, defaultValue);
            }
        }
    }
}
