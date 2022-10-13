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
package com.d3x.morpheus.array;

import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.Asserts;

/**
 * A class designed to build an array incrementally, without necessarily knowing the type upfront, or the final length.
 *
 * @param <T>   the array element dataType
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
public class ArrayBuilder<T> {

    private int capacity;
    private int index = 0;
    private float fillPct;
    private Class<T> type;
    private Array<T> array;
    private ArrayType dataType;
    private boolean checkType;

    /**
     * Constructor
     * @param capacity      the initial capacity for this builder
     * @param type          the optional array element dataType if known (null allowed)
     * @param defaultValue  the default value for the array (null allowed, even for primitive types)
     * @param fillPct       the array fill percent which must be > 0 and <= 1 (1 implies dense array, < 1 implies sparse array)
     */
    @SuppressWarnings("unchecked")
    private ArrayBuilder(int capacity, Class<T> type, T defaultValue, float fillPct) {
        Asserts.check(fillPct > 0f, "The load factor mus be > 0 and <= 1");
        Asserts.check(fillPct <= 1f, "The load factor mus be > 0 and <= 1");
        this.fillPct = fillPct;
        this.capacity = capacity > 0 ? capacity : 10;
        if (type != null) {
            this.type = type;
            this.dataType = ArrayType.of(type);
            this.array = Array.of(type, this.capacity, defaultValue, fillPct);
            this.checkType = false;
        } else if (defaultValue != null) {
            this.type = (Class<T>)defaultValue.getClass();
            this.dataType = ArrayType.of(this.type);
            this.array = Array.of(this.type, this.capacity, defaultValue, fillPct);
            this.checkType = false;
        }
    }

    /**
     * Returns a newly created builder for a dense arrays with initial length
     * @param initialLength     the initial capacity for builder
     * @param <T>               the array element dataType
     * @return                  the newly created builder
     */
    public static <T> ArrayBuilder<T> of(int initialLength) {
        return new ArrayBuilder<>(initialLength, null, null, 1f);
    }


    /**
     * Returns a newly created builder for a dense arrays with initial length
     * @param initialLength     the initial capacity for builder
     * @param fillPct           the array fill percent which must be > 0 and <= 1 (1 implies dense array, < 1 implies sparse array)
     * @param <T>               the array element dataType
     * @return                  the newly created builder
     */
    public static <T> ArrayBuilder<T> of(int initialLength, float fillPct) {
        return new ArrayBuilder<>(initialLength, null, null, fillPct);
    }


    /**
     * Returns a newly created builder for a dense arrays based on the arguments provided
     * @param initialLength     the initial capacity for builder
     * @param type              the dataType for array elements
     * @param <T>               the array element dataType
     * @return                  the newly created builder
     */
    public static <T> ArrayBuilder<T> of(int initialLength, Class<T> type) {
        return new ArrayBuilder<>(initialLength, type, null, 1f);
    }

    /**
     * Returns a newly created builder for a dense arrays based on the arguments provided
     * @param initialLength     the initial capacity for builder
     * @param type              the dataType for array elements
     * @param defaultValue      the default value for the array (null allowed, even for primitive types)
     * @param <T>               the array element dataType
     * @return                  the newly created builder
     */
    public static <T> ArrayBuilder<T> of(int initialLength, Class<T> type, T defaultValue) {
        return new ArrayBuilder<>(initialLength, type, defaultValue, 1f);
    }

    /**
     * Returns a newly created builder for a dense arrays based on the arguments provided
     * @param initialLength     the initial capacity for builder
     * @param type              the dataType for array elements
     * @param defaultValue      the default value for the array (null allowed, even for primitive types)
     * @param fillPct           the array fill percent which must be > 0 and <= 1 (1 implies dense array, < 1 implies sparse array)
     * @param <T>               the array element dataType
     * @return                  the newly created builder
     */
    public static <T> ArrayBuilder<T> of(int initialLength, Class<T> type, T defaultValue, float fillPct) {
        return new ArrayBuilder<>(initialLength, type, defaultValue, fillPct);
    }


    /**
     * Returns the data type for this builder
     * @return      the data type for builder
     */
    public ArrayType getDataType() {
        return dataType != null ? dataType : ArrayType.OBJECT;
    }


    /**
     * Returns true if the entry at index is null
     * @param index     the array index
     * @return          true if entry is null at index
     */
    public boolean isNull(int index) {
        return array == null || array.isNull(index);
    }


    /**
     * Adds an entry to array being built
     * @param value the value to add
     * @return  the index assigned to element
     */
    @SuppressWarnings("unchecked")
    public final int append(T value) {
        if (value != null) {
            this.checkType((Class<T>)value.getClass());
            this.checkLength(index);
            this.array.setValue(index, value);
        }
        this.index++;
        return index-1;
    }

    /**
     * Adds an entry to array being built
     * @param value the value to add
     * @return  the index assigned to element
     */
    @SuppressWarnings("unchecked")
    public final int appendBoolean(boolean value) {
        this.checkType((Class<T>)Boolean.class);
        this.checkLength(index);
        this.array.setBoolean(index++, value);
        return index-1;
    }

    /**
     * Adds an entry to array being built
     * @param value the value to add
     * @return  the index assigned to element
     */
    @SuppressWarnings("unchecked")
    public final int appendInt(int value) {
        this.checkType((Class<T>)Integer.class);
        this.checkLength(index);
        this.array.setInt(index++, value);
        return index-1;
    }

    /**
     * Adds an entry to array being built
     * @param value the value to add
     * @return  the index assigned to element
     */
    @SuppressWarnings("unchecked")
    public final int appendLong(long value) {
        this.checkType((Class<T>)Long.class);
        this.checkLength(index);
        this.array.setLong(index++, value);
        return index-1;
    }

    /**
     * Adds an entry to array being built
     * @param value the value to add
     * @return  the index assigned to element
     */
    @SuppressWarnings("unchecked")
    public final int appendDouble(double value) {
        this.checkType((Class<T>)Double.class);
        this.checkLength(index);
        this.array.setDouble(index++, value);
        return index-1;
    }

    /**
     * Sets a value for this array builder
     * @param index the array index location
     * @param value the value to apply
     */
    @SuppressWarnings("unchecked")
    public final void setValue(int index, T value) {
        if (value != null) {
            this.checkType((Class<T>)value.getClass());
            this.checkLength(index);
            this.array.setValue(index, value);
        }
        this.index = Math.max(this.index, index+1);
    }


    /**
     * Sets a value for this array builder
     * @param index the array index location
     * @param value the value to apply
     */
    @SuppressWarnings("unchecked")
    public final void setBoolean(int index, boolean value) {
        this.checkType((Class<T>)Boolean.class);
        this.checkLength(index);
        this.array.setBoolean(index, value);
        this.index = Math.max(this.index, index+1);
    }


    /**
     * Sets a value for this array builder
     * @param index the array index location
     * @param value the value to apply
     */
    @SuppressWarnings("unchecked")
    public final void setInt(int index, int value) {
        this.checkType((Class<T>)Integer.class);
        this.checkLength(index);
        this.array.setInt(index, value);
        this.index = Math.max(this.index, index+1);
    }


    /**
     * Sets a value for this array builder
     * @param index the array index location
     * @param value the value to apply
     */
    @SuppressWarnings("unchecked")
    public final void setLong(int index, long value) {
        this.checkType((Class<T>)Long.class);
        this.checkLength(index);
        this.array.setLong(index, value);
        this.index = Math.max(this.index, index+1);
    }


    /**
     * Sets a value for this array builder
     * @param index the array index location
     * @param value the value to add
     */
    @SuppressWarnings("unchecked")
    public final void setDouble(int index, double value) {
        this.checkType((Class<T>)Double.class);
        this.checkLength(index);
        this.array.setDouble(index, value);
        this.index = Math.max(this.index, index+1);
    }


    /**
     * Adds the value to the existing value at the index specified
     * @param index the array index location
     * @param value the value to add
     */
    @SuppressWarnings("unchecked")
    public final void plusInt(int index, int value) {
        this.checkType((Class<T>)Integer.class);
        this.checkLength(index);
        var existing = array.getInt(index);
        this.array.setInt(index, existing + value);
        this.index = Math.max(this.index, index+1);
    }


    /**
     * Adds the value to the existing value at the index specified
     * @param index the array index location
     * @param value the value to add
     */
    @SuppressWarnings("unchecked")
    public final void plusLong(int index, long value) {
        this.checkType((Class<T>)Long.class);
        this.checkLength(index);
        var existing = array.getLong(index);
        this.array.setLong(index, existing + value);
        this.index = Math.max(this.index, index+1);
    }


    /**
     * Adds the value to the existing value at the index specified
     * @param index the array index location
     * @param value the value to apply
     */
    @SuppressWarnings("unchecked")
    public final void plusDouble(int index, double value) {
        this.checkType((Class<T>)Double.class);
        this.checkLength(index);
        var toAdd = Double.isNaN(value) ? 0d : value;
        var existing = array.getDouble(index);
        var result = Double.isNaN(existing) ? value : existing + toAdd;
        this.array.setDouble(index, result);
        this.index = Math.max(this.index, index+1);
    }


    /**
     * Appends all values from the iterable specified
     * @param values    the values to add
     */
    public final ArrayBuilder<T> appendAll(Iterable<T> values) {
        if (values instanceof Range) {
            values.forEach(this::append);
        } else if (values instanceof Array) {
            final Array<T> newArray = (Array<T>)values;
            final int newLength = index + newArray.length();
            this.array.expand(newLength);
            switch (newArray.typeCode()) {
                case BOOLEAN:           newArray.forEachBoolean(this::appendBoolean);  break;
                case INTEGER:           newArray.forEachInt(this::appendInt);          break;
                case LONG:              newArray.forEachLong(this::appendLong);        break;
                case DOUBLE:            newArray.forEachDouble(this::appendDouble);    break;
                case DATE:              newArray.forEachLong(this::appendLong);        break;
                case INSTANT:           newArray.forEachLong(this::appendLong);        break;
                case LOCAL_DATE:        newArray.forEachLong(this::appendLong);        break;
                case LOCAL_TIME:        newArray.forEachLong(this::appendLong);        break;
                case LOCAL_DATETIME:    newArray.forEachLong(this::appendLong);        break;
                default:                newArray.forEach(this::append);                break;
            }
        } else {
            values.forEach(this::append);
        }
        return this;
    }

    /**
     * Checks that the specified data dataType is compatible with the current array we are building
     * @param type  the element data dataType being added
     */
    @SuppressWarnings("unchecked")
    private void checkType(Class<T> type) {
        if (array == null) {
            this.type = type;
            this.dataType = ArrayType.of(type);
            this.array = Array.of(type, capacity, fillPct);
            this.checkType = this.type != Object.class;
            this.capacity = array.length();
        } else if (checkType && !isMatch(type)) {
            var newArray = Array.<T>ofObjects(array.length(), fillPct);
            for (int i=0; i<array.length(); ++i) newArray.setValue(i, array.getValue(i));
            this.array = newArray;
            this.type = (Class<T>)Object.class;
            this.dataType = ArrayType.OBJECT;
            this.capacity = array.length();
        }
    }

    /**
     * Returns the current length for this builder
     * @return      the current length for builder
     */
    public int length() {
        return index;
    }

    /**
     * Returns true if the data dataType is a match for the current array dataType
     * @param type  the data dataType class
     * @return          true if match
     */
    private boolean isMatch(Class<T> type) {
        return type == this.type || this.type.isAssignableFrom(type);
    }

    /**
     * Checks to see if the current length supports set a value at the index specified, expanding if necessary
     * @param index the index to check whether current length supports this location
     */
    private void checkLength(int index) {
        if (index >= capacity) {
            int newLength = capacity + (capacity >> 1);
            if (newLength < index + 1) newLength = index + 1;
            this.array.expand(newLength);
            this.capacity = array.length();
        }
    }

    /**
     * Appends all items from the other builder
     * @param other the other builder
     */
    public final ArrayBuilder<T> appendAll(ArrayBuilder<T> other) {
        if (array == null) {
            this.array = other.array.copy();
            return this;
        } else {
            final Array<T> arrayToAdd = other.array;
            final int totalLength = this.index + other.index;
            if (totalLength > array.length()) array.expand(totalLength);
            for (int i=0; i<other.index; ++i) {
                //todo: optimize this for common types to avoid boxing
                final T value = arrayToAdd.getValue(i);
                this.append(value);
            }
            return this;
        }
    }

    /**
     * Returns the final array for this appender
     * @return  the final array result
     */
    @SuppressWarnings("unchecked")
    public final Array<T> toArray() {
        if (array == null) {
            return Array.ofObjects(0);
        } else {
            return index < array.length() ? array.copy(0, index) : array;
        }
    }

}
