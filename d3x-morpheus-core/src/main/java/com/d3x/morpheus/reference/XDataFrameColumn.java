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

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameCursor;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.util.Bounds;

/**
 * An implementation of DataFrameVector used to represent a column vector in a DataFrame.
 *
 * @param <C>   the column key type
 * @param <R>   the row key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class XDataFrameColumn<R,C> extends XDataFrameVector<C,R,R,C,DataFrameColumn<R,C>> implements DataFrameColumn.Cursor<R,C> {

    private static final long serialVersionUID = 1L;

    private C colKey;
    private int colIndex = -1;
    private int colOrdinal = -1;
    private XDataFrame<R,C> frame;
    private XDataFrameContent<R,C> data;

    /**
     * Constructor
     * @param frame     the frame reference
     * @param parallel  true for parallel implementation
     */
    XDataFrameColumn(XDataFrame<R,C> frame, boolean parallel) {
        this(frame, parallel, -1);
    }

    /**
     * Constructor
     * @param frame     the frame reference
     * @param parallel  true for parallel implementation
     * @param ordinal   the column ordinal in view space
     */
    XDataFrameColumn(XDataFrame<R,C> frame, boolean parallel, int ordinal) {
        super(frame, false, parallel);
        this.frame = frame;
        this.data = frame.content();
        if (ordinal >= 0) {
            this.atOrdinal(ordinal);
        }
    }


    @Override()
    public final DataFrameColumn<R,C> forEachValue(Consumer<DataFrameValue<R,C>> consumer) {
        final int rowCount = frame.rowCount();
        if (rowCount > 0) {
            final int colOrdinal = ordinal();
            final DataFrameCursor<R,C> cursor = frame.cursor().at(0, colOrdinal);
            for (int i=0; i<rowCount; ++i) {
                cursor.rowAt(i);
                consumer.accept(cursor);
            }
        }
        return this;
    }


    @Override
    public final Iterator<DataFrameValue<R,C>> iterator() {
        final int colOrdinal = ordinal();
        final DataFrameCursor<R,C> cursor = frame.cursor().colAt(colOrdinal);
        return new Iterator<>() {
            private int ordinal = 0;
            @Override
            public DataFrameValue<R,C> next() {
                return cursor.rowAt(ordinal++);
            }
            @Override
            public boolean hasNext() {
                return ordinal < frame.rowCount();
            }
        };
    }


    @Override
    public final Iterator<DataFrameValue<R,C>> iterator(Predicate<DataFrameValue<R,C>> predicate) {
        final int colOrdinal = ordinal();
        final DataFrameCursor<R,C> cursor = frame.cursor().colAt(colOrdinal);
        return new Iterator<>() {
            private int ordinal = 0;
            @Override
            public DataFrameValue<R,C> next() {
                return cursor.rowAt(ordinal++);
            }
            @Override
            public boolean hasNext() {
                while (ordinal < frame.rowCount()) {
                    cursor.rowAt(ordinal);
                    if (predicate == null || predicate.test(cursor)) {
                        return true;
                    } else {
                        ordinal++;
                    }
                }
                return false;
            }
        };
    }


    @Override()
    public final Optional<DataFrameValue<R,C>> first(Predicate<DataFrameValue<R,C>> predicate) {
        final DataFrameCursor<R,C> value = frame().cursor();
        value.colAt(ordinal());
        for (int i=0; i<size(); ++i) {
            value.rowAt(i);
            if (predicate.test(value)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }


    @Override()
    public final Optional<DataFrameValue<R,C>> last(Predicate<DataFrameValue<R,C>> predicate) {
        final DataFrameCursor<R,C> value = frame().cursor();
        value.colAt(ordinal());
        for (int i=size()-1; i>=0; --i) {
            value.rowAt(i);
            if (predicate.test(value)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }


    @Override()
    public final Optional<DataFrameValue<R,C>> min(Predicate<DataFrameValue<R,C>> predicate) {
        if (frame.rowCount() == 0 || frame.colCount() == 0) {
            return Optional.empty();
        } else {
            return first(predicate).map(first -> {
                final int rowStart = first.rowOrdinal();
                final DataFrameCursor<R,C> result = frame.cursor().at(first.rowOrdinal(), first.colOrdinal());
                final DataFrameCursor<R,C> value = frame.cursor().at(first.rowOrdinal(), first.colOrdinal());
                for (int i=rowStart+1; i<frame.rowCount(); ++i) {
                    value.rowAt(i);
                    if (predicate.test(value) && value.compareTo(result) < 0) {
                        result.rowAt(value.rowOrdinal());
                    }
                }
                return result;
            });
        }
    }


    @Override()
    public final Optional<DataFrameValue<R,C>> max(Predicate<DataFrameValue<R,C>> predicate) {
        if (frame.rowCount() == 0 || frame.colCount() == 0) {
            return Optional.empty();
        } else {
            return first(predicate).map(first -> {
                final int rowStart = first.rowOrdinal();
                final DataFrameCursor<R,C> result = frame.cursor().at(first.rowOrdinal(), first.colOrdinal());
                final DataFrameCursor<R,C> value = frame.cursor().at(first.rowOrdinal(), first.colOrdinal());
                for (int i=rowStart+1; i<frame.rowCount(); ++i) {
                    value.rowAt(i);
                    if (predicate.test(value) && value.compareTo(result) > 0) {
                        result.rowAt(value.rowOrdinal());
                    }
                }
                return result;
            });
        }
    }


    @Override
    public final Optional<DataFrameValue<R,C>> min(Comparator<DataFrameValue<R, C>> comparator) {
        if (frame.rowCount() == 0 || frame.colCount() == 0) {
            return Optional.empty();
        } else {
            final C colKey = key();
            final DataFrameCursor<R,C> result = frame.cursor().col(colKey);
            final DataFrameCursor<R,C> value = frame.cursor().col(colKey);
            for (int i=0; i<frame.rowCount(); ++i) {
                value.rowAt(i);
                if (comparator.compare(value, result) < 0) {
                    result.rowAt(value.rowOrdinal());
                }
            }
            return Optional.of(result);
        }
    }


    @Override
    public final Optional<DataFrameValue<R,C>> max(Comparator<DataFrameValue<R, C>> comparator) {
        if (frame.rowCount() == 0 || frame.colCount() == 0) {
            return Optional.empty();
        } else {
            final C colKey = key();
            final DataFrameCursor<R,C> result = frame.cursor().col(colKey);
            final DataFrameCursor<R,C> value = frame.cursor().col(colKey);
            for (int i=0; i<frame.rowCount(); ++i) {
                value.rowAt(i);
                if (comparator.compare(value, result) > 0) {
                    result.rowAt(value.rowOrdinal());
                }
            }
            return Optional.of(result);
        }
    }


    @Override
    public <V> Optional<Bounds<V>> bounds(Predicate<DataFrameValue<R,C>> predicate) {
        if (frame.rowCount() == 0 || frame.colCount() == 0) {
            return Optional.empty();
        } else {
            return first(predicate).map(first -> {
                final int rowStart = first.rowOrdinal();
                final DataFrameCursor<R,C> cursor = frame.cursor().at(first.rowOrdinal(), first.colOrdinal());
                final DataFrameCursor<R,C> minValue = frame.cursor().at(first.rowOrdinal(), first.colOrdinal());
                final DataFrameCursor<R,C> maxValue = frame.cursor().at(first.rowOrdinal(), first.colOrdinal());
                for (int i=rowStart+1; i<frame.rowCount(); ++i) {
                    cursor.rowAt(i);
                    if (predicate.test(cursor)) {
                        if (minValue.compareTo(cursor) < 0) {
                            minValue.rowAt(cursor.rowOrdinal());
                        }
                        if (maxValue.compareTo(cursor) > 0) {
                            maxValue.rowAt(cursor.rowOrdinal());
                        }
                    }
                }
                final V lower = minValue.getValue();
                final V upper = maxValue.getValue();
                return Bounds.of(lower, upper);
            });
        }
    }


    @Override
    public boolean isNull(R key) {
        var rowCoord = data.rowCoordinate(key);
        var colCoord = data.colCoordinate(colKey);
        return data.isNullAt(rowCoord, colCoord);
    }


    @Override
    public boolean isNullAt(int ordinal) {
        var rowCoord = data.rowCoordinateAt(ordinal);
        var colCoord = data.colCoordinate(colKey);
        return data.isNullAt(rowCoord, colCoord);
    }



    @Override()
    public final DataFrame<R,C> rank() {
        final double[] values = toDoubleStream().toArray();
        final double[] ranks = XDataFrameRank.rank(values);
        final Array<R> rowKeys = frame.rowKeys().toArray();
        return DataFrame.ofDoubles(rowKeys, key()).applyDoubles(v -> ranks[v.rowOrdinal()]);
    }


    @Override()
    public final boolean isNumeric() {
        return ArrayType.of(dataClass()).isNumeric();
    }


    @Override
    public final DataFrameColumn<R, C> parallel() {
        return new XDataFrameColumn<>(frame, true, ordinal());
    }


    @Override
    public boolean tryKey(C colKey) {
        if (!data.colKeys().contains(colKey)) {
            return false;
        } else {
            this.atKey(colKey);
            return true;
        }
    }


    @Override
    public boolean tryOrdinal(int colOrdinal) {
        if (colOrdinal < 0 || colOrdinal >= size()) {
            return false;
        } else {
            this.atOrdinal(colOrdinal);
            return true;
        }
    }


    @Override
    public final XDataFrameColumn<R,C> atKey(C key) {
        try {
            this.colOrdinal = data.colKeys().getOrdinal(key);
            this.colIndex = data.colKeys().getCoordinateAt(colOrdinal);
            this.colKey = key;
            return this;
        } catch (Exception ex) {
            throw new DataFrameException(ex.getMessage(), ex);
        }
    }


    @Override
    public final XDataFrameColumn<R,C> atOrdinal(int ordinal) {
        try {
            this.colKey = data.colKeys().getKey(ordinal);
            this.colIndex = data.colKeys().getCoordinateAt(ordinal);
            this.colOrdinal = ordinal;
            return this;
        } catch (Exception ex) {
            throw new DataFrameException(ex.getMessage(), ex);
        }
    }


    @Override
    public final C key() {
        return colKey;
    }

    @Override
    public final int ordinal() {
        return colOrdinal;
    }

    @Override
    public final Class<?> dataClass() {
        return data.colType(colKey);
    }

    @Override
    public final boolean getBoolean(R key) {
        final int rowIndex = data.rowCoordinate(key);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final boolean getBooleanAt(int ordinal) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final int getInt(R key) {
        final int rowIndex = data.rowCoordinate(key);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final int getIntAt(int ordinal) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final long getLong(R key) {
        final int rowIndex = data.rowCoordinate(key);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final long getLongAt(int ordinal) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final double getDouble(R key) {
        final int rowIndex = data.rowCoordinate(key);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final double getDoubleAt(int ordinal) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValue(R key) {
        final int rowIndex = data.rowCoordinate(key);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValueAt(int ordinal) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final boolean setBooleanAt(int ordinal, boolean value) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final boolean setBoolean(R key, boolean value) {
        final int rowIndex = data.rowCoordinate(key);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setIntAt(int ordinal, int value) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setInt(R key, int value) {
        final int rowIndex = data.rowCoordinate(key);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLongAt(int ordinal, long value) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLong(R key, long value) {
        final int rowIndex = data.rowCoordinate(key);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDoubleAt(int ordinal, double value) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDouble(R key, double value) {
        final int rowIndex = data.rowCoordinate(key);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValueAt(int ordinal, V value) {
        final int rowIndex = data.rowCoordinateAt(ordinal);
        return data.valueAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValue(R key, V value) {
        final int rowIndex = data.rowCoordinate(key);
        return data.valueAt(rowIndex, colIndex, value);
    }

}
