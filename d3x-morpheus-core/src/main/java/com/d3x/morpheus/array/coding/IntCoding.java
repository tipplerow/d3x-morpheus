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
package com.d3x.morpheus.array.coding;

import java.time.Year;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.IntStream;

import com.d3x.core.util.Option;
import com.d3x.morpheus.util.IntComparator;
import com.d3x.morpheus.util.SortAlgorithm;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * An interface that exposes a coding between object values and corresponding int code
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
public interface IntCoding<T> extends Coding<T> {

    /**
     * Returns the code for the value specified
     * @param value the value, which can be null
     * @return      the code for value
     */
    int getCode(T value);

    /**
     * Returns the value for the code specified
     * @param code  the code for requested value
     * @return      the value match, which can be null
     */
    T getValue(int code);


    /**
     * Returns a new coding for the Year class
     * @return  the newly created coding
     */
    static IntCoding<Year> ofYear() {
        return new OfYear();
    }

    /**
     * Returns a new coding for the ZoneId class
     * @return  the newly created coding
     */
    static IntCoding<ZoneId> ofZoneId() {
        return new OfZoneId();
    }

    /**
     * Returns a new coding for the TimeZone class
     * @return  the newly created coding
     */
    static IntCoding<TimeZone> ofTimeZone() {
        return new OfTimeZone();
    }

    /**
     * Returns a new coding for the Currency class
     * @return  the newly created coding
     */
    static IntCoding<Currency> ofCurrency() {
        return new OfCurrency();
    }

    /**
     * Returns a new coding for the enum specified
     * @param type  the enum type
     * @param <T>   the type
     * @return      the newly created coding
     */
    static <T extends Enum> IntCoding<T> ofEnum(Class<T> type) {
        return new OfEnum<>(type);
    }


    /**
     * Manages IntCoding support
     */
    class Support {

        private static Map<Class<?>,IntCoding<?>> codingMap = new HashMap<>();

        /*
         * Static initializer
         */
        static {
            Support.register(Integer.class, new OfInt());
            Support.register(Year.class, new OfYear());
            Support.register(ZoneId.class, new OfZoneId());
            Support.register(TimeZone.class, new OfTimeZone());
            Support.register(Currency.class, new OfCurrency());
        }


        /**
         * Returns true if int coding is supported for type
         * @param type  the data type
         * @return      true if supported
         */
        public static boolean includes(Class<?> type) {
            return type.isEnum() || codingMap.containsKey(type);
        }


        /**
         * Registers a coding definition for the type
         * @param type      the coding type
         * @param coding    the coding instance
         * @param <T>       the type
         */
        public static <T> void register(Class<T> type, IntCoding<T> coding) {
            codingMap.put(type, coding);
        }


        /**
         * Returns the int coding for type if available
         * @param type  the data type
         * @param <T>   the coding type
         * @return      the coding option
         */
        @SuppressWarnings("unchecked")
        public static <T> Option<IntCoding<T>> getCoding(Class<T> type) {
            if (type.isEnum()) {
                var enumType = (Class<Enum>)type;
                return Option.of((IntCoding<T>)new OfEnum<>(enumType));
            } else {
                return Option.of((IntCoding<T>)codingMap.get(type));
            }
        }
    }


    /**
     * An identity coding for Long
     */
    class OfInt extends BaseCoding<Integer> implements IntCoding<Integer> {

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         */
        OfInt() {
            super(Integer.class);
        }

        @Override
        public final int getCode(Integer value) {
            return value == null ? Integer.MIN_VALUE : value;
        }

        @Override
        public final Integer getValue(int code) {
            return code == Integer.MIN_VALUE ? null : code;
        }
    }


    /**
     * An IntCoding implementation for an Enum class
     */
    class OfEnum<T extends Enum> extends BaseCoding<T> implements IntCoding<T> {

        private static final long serialVersionUID = 1L;

        private final T[] values;
        private final int[] codes;

