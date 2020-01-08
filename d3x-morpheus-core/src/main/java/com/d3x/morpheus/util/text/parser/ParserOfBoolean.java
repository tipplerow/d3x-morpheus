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
package com.d3x.morpheus.util.text.parser;

import java.util.Set;

import com.d3x.morpheus.util.Collect;
import com.d3x.morpheus.util.functions.FunctionStyle;
import com.d3x.morpheus.util.functions.ToBooleanFunction;
import com.d3x.morpheus.util.text.FormatException;

/**
 * A Parser implementation for booleans
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class ParserOfBoolean extends Parser<Boolean> {

    private static final Set<String> trueSet = Collect.asSet("true", "yes", "on", "y");
    private static final Set<String> falseSet = Collect.asSet("false", "no", "off", "n");

    /**
     * Constructor
     * @param nullChecker   the null checker function
     */
    ParserOfBoolean(ToBooleanFunction<String> nullChecker) {
        super(FunctionStyle.BOOLEAN, Boolean.class, nullChecker);
    }

    @Override
    public final boolean isSupported(String value) {
        return !getNullChecker().applyAsBoolean(value) && (trueSet.contains(lower(value)) || falseSet.contains(lower(value)));
    }

    @Override
    public Parser<Boolean> optimize(String value) {
        return this;
    }

    @Override
    public final boolean applyAsBoolean(String value) {
        try {
            return !getNullChecker().applyAsBoolean(value) && trueSet.contains(lower(value));
        } catch (Exception ex) {
            throw new FormatException("Failed to parse value into Boolean: " + value, ex);
        }
    }

    /**
     * Returns lower case version of value or null if value is null
     * @param value the value to convert to lower case
     * @return      the lower case value
     */
    private static String lower(String value) {
        return value != null ? value.toLowerCase() : null;
    }
}
