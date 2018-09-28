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
package com.zavtech.morpheus.frame;

/**
 * The Random Access interface to access DataFrame elements by row and column ordinals or keys
 *
 * @param <R>       the row key type for frame
 * @param <C>       the column key type for frame
 *
 * @author Xavier Witdouck
 */
public interface DataFrameAccess<R,C> {

    /**
     * Returns the value for the row and column key coordinates provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @return          the value for coordinates
     */
    boolean getBoolean(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @return              the value for coordinates
     */
    boolean getBooleanAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns the value for the row and column key coordinates provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @return          the value for coordinates
     */
    int getInt(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @return              the value for coordinates
     */
    int getIntAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns the value for the row and column key coordinates provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @return          the value for coordinates
     */
    long getLong(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @return              the value for coordinates
     */
    long getLongAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns the value for the row and column key coordinates provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @return          the value for coordinates
     */
    double getDouble(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @return              the value for coordinates
     */
    double getDoubleAt(int rowOrdinal, int colOrdinal);

    /**
     * Returns the value for the row and column key coordinates provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @return          the value for coordinates
     */
    <T> T getValue(R rowKey, C colKey);

    /**
     * Returns the value for the row and column ordinal coordinates provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @return              the value for coordinates
     */
    <T> T getValueAt(int rowOrdinal, int colOrdinal);

    /**
     * Sets the value at the row and column keys provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @param value     the value to set
     */
    boolean setBoolean(R rowKey, C colKey, boolean value);

    /**
     * Sets the value at the row and column ordinals provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @param value         the value to set
     */
    boolean setBooleanAt(int rowOrdinal, int colOrdinal, boolean value);

    /**
     * Sets the value at the row and column keys provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @param value     the value to set
     */
    int setInt(R rowKey, C colKey, int value);

    /**
     * Sets the value at the row and column ordinals provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @param value         the value to set
     */
    int setIntAt(int rowOrdinal, int colOrdinal, int value);

    /**
     * Sets the value at the row and column keys provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @param value     the value to set
     */
    long setLong(R rowKey, C colKey, long value);

    /**
     * Sets the value at the row and column ordinals provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @param value         the value to set
     */
    long setLongAt(int rowOrdinal, int colOrdinal, long value);

    /**
     * Sets the value at the row and column keys provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @param value     the value to set
     */
    double setDouble(R rowKey, C colKey, double value);

    /**
     * Sets the value at the row and column ordinals provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @param value         the value to set
     */
    double setDoubleAt(int rowOrdinal, int colOrdinal, double value);

    /**
     * Sets the value at the row and column keys provided
     * @param rowKey    the row key coordinate
     * @param colKey    the column key coordinate
     * @param value     the value to set
     */
    <T> T setValue(R rowKey, C colKey, T value);

    /**
     * Sets the value at the row and column ordinals provided
     * @param rowOrdinal    the row ordinal coordinate
     * @param colOrdinal    the column ordinal coordinate
     * @param value         the value to set
     */
    <T> T setValueAt(int rowOrdinal, int colOrdinal, T value);

}