        /**
         * Constructor
         * @param type  the enum class
         */
        @SuppressWarnings("unchecked")
        OfEnum(Class<T> type) {
            super(type);
            this.values = type.getEnumConstants();
            this.codes = IntStream.range(0, values.length).toArray();
            final IntComparator comparator = (i, j) -> values[i].compareTo(values[j]);
            SortAlgorithm.getDefault(false).sort(0, values.length, comparator, (i, j) -> {
                final T v1 = values[i]; values[i] = values[j]; values[j] = v1;
                final int code = codes[i]; codes[i] = codes[j]; codes[j] = code;
            });
        }

        @Override
        public final int getCode(T value) {
            return value == null ? -1 : codes[value.ordinal()];
        }

        @Override
        public final T getValue(int code) {
            return code < 0 ? null : values[code];
        }
    }



    /**
     * An IntCoding implementation for the Year class
     */
    class OfYear extends BaseCoding<Year> implements IntCoding<Year> {

        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         */
        public OfYear() {
            super(Year.class);
        }

        @Override
        public final int getCode(Year value) {
            return value == null ? -1 : value.getValue();
        }

        @Override
        public final Year getValue(int code) {
            return code < 0 ? null : Year.of(code);
        }
    }



    /**
     * An IntCoding implementation for the Currency class.
     */
    class OfCurrency extends BaseCoding<Currency> implements IntCoding<Currency> {

        private static final long serialVersionUID = 1L;

        private final Currency[] currencies;
        private final TObjectIntMap<Currency> codeMap;

        /**
         * Constructor
         */
        public OfCurrency() {
            super(Currency.class);
            this.currencies = Currency.getAvailableCurrencies().stream().toArray(Currency[]::new);
            this.codeMap = new TObjectIntHashMap<>(currencies.length, 0.5f, -1);
            Arrays.sort(currencies, (c1, c2) -> c1.getCurrencyCode().compareTo(c2.getCurrencyCode()));
            for (int i = 0; i< currencies.length; ++i) {
                this.codeMap.put(currencies[i], i);
            }
        }

        @Override
        public final int getCode(Currency value) {
            return value == null ? -1 : codeMap.get(value);
        }

        @Override
        public final Currency getValue(int code) {
            return code < 0 ? null : currencies[code];
        }
    }


    /**
     * An IntCoding implementation for the ZoneId class.
     */
    class OfZoneId extends BaseCoding<ZoneId> implements IntCoding<ZoneId> {

        private static final long serialVersionUID = 1L;

        private final ZoneId[] zoneIds;
        private final TObjectIntMap<ZoneId> codeMap;

        /**
         * Constructor
         */
        OfZoneId() {
            super(ZoneId.class);
            this.zoneIds = ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).toArray(ZoneId[]::new);
            this.codeMap = new TObjectIntHashMap<>(zoneIds.length, 0.5f, -1);
            Arrays.sort(zoneIds, (z1, z2) -> z1.getId().compareTo(z2.getId()));
            for (int i=0; i<zoneIds.length; ++i) {
                this.codeMap.put(zoneIds[i], i);
            }
        }

        @Override
        public final int getCode(ZoneId value) {
            return value == null ? -1 : codeMap.get(value);
        }

        @Override
        public final ZoneId getValue(int code) {
            return code < 0 ? null : zoneIds[code];
        }
    }


    /**
     * An IntCoding implementation for the TimeZone class.
     */
    class OfTimeZone extends BaseCoding<TimeZone> implements IntCoding<TimeZone> {

        private static final long serialVersionUID = 1L;

        private final TimeZone[] timeZones;
        private final TObjectIntMap<TimeZone> codeMap;

        /**
         * Constructor
         */
        OfTimeZone() {
            super(TimeZone.class);
            this.timeZones = Arrays.stream(TimeZone.getAvailableIDs()).map(TimeZone::getTimeZone).toArray(TimeZone[]::new);
            this.codeMap = new TObjectIntHashMap<>(timeZones.length, 0.5f, -1);
            Arrays.sort(timeZones, (tz1, tz2) -> tz1.getID().compareTo(tz2.getID()));
            for (int i = 0; i< timeZones.length; ++i) {
                this.codeMap.put(timeZones[i], i);
            }
        }

        @Override
        public final int getCode(TimeZone value) {
            return value == null ? -1 : codeMap.get(value);
        }

        @Override
        public final TimeZone getValue(int code) {
            return code < 0 ? null : timeZones[code];
        }
    }


}
