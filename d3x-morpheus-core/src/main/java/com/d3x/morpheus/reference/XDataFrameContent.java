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
package com.d3x.morpheus.reference;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.array.ArrayUtils;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameCursor;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameOptions;
import com.d3x.morpheus.frame.DataFrameRow;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.index.IndexException;
import com.d3x.morpheus.index.IndexMapper;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.util.Mapper;
import com.d3x.morpheus.util.functions.ToBooleanFunction;

/**
 * A class that encapsulates the contents of a DataFrame, including row axis, column axis and data.
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class XDataFrameContent<R,C> implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private Index<R> rowKeys;
    private Index<C> colKeys;
    private boolean columnStore;
    private List<Array<?>> data;

    /**
     * Constructor
     * @param rowKeys  the row key index
     * @param colKeys  the column key index
     */
    @SuppressWarnings("unchecked")
    XDataFrameContent(Iterable<R> rowKeys, Iterable<C> colKeys, Class<?> dataType) {
        this(rowKeys, colKeys, true, new ArrayList<>());
        this.data = new ArrayList<>(this.rowKeys.capacity());
        final int rowCapacity = rowKeys().capacity();
        this.colKeys.keys().forEach(colKey -> {
            final Array<?> array = Array.of(dataType, rowCapacity);
            this.data.add(array);
        });
    }

    /**
     * Private constructor used to create filters on contents
     * @param rowKeys       the row axis
     * @param colKeys       the column axis
     * @param columnStore   true to store data in column major form
     * @param data          the data payload
     */
    private XDataFrameContent(Iterable<R> rowKeys, Iterable<C> colKeys, boolean columnStore, List<Array<?>> data) {
        this.columnStore = columnStore;
        this.rowKeys = toIndex(rowKeys);
        this.colKeys = toIndex(colKeys);
        this.data = data;
    }


    /**
     * Returns a newly created dimension for the Iterable provided
     * @param keys  the Iterable collection of keys
     * @param <K>   the key type
     * @return      the newly created dimension with keys
     */
    @SuppressWarnings("unchecked")
    private <K> Index<K> toIndex(Iterable<K> keys) {
        if (keys instanceof Index) {
            return (Index<K>)keys;
        } else if (keys instanceof Array) {
            return Index.of(keys);
        } else if (keys instanceof Range) {
            return Index.of(((Range<K>)keys).toArray());
        } else {
            final Class<K> keyType = (Class<K>)keys.iterator().next().getClass();
            final Array<K> array = ArrayBuilder.of(1000, keyType).addAll(keys).toArray();
            return Index.of(array);
        }
    }


    /**
     * Returns true if data is stored in columns, false if row store
     * @return  true if data is stored as columns
     */
    boolean isColumnStore() {
        return columnStore;
    }


    /**
     * Returns the row capacity for this content
     * @return      the row capacity for content
     */
    final int rowCapacity() {
        if (!isColumnStore() || data.size() == 0) {
            return rowKeys.capacity();
        } else {
            return data.get(0).length();
        }
    }


    /**
     * Returns the row key index for this content
     * @return      the row key index for content
     */
    final Index<R> rowKeys() {
        return rowKeys;
    }


    /**
     * Returns the column key index for this content
     * @return  the column key index for content
     */
    final Index<C> colKeys() {
        return colKeys;
    }


    /**
     * Returns the transpose of this content
     * @return  the transpose of this content
     */
    final XDataFrameContent<C,R> transpose() {
        return new XDataFrameContent<>(colKeys, rowKeys, !isColumnStore(), data);
    }


    /**
     * Returns a newly created cursor for this content
     * @param frame the frame reference
     * @return  the newly created cursor
     */
    final DataFrameCursor<R,C> cursor(XDataFrame<R,C> frame) {
        final int row = frame.rowCount() > 0 ? 0 : -1;
        final int col = frame.colCount() > 0 ? 0 : -1;
        return new Cursor().init(frame, row, col);
    }


    /**
     * Returns the most specific type that can be used to describe all elements
     * @return      the most specific type that can be used to describe all elements
     */
    private Class<?> typeInfo() {
        final Set<Class<?>> typeSet = new HashSet<>();
        this.data.forEach(array -> typeSet.add(array.type()));
        if (typeSet.size() == 1) {
            return typeSet.iterator().next();
        } else {
            return Object.class;
        }
    }


    /**
     * Returns the in-memory row coordinate for row key
     * @param rowKey    the row key
     * @return          the in-memory row coordinate
     * @throws DataFrameException   if no match for row key
     */
    final int rowCoordinate(R rowKey) throws DataFrameException {
        final int coord = rowKeys.getCoordinate(rowKey);
        if (coord >= 0) {
            return coord;
        } else {
            throw new DataFrameException("No match for row key: " + rowKey);
        }
    }


    /**
     * Returns the in-memory row coordinate for row ordinal
     * @param rowOrdinal    the row ordinal
     * @return              the in-memory row coordinate
     * @throws DataFrameException   if row ordinal out of bounds
     */
    final int rowCoordinateAt(int rowOrdinal) {
        try {
            return rowKeys.getCoordinateAt(rowOrdinal);
        } catch (IndexException ex) {
            throw new DataFrameException("DataFrame access error, row ordinal out of bounds: " + rowOrdinal, ex);
        }
    }


    /**
     * Returns the in-memory column coordinate for column key
     * @param colKey    the column key
     * @return          the in-memory column coordinate
     * @throws DataFrameException   if no match for column key
     */
    final int colCoordinate(C colKey) throws DataFrameException {
        final int coord = colKeys.getCoordinate(colKey);
        if (coord >= 0) {
            return coord;
        } else {
            throw new DataFrameException("No match for column key: " + colKey);
        }
    }


    /**
     * Returns the in-memory column coordinate for column ordinal
     * @param colOrdinal    the column ordinal
     * @return              the in-memory column coordinate
     * @throws DataFrameException   if column ordinal out of bounds
     */
    final int colCoordinateAt(int colOrdinal) {
        try {
            return colKeys.getCoordinateAt(colOrdinal);
        } catch (Exception ex) {
            throw new DataFrameException("DataFrame access error, column ordinal out of bounds: " + colOrdinal, ex);
        }
    }


    /**
     * Returns a stream of types to describe each row in this content
     * @return  the stream of row types
     */
    final Stream<Class<?>> rowTypes() {
        return isColumnStore() ? IntStream.range(0, rowKeys.size()).mapToObj(i -> typeInfo()) : data.stream().map(Array::type);
    }

    /**
     * Returns a stream of types to describe each column in this content
     * @return  the stream of column types
     */
    final Stream<Class<?>> colTypes() {
        return isColumnStore() ? data.stream().map(Array::type) : IntStream.range(0, colKeys.size()).mapToObj(i -> typeInfo());
    }


    /**
     * Returns true if the value is null at the coordinates specified
     * @param rowCoord  the row coordinate
     * @param colCoord  the column coordinate
     * @return          true if value is null
     */
    final boolean isNullAt(int rowCoord, int colCoord) {
        if (columnStore) {
            var colArray = data.get(colCoord);
            return colArray.isNull(rowCoord);
        } else {
            var rowArray = data.get(rowCoord);
            return rowArray.isNull(colCoord);
        }
    }


    /**
     * Returns the array type for the vector implied by the row key specified
     * @param rowKey    the row key
     * @return          the array type for row key
     */
    final Class<?> rowType(R rowKey) {
        if (isColumnStore()) {
            return typeInfo();
        } else {
            final int rowIndex = rowKeys.getCoordinate(rowKey);
            return data.get(rowIndex).type();
        }
    }


    /**
     * Returns the array type for the vector implied by the column key specified
     * @param colKey    the row key
     * @return          the array type for column key
     */
    final Class<?> colType(C colKey) {
        if (!isColumnStore()) {
            return typeInfo();
        } else {
            var colIndex = colKeys.getCoordinate(colKey);
            if (colIndex < 0) {
                throw new DataFrameException("No match for col key: " + colKey);
            } else {
                return data.get(colIndex).type();
            }
        }
    }


    /**
     * Returns the array for the row or column key specified
     * @param key   the row or column key if this is row or column major respectively
     * @return      the array for the key specified
     * @throws IndexException if no match for the key
     */
    @SuppressWarnings("unchecked")
    private <T> Array<T> getArray(Object key) {
        if (isColumnStore()) {
            final int index = colKeys.getCoordinate((C)key);
            return (Array<T>)data.get(index);
        } else {
            final int index = rowKeys.getCoordinate((R)key);
            return (Array<T>)data.get(index);
        }
    }


    /**
     * Returns a shallow copy of this content after mapping the row keys
     * @param mapper    the row key mapping function
     * @param <T>       the new row key type
     * @return          shallow copy of this content
     */
    final <T> XDataFrameContent<T,C> mapRowKeys(IndexMapper<R,T> mapper) {
        final Index<T> newIndex = rowKeys.map(mapper);
        return new XDataFrameContent<>(newIndex, colKeys, columnStore, data);
    }


    /**
     * Returns a shallow copy of this content after mapping the column keys
     * @param mapper    the column key mapping function
     * @param <T>       the new column key type
     * @return          shallow copy of this content
     */
    final <T> XDataFrameContent<R,T> mapColKeys(IndexMapper<C,T> mapper) {
        final Index<T> newIndex = colKeys.map(mapper);
        return new XDataFrameContent<>(rowKeys, newIndex, columnStore, data);
    }


    /**
     * Adds a new row to this content if it does not already exist
     * @param rowKey    the row key to add
     * @return          true if added
     */
    final boolean addRow(R rowKey) {
        if (!isColumnStore()) {
            throw new DataFrameException("Cannot add rows to a transposed DataFrame, call transpose() and then add columns");
        } else if (this.rowKeys.isFilter()) {
            throw new DataFrameException("Cannot add keys to a filtered axis of a DataFrame");
        } else {
            final boolean added = rowKeys.add(rowKey);
            final int rowCount = rowKeys.size();
            this.ensureCapacity(rowCount);
            return added;
        }
    }


    /**
     * Adds multiple new rows to this content based on the keys provided
     * @param rowKeys   the row keys to add
     * @return          the array of keys added
     * @throws DataFrameException   if keys already exist, and silent is false
     */
    final Array<R> addRows(Iterable<R> rowKeys) throws DataFrameException {
        if (!isColumnStore()) {
            throw new DataFrameException("This DataFrame is configured as a row store, transpose() first");
        } else if (this.rowKeys.isFilter()) {
            throw new DataFrameException("Cannot add keys to a sliced axis of a DataFrame");
        } else {
            final int preSize = this.rowKeys.size();
            final Class<R> type = this.rowKeys.type();
            final boolean ignoreDuplicates = DataFrameOptions.isIgnoreDuplicates();
            final int count = this.rowKeys.addAll(rowKeys, ignoreDuplicates);
            final Array<R> added = Array.of(type, count);
            for (int i=0; i<count; ++i) {
                final R key = this.rowKeys.getKey(preSize + i);
                added.setValue(i, key);
            }
            final int rowCount = this.rowKeys.size();
            this.ensureCapacity(rowCount);
            return added;
        }
    }


    /**
     * Ensures that the data arrays support the capacity of row index
     * @param rowCount      the row count for current row index
     */
    private void ensureCapacity(int rowCount) {
        if (data.size() > 0) {
            final int capacity = rowCapacity();
            if (rowCount > capacity) {
                final int newCapacity = capacity + (capacity >> 1);
                if (newCapacity < rowCount) {
                    this.data.forEach(s -> s.expand(rowCount));
                } else {
                    this.data.forEach(s -> s.expand(newCapacity));
                }
            }
        }
    }


    /**
     * Adds a column with the key and array data provided
     * @param key       the column key
     * @param values    the values for column
     * @return          true if the column did not already exist
     */
    @SuppressWarnings("unchecked")
    final <T> boolean addColumn(C key, Iterable<T> values) {
        if (!isColumnStore()) {
            throw new DataFrameException("This DataFrame is configured as a row store, transpose() first");
        } else {
            final boolean added = colKeys.add(key);
            if (added) {
                final Array<T> array = ArrayUtils.toArray(values);
                final int rowCapacity = rowCapacity();
                array.expand(rowCapacity);
                this.data.add(array);
            }
            return added;
        }
    }


    /**
     * Maps the specified column to booleans using the mapper function provided
     * @param frame     the frame reference
     * @param colKey    the column key to apply mapper function to
     * @param mapper    the mapper function to apply
     * @return          the newly created content, with update column
     */
    @SuppressWarnings("unchecked")
    final XDataFrameContent<R,C> mapToBooleans(XDataFrame<R,C> frame, C colKey, ToBooleanFunction<DataFrameValue<R,C>> mapper) {
        if (!isColumnStore()) {
            throw new DataFrameException("Cannot apply columns of a transposed DataFrame");
        } else {
            final int rowCount = rowKeys.size();
            final boolean parallel  = frame.isParallel();
            final int colIndex = colKeys.getCoordinate(colKey);
            return new XDataFrameContent<>(rowKeys, colKeys, true, Mapper.apply(data, parallel, (index, array) -> {
                if (index != colIndex) {
                    return array;
                } else {
                    final int colOrdinal = colKeys.getOrdinal(colKey);
                    final Array<?> targetValues = Array.of(Boolean.class, array.length());
                    final Cursor cursor = new Cursor().init(frame, rowKeys.isEmpty() ? -1 : 0, colOrdinal);
                    for (int i = 0; i < rowCount; ++i) {
                        cursor.toRowAt(i);
                        final boolean value = mapper.applyAsBoolean(cursor);
                        targetValues.setBoolean(cursor.rowIndex, value);
                    }
                    return targetValues;
                }
            }));
        }
    }


    /**
     * Maps the specified column to ints using the mapper function provided
     * @param frame     the frame reference
     * @param colKey    the column key to apply mapper function to
     * @param mapper    the mapper function to apply
     * @return          the newly created content, with update column
     */
    @SuppressWarnings("unchecked")
    final XDataFrameContent<R,C> mapToInts(XDataFrame<R,C> frame, C colKey, ToIntFunction<DataFrameValue<R,C>> mapper) {
        if (!isColumnStore()) {
            throw new DataFrameException("Cannot apply columns of a transposed DataFrame");
        } else {
            final int rowCount = rowKeys.size();
            final boolean parallel  = frame.isParallel();
            final int colIndex = colKeys.getCoordinate(colKey);
            return new XDataFrameContent<>(rowKeys, colKeys, true, Mapper.apply(data, parallel, (index, array) -> {
                if (index != colIndex) {
                    return array;
                } else {
                    final int colOrdinal = colKeys.getOrdinal(colKey);
                    final Array<?> targetValues = Array.of(Integer.class, array.length());
                    final Cursor cursor = new Cursor().init(frame, rowKeys.isEmpty() ? -1 : 0, colOrdinal);
                    for (int i = 0; i < rowCount; ++i) {
                        cursor.toRowAt(i);
                        final int value = mapper.applyAsInt(cursor);
                        targetValues.setInt(cursor.rowIndex, value);
                    }
                    return targetValues;
                }
            }));
        }
    }


    /**
     * Maps the specified column to longs using the mapper function provided
     * @param frame     the frame reference
     * @param colKey    the column key to apply mapper function to
     * @param mapper    the mapper function to apply
     * @return          the newly created content, with update column
     */
    @SuppressWarnings("unchecked")
    final XDataFrameContent<R,C> mapToLongs(XDataFrame<R,C> frame, C colKey, ToLongFunction<DataFrameValue<R,C>> mapper) {
        if (!isColumnStore()) {
            throw new DataFrameException("Cannot apply columns of a transposed DataFrame");
        } else {
            final int rowCount = rowKeys.size();
            final boolean parallel  = frame.isParallel();
            final int colIndex = colKeys.getCoordinate(colKey);
            return new XDataFrameContent<>(rowKeys, colKeys, true, Mapper.apply(data, parallel, (index, array) -> {
                if (index != colIndex) {
                    return array;
                } else {
                    final int colOrdinal = colKeys.getOrdinal(colKey);
                    final Array<?> targetValues = Array.of(Long.class, array.length());
                    final Cursor cursor = new Cursor().init(frame, rowKeys.isEmpty() ? -1 : 0, colOrdinal);
                    for (int i = 0; i < rowCount; ++i) {
                        cursor.toRowAt(i);
                        final long value = mapper.applyAsLong(cursor);
                        targetValues.setLong(cursor.rowIndex, value);
                    }
                    return targetValues;
                }
            }));
        }
    }


    /**
     * Maps the specified column to doubles using the mapper function provided
     * @param frame     the frame reference
     * @param colKey    the column key to apply mapper function to
     * @param mapper    the mapper function to apply
     * @return          the newly created content, with update column
     */
    @SuppressWarnings("unchecked")
    final XDataFrameContent<R,C> mapToDoubles(XDataFrame<R,C> frame, C colKey, ToDoubleFunction<DataFrameValue<R,C>> mapper) {
        if (!isColumnStore()) {
            throw new DataFrameException("Cannot apply columns of a transposed DataFrame");
        } else {
            final int rowCount = rowKeys.size();
            final boolean parallel  = frame.isParallel();
            final int colIndex = colKeys.getCoordinate(colKey);
            return new XDataFrameContent<>(rowKeys, colKeys, true, Mapper.apply(data, parallel, (index, array) -> {
                if (index != colIndex) {
                    return array;
                } else {
                    final int colOrdinal = colKeys.getOrdinal(colKey);
                    final Array<?> targetValues = Array.of(Double.class, array.length());
                    final Cursor cursor = new Cursor().init(frame, rowKeys.isEmpty() ? -1 : 0, colOrdinal);
                    for (int i = 0; i < rowCount; ++i) {
                        cursor.toRowAt(i);
                        final double value = mapper.applyAsDouble(cursor);
                        targetValues.setDouble(cursor.rowIndex, value);
                    }
                    return targetValues;
                }
            }));
        }
    }


    /**
     * Maps the specified column to objects using the mapper function provided
     * @param frame     the frame reference
     * @param colKey    the column key to apply mapper function to
     * @param type      the data type for mapped column
     * @param mapper    the mapper function to apply
     * @return          the newly created content, with update column
     */
    @SuppressWarnings("unchecked")
    final <T> XDataFrameContent<R,C> mapToObjects(XDataFrame<R,C> frame, C colKey, Class<T> type, Function<DataFrameValue<R,C>,T> mapper) {
        if (!isColumnStore()) {
            throw new DataFrameException("Cannot map columns of a transposed DataFrame, call copy() first");
        } else {
            final int rowCount = rowKeys.size();
            final boolean parallel  = frame.isParallel();
            final int colIndex = colKeys.getCoordinate(colKey);
            return new XDataFrameContent<>(rowKeys, colKeys, true, Mapper.apply(data, parallel, (index, array) -> {
                if (index != colIndex) {
                    return array;
                } else {
                    final int colOrdinal = colKeys.getOrdinal(colKey);
                    final Array<T> targetValues = Array.of(type, array.length());
                    final Cursor cursor = new Cursor().init(frame, rowKeys.isEmpty() ? -1 : 0, colOrdinal);
                    for (int i = 0; i < rowCount; ++i) {
                        cursor.toRowAt(i);
                        final T value = mapper.apply(cursor);
                        targetValues.setValue(cursor.rowIndex, value);
                    }
                    return targetValues;
                }
            }));
        }
    }


    /**
     * Returns a filter of this contents based on the row and column keys provided
     * @param newRowKeys   the optionally filtered row keys
     * @param newColKeys   the optionally filtered column keys
     */
    final XDataFrameContent<R,C> filter(Index<R> newRowKeys, Index<C> newColKeys) {
        return new XDataFrameContent<>(newRowKeys, newColKeys, columnStore, data);
    }


    /**
     * Returns a newly created comparator to sort this content in the row dimension
     * @param colKeys       the column keys to sort rows by, in order of precedence
     * @param multiplier    the multiplier to control ascending / descending
     * @return              the newly created comparator
     */
    final XDataFrameComparator createRowComparator(List<C> colKeys, int multiplier) {
        final XDataFrameComparator[] comparators = new XDataFrameComparator[colKeys.size()];
        for (int i=0; i<colKeys.size(); ++i) {
            final C colKey = colKeys.get(i);
            final Array<?> array = getColArray(colKey);
            comparators[i] = XDataFrameComparator.create(array, multiplier);
        }
        return XDataFrameComparator.create(comparators).withIndex(rowKeys);
    }


    /**
     * Returns a newly created comparator to sort this content in the column dimension
     * @param rowKeys       the row keys to sort columns by, in order of precedence
     * @param multiplier    the multiplier to control ascending / descending
     * @return              the newly created comparator
     */
    final XDataFrameComparator createColComparator(List<R> rowKeys, int multiplier) {
        final XDataFrameComparator[] comparators = new XDataFrameComparator[rowKeys.size()];
        for (int i=0; i<rowKeys.size(); ++i) {
            final R rowKey = rowKeys.get(i);
            final Array<?> array = getRowArray(rowKey);
            comparators[i] = XDataFrameComparator.create(array, multiplier);
        }
        return XDataFrameComparator.create(comparators).withIndex(colKeys);
    }


    /**
     * Returns row data as an array for internal use only
     * @param rowKey    the row key
     * @return          the array of row data
     */
    private Array<?> getRowArray(R rowKey) {
        final Class<?> type = rowType(rowKey);
        final Array<?> array = Array.of(type, colKeys.size());
        final int rowIndex = rowKeys.getCoordinate(rowKey);
        switch (ArrayType.of(type)) {
            case BOOLEAN:           return array.applyBooleans(v -> booleanAt(rowIndex, colCoordinateAt(v.index())));
            case INTEGER:           return array.applyInts(v -> intAt(rowIndex, colCoordinateAt(v.index())));
            case LONG:              return array.applyLongs(v -> longAt(rowIndex, colCoordinateAt(v.index())));
            case DOUBLE:            return array.applyDoubles(v -> doubleAt(rowIndex, colCoordinateAt(v.index())));
            case DATE:              return array.applyLongs(v -> longAt(rowIndex, colCoordinateAt(v.index())));
            case INSTANT:           return array.applyLongs(v -> longAt(rowIndex, colCoordinateAt(v.index())));
            case LOCAL_DATE:        return array.applyLongs(v -> longAt(rowIndex, colCoordinateAt(v.index())));
            case LOCAL_TIME:        return array.applyLongs(v -> longAt(rowIndex, colCoordinateAt(v.index())));
            case LOCAL_DATETIME:    return array.applyLongs(v -> longAt(rowIndex, colCoordinateAt(v.index())));
            default:                return array.applyValues(v -> valueAt(rowIndex, colCoordinateAt(v.index())));
        }
    }


    /**
     * Returns column data as an array for internal use only
     * @param colKey    the column key
     * @return          the array of column data
     */
    private Array<?> getColArray(C colKey) {
        final Class<?> type = colType(colKey);
        final Array<?> array = Array.of(type, rowKeys.size());
        final int colIndex = colKeys.getCoordinate(colKey);
        switch (ArrayType.of(type)) {
            case BOOLEAN:           return array.applyBooleans(v -> booleanAt(rowCoordinateAt(v.index()), colIndex));
            case INTEGER:           return array.applyInts(v -> intAt(rowCoordinateAt(v.index()), colIndex));
            case LONG:              return array.applyLongs(v -> longAt(rowCoordinateAt(v.index()), colIndex));
            case DOUBLE:            return array.applyDoubles(v -> doubleAt(rowCoordinateAt(v.index()), colIndex));
            case DATE:              return array.applyLongs(v -> longAt(rowCoordinateAt(v.index()), colIndex));
            case INSTANT:           return array.applyLongs(v -> longAt(rowCoordinateAt(v.index()), colIndex));
            case LOCAL_DATE:        return array.applyLongs(v -> longAt(rowCoordinateAt(v.index()), colIndex));
            case LOCAL_TIME:        return array.applyLongs(v -> longAt(rowCoordinateAt(v.index()), colIndex));
            case LOCAL_DATETIME:    return array.applyLongs(v -> longAt(rowCoordinateAt(v.index()), colIndex));
            default:                return array.applyValues(v -> valueAt(rowCoordinateAt(v.index()), colIndex));
        }
    }


    /**
     * Returns a deep copy of this contents
     * @return  a deep copy of this contents
     */
    final XDataFrameContent<R,C> copy() {
        return isColumnStore() ? copyColumnStore() : copyRowStore();
    }


    /**
     * Returns a deep copy of this content which will turn a row-store into a column-store
     * @return  the deep copy of this content
     */
    @SuppressWarnings("unchecked")
    private XDataFrameContent<R,C> copyRowStore() {
        final int rowCount = rowKeys.size();
        final Array<R> rowKeys = rowKeys().toArray();
        final Index<C> colKeys = Index.of(colKeys().type(), colKeys().size());
        final XDataFrameContent<R,C> newContent = new XDataFrameContent<>(rowKeys, colKeys, Object.class);
        this.colKeys().forEach(colKey -> {
            final Class<?> dataType = colType(colKey);
            final ArrayType type = ArrayType.of(dataType);
            final int colIndex = colKeys.getCoordinate(colKey);
            final Array<Object> array = Array.of((Class<Object>)dataType, rowCount);
            newContent.addColumn(colKey, array);
            if (type.isBoolean()) {
                for (int i=0; i<rowCount; ++i) {
                    final int rowIndex = rowCoordinateAt(i);
                    final boolean value = booleanAt(rowIndex, colIndex);
                    array.setBoolean(i, value);
                }
            } else if (type.isInteger()) {
                for (int i=0; i<rowCount; ++i) {
                    final int rowIndex = rowCoordinateAt(i);
                    final int value = intAt(rowIndex, colIndex);
                    array.setInt(i, value);
                }
            } else if (type.isLong()) {
                for (int i=0; i<rowCount; ++i) {
                    final int rowIndex = rowCoordinateAt(i);
                    final long value = longAt(rowIndex, colIndex);
                    array.setLong(i, value);
                }
            } else if (type.isDouble()) {
                for (int i = 0; i < rowCount; ++i) {
                    final int rowIndex = rowCoordinateAt(i);
                    final double value = doubleAt(rowIndex, colIndex);
                    array.setDouble(i, value);
                }
            } else {
                for (int i = 0; i < rowCount; ++i) {
                    final int rowIndex = rowCoordinateAt(i);
                    final Object value = valueAt(rowIndex, colIndex);
                    array.setValue(i, value);
                }
            }
        });
        return newContent;
    }


    /**
     * Returns a deep copy of this content which is expressed as a column store
     * @return  the deep copy of this content
     */
    @SuppressWarnings("unchecked")
    private XDataFrameContent<R,C> copyColumnStore() {
        try {
            if (rowKeys().isFilter()) {
                final Array<R> rowKeys = this.rowKeys.toArray();
                final Array<C> colKeys = this.colKeys.toArray();
                final int[] modelIndexes = this.rowKeys.indexes().toArray();
                final Index<R> newRowAxis = Index.of(rowKeys);
                final Index<C> newColAxis = Index.of(colKeys);
                final List<Array<?>> newData = this.colKeys.keys().map(c -> getArray(c).copy(modelIndexes)).collect(Collectors.toList());
                return new XDataFrameContent<>(newRowAxis, newColAxis, columnStore, newData);
            } else if (colKeys().isFilter()) {
                final Array<C> colKeys = this.colKeys.toArray();
                final Index<R> newRowAxis = rowKeys.copy();
                final Index<C> newColAxis = Index.of(colKeys);
                final List<Array<?>> newData = this.colKeys.keys().map(c -> getArray(c).copy()).collect(Collectors.toList());
                return new XDataFrameContent<>(newRowAxis, newColAxis, columnStore, newData);
            } else {
                final XDataFrameContent<R,C> clone = (XDataFrameContent<R,C>)super.clone();
                clone.data = this.data.stream().map(Array::copy).collect(Collectors.toList());
                clone.rowKeys = this.rowKeys.copy();
                clone.colKeys = this.colKeys.copy();
                return clone;
            }
        } catch (CloneNotSupportedException ex) {
            throw new DataFrameException("Clone operation not supported", ex);
        }
    }

    /**
     * Returns the value given the in-memory coordinates
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @return          the value for coordinates
     */
    final boolean booleanAt(int rowIndex, int colIndex) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return colArray.getBoolean(rowIndex);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return rowArray.getBoolean(colIndex);
        }
    }


    /**
     * Returns the value given the in-memory coordinates
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @return          the value for coordinates
     */
    final int intAt(int rowIndex, int colIndex) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return colArray.getInt(rowIndex);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return rowArray.getInt(colIndex);
        }
    }


    /**
     * Returns the value given the in-memory coordinates
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @return          the value for coordinates
     */
    final long longAt(int rowIndex, int colIndex) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return colArray.getLong(rowIndex);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return rowArray.getLong(colIndex);
        }
    }


    /**
     * Returns the value given the in-memory coordinates
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @return          the value for coordinates
     */
    final double doubleAt(int rowIndex, int colIndex) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return colArray.getDouble(rowIndex);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return rowArray.getDouble(colIndex);
        }
    }


    /**
     * Returns the value given the in-memory coordinates
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @return          the value for coordinates
     */
    @SuppressWarnings("unchecked")
    final <V> V valueAt(int rowIndex, int colIndex) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return (V)colArray.getValue(rowIndex);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return (V)rowArray.getValue(colIndex);
        }
    }


    /**
     * Sets the value at the in-memory coordinates provided
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @param value     the value to set
     * @return          the previous value
     */
    final boolean booleanAt(int rowIndex, int colIndex, boolean value) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return colArray.setBoolean(rowIndex, value);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return rowArray.setBoolean(colIndex, value);
        }
    }


    /**
     * Sets the value at the in-memory coordinates provided
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @param value     the value to set
     * @return          the previous value
     */
    final int intAt(int rowIndex, int colIndex, int value) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return colArray.setInt(rowIndex, value);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return rowArray.setInt(colIndex, value);
        }
    }


    /**
     * Sets the value at the in-memory coordinates provided
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @param value     the value to set
     * @return          the previous value
     */
    final long longAt(int rowIndex, int colIndex, long value) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return colArray.setLong(rowIndex, value);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return rowArray.setLong(colIndex, value);
        }
    }


    /**
     * Sets the value at the in-memory coordinates provided
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @param value     the value to set
     * @return          the previous value
     */
    final double doubleAt(int rowIndex, int colIndex, double value) {
        if (columnStore) {
            final Array<?> colArray = data.get(colIndex);
            return colArray.setDouble(rowIndex, value);
        } else {
            final Array<?> rowArray = data.get(rowIndex);
            return rowArray.setDouble(colIndex, value);
        }
    }


    /**
     * Sets the value at the in-memory coordinates provided
     * @param rowIndex  the in-memory row index coordinate
     * @param colIndex  the in-memory column index coordinate
     * @param value     the value to set
     * @return          the previous value
     */
    @SuppressWarnings("unchecked")
    final <V> V valueAt(int rowIndex, int colIndex, V value) {
        if (columnStore) {
            final Array<V> colArray = (Array<V>)data.get(colIndex);
            return colArray.setValue(rowIndex, value);
        } else {
            final Array<V> rowArray = (Array<V>)data.get(rowIndex);
            return rowArray.setValue(colIndex, value);
        }
    }


    /**
     * Custom object serialization method for improved performance
     * @param is    the input stream
     * @throws IOException  if read fails
     * @throws ClassNotFoundException   if read fails
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        final int rowCount = is.readInt();
        final int colCount = is.readInt();
        final Class<R> rowType = (Class<R>)is.readObject();
        final Class<C> colType = (Class<C>)is.readObject();
        this.columnStore = is.readBoolean();
        this.rowKeys = Index.of(rowType, rowCount);
        this.colKeys = Index.of(colType, colCount);
        if (columnStore) {
            for (int i=0; i<rowCount; ++i) {
                final R rowKey = (R)is.readObject();
                this.rowKeys.add(rowKey);
            }
            this.data = new ArrayList<>(colCount);
            for (int j=0; j<colCount; ++j) {
                final C colKey = (C)is.readObject();
                final Class<?> type = (Class<?>)is.readObject();
                final Array array = Array.of(type, rowCount);
                array.read(is, rowCount);
                this.data.add(array);
                this.colKeys.add(colKey);
            }
        } else {
            for (int i=0; i<colCount; ++i) {
                final C rowKey = (C)is.readObject();
                this.colKeys.add(rowKey);
            }
            this.data = new ArrayList<>(rowCount);
            for (int j=0; j<rowCount; ++j) {
                final R rowKey = (R)is.readObject();
                final Class<?> type = (Class<?>)is.readObject();
                final Array array = Array.of(type, rowCount);
                array.read(is, rowCount);
                this.data.add(array);
                this.rowKeys.add(rowKey);
            }
        }
    }


    /**
     * Custom object serialization method for improved performance
     * @param os    the output stream
     * @throws IOException  if write fails
     */
    private void writeObject(ObjectOutputStream os) throws IOException {
        final int rowCount = rowKeys.size();
        final int colCount = colKeys.size();
        os.writeInt(rowCount);
        os.writeInt(colCount);
        os.writeObject(rowKeys.type());
        os.writeObject(colKeys.type());
        os.writeBoolean(columnStore);
        if (isColumnStore()) {
            for (int i=0; i<rowCount; ++i) {
                final R rowKey = rowKeys.getKey(i);
                os.writeObject(rowKey);
            }
            final int[] indexes = rowKeys.indexes().toArray();
            for (int j=0; j<colCount; ++j) {
                final C colKey = colKeys().getKey(j);
                final Array<?> array = data.get(j);
                final Class<?> type = array.type();
                os.writeObject(colKey);
                os.writeObject(type);
                array.write(os, indexes);
            }
        } else {
            for (int i=0; i<colCount; ++i) {
                final C colKey = colKeys.getKey(i);
                os.writeObject(colKey);
            }
            final int[] indexes = colKeys.indexes().toArray();
            for (int j=0; j<rowCount; ++j) {
                final R rowKey = rowKeys.getKey(j);
                final Array<?> array = data.get(j);
                final Class<?> type = array.type();
                os.writeObject(rowKey);
                os.writeObject(type);
                array.write(os, indexes);
            }
        }
    }


    /**
     * A DataFrameCursor implementation that operates in view space
     */
    private class Cursor implements DataFrameCursor<R,C> {

        private int rowIndex;
        private int colIndex;
        private int rowOrdinal;
        private int colOrdinal;
        private Array<?> array;
        private XDataFrame<R,C> frame;
        private XDataFrameRow<R,C> row;
        private XDataFrameColumn<R,C> column;

        /**
         * Constructor
         */
        private Cursor() {
            super();
        }

        /**
         * Initializes this cursor for the frame at the location specified
         * @param frame         the frame reference
         * @param rowOrdinal    the initial row ordinal
         * @param colOrdinal    the initial column ordinal
         * @return              this cursor
         */
        private Cursor init(XDataFrame<R,C> frame, int rowOrdinal, int colOrdinal) {
            this.frame = frame;
            if (rowOrdinal >= 0) toRowAt(rowOrdinal);
            if (colOrdinal >= 0) toColAt(colOrdinal);
            return this;
        }

        @Override
        public final R rowKey() {
            return rowOrdinal < 0 ? null : rowKeys.getKey(rowOrdinal);
        }

        @Override
        public final C colKey() {
            return colOrdinal < 0 ? null : colKeys.getKey(colOrdinal);
        }

        @Override
        public final int rowOrdinal() {
            return rowOrdinal;
        }

        @Override
        public final int colOrdinal() {
            return colOrdinal;
        }

        @Override
        public final DataFrame<R,C> frame() {
            return frame;
        }

        @Override
        public final boolean isBoolean() {
            return array.typeCode() == ArrayType.BOOLEAN;
        }

        @Override
        public final boolean isInteger() {
            return array.typeCode() == ArrayType.INTEGER;
        }

        @Override
        public final boolean isLong() {
            return array.typeCode() == ArrayType.LONG;
        }

        @Override
        public final boolean isDouble() {
            return array.typeCode() == ArrayType.DOUBLE;
        }

        @Override
        public final boolean isNumeric() {
            return isInteger() || isLong() || isDouble();
        }

        @Override
        public boolean isString() {
            return array.typeCode() == ArrayType.STRING;
        }

        @Override
        public boolean isDate() {
            return array.typeCode() == ArrayType.DATE;
        }

        @Override
        public boolean isLocalDate() {
            return array.typeCode() == ArrayType.LOCAL_DATE;
        }


        @Override
        public final boolean isNull() {
            try {
                if (columnStore) {
                    return array.isNull(rowIndex);
                } else {
                    return array.isNull(colIndex);
                }
            } catch (Exception ex) {
                throw new DataFrameException("DataFrame read error at (" + rowKey() + ", " + colKey() + "): " + ex.getMessage(), ex);
            }
        }

        @Override
        public final boolean notNull() {
            return !isNull();
        }

        @Override()
        public final boolean isEqualTo(Object value) {
            if (value == null) {
                return isNull();
            } else if (value instanceof Number) {
                final Number num = (Number)value;
                switch (array.typeCode()) {
                    case INTEGER:   return num.intValue() == getInt();
                    case LONG:      return num.longValue() == getLong();
                    case DOUBLE:    return Double.compare(num.doubleValue(), getDouble()) == 0;
                }
            }
            final Object thisValue = getValue();
            return Objects.equals(value, thisValue);
        }

        @Override
        public final DataFrameRow<R,C> row() {
            if (row == null) {
                this.row = new XDataFrameRow<>(frame, false, rowOrdinal);
                return row;
            } else {
                this.row.moveTo(rowOrdinal);
                return row;
            }
        }

        @Override
        public final DataFrameColumn<R,C> col() {
            if (column == null) {
                this.column = new XDataFrameColumn<>(frame, false, colOrdinal);
                return column;
            } else {
                this.column.moveTo(colOrdinal);
                return column;
            }
        }

        @Override
        public final boolean getBoolean() {
            try {
                if (columnStore) {
                    return array.getBoolean(rowIndex);
                } else {
                    return array.getBoolean(colIndex);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame read error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        public final int getInt() {
            try {
                if (columnStore) {
                    return array.getInt(rowIndex);
                } else {
                    return array.getInt(colIndex);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame read error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        public final long getLong() {
            try {
                if (columnStore) {
                    return array.getLong(rowIndex);
                } else {
                    return array.getLong(colIndex);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame read error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        public final double getDouble() {
            try {
                if (columnStore) {
                    return array.getDouble(rowIndex);
                } else {
                    return array.getDouble(colIndex);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame read error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <V> V getValue() {
            try {
                if (columnStore) {
                    return (V)array.getValue(rowIndex);
                } else {
                    return (V)array.getValue(colIndex);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame read error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        public final void setBoolean(boolean value) {
            try {
                if (columnStore) {
                    array.setBoolean(rowIndex, value);
                } else {
                    array.setBoolean(colIndex, value);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame write error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        public final void setInt(int value) {
            try {
                if (columnStore) {
                    array.setInt(rowIndex, value);
                } else {
                    array.setInt(colIndex, value);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame write error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        public final void setLong(long value) {
            try {
                if (columnStore) {
                    array.setLong(rowIndex, value);
                } else {
                    array.setLong(colIndex, value);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame write error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        public final void setDouble(double value) {
            try {
                if (columnStore) {
                    array.setDouble(rowIndex, value);
                } else {
                    array.setDouble(colIndex, value);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame write error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public final <V> void setValue(V value) {
            try {
                if (columnStore) {
                    ((Array<V>)array).setValue(rowIndex, value);
                } else {
                    ((Array<V>)array).setValue(colIndex, value);
                }
            } catch (Throwable t) {
                throw new DataFrameException("DataFrame write error at (" + rowKey() + ", " + colKey() + "): ", t);
            }
        }

        @Override
        public final DataFrameCursor<R,C> toRowAt(int ordinal) {
            try {
                this.rowIndex = rowKeys.getCoordinateAt(ordinal);
                this.array = columnStore ? array : data.get(rowIndex);
                this.rowOrdinal = ordinal;
                return this;
            } catch (Throwable t) {
                throw new DataFrameException("Failed to move cursor to row ordinal " + ordinal, t);
            }
        }

        @Override
        public final DataFrameCursor<R,C> toColAt(int ordinal) {
            try {
                this.colIndex = colKeys.getCoordinateAt(ordinal);
                this.array = columnStore ? data.get(colIndex) : array;
                this.colOrdinal = ordinal;
                return this;
            } catch (Throwable t) {
                throw new DataFrameException("Failed to move cursor to column ordinal " + ordinal, t);
            }
        }

        @Override
        public final DataFrameCursor<R,C> copy() {
            return new Cursor().init(frame, rowOrdinal, colOrdinal);
        }

        @Override
        public final DataFrameCursor<R,C> toRow(R key) {
            try {
                final int ordinal = rowKeys.getOrdinal(key);
                return toRowAt(ordinal);
            } catch (DataFrameException ex) {
                throw ex;
            } catch (Throwable t) {
                throw new DataFrameException("Failed to low row for " + key, t);
            }
        }

        @Override
        public final DataFrameCursor<R,C> toCol(C key) {
            try {
                final int ordinal = colKeys.getOrdinal(key);
                return toColAt(ordinal);
            } catch (DataFrameException ex) {
                throw ex;
            } catch (Throwable t) {
                throw new DataFrameException("Failed to low row for " + key, t);
            }
        }

        @Override
        public final DataFrameCursor<R,C> atKeys(R rowKey, C colKey) {
            return toRow(rowKey).toCol(colKey);
        }

        @Override
        public final DataFrameCursor<R,C> at(int rowOrdinal, int colOrdinal) {
            return toRowAt(rowOrdinal).toColAt(colOrdinal);
        }

        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(DataFrameValue<R,C> other) {
            final Object v1 = getValue();
            final Object v2 = other.getValue();
            try {
                if (v1 == v2) {
                    return 0;
                } else if (v1 == null) {
                    return -1;
                } else if (v2 == null) {
                    return 1;
                } else if (v1.getClass() == v2.getClass()) {
                    if (v1 instanceof Comparable) {
                        final Comparable c1 = (Comparable)v1;
                        final Comparable c2 = (Comparable)v2;
                        return c1.compareTo(c2);
                    } else {
                        final String s1 = v1.toString();
                        final String s2 = v2.toString();
                        return s1.compareTo(s2);
                    }
                } else {
                    return 0;
                }
            } catch (Exception ex) {
                throw new DataFrameException("Failed to compare DataFrameValues: " + v1 + " vs " + v2);
            }
        }
    }

}
