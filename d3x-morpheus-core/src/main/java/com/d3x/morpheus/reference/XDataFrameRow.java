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

import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameRow;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.util.Bounds;

/**
 * An implementation of DataFrameVector which represents a view onto a single row of an underlying DataFrame.
 *
 * @param <R>   the row key type
 * @param <C>   the column key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class XDataFrameRow<R,C> extends XDataFrameVector<R,C,R,C,DataFrameRow<R,C>> implements DataFrameRow.Cursor<R,C> {

    private static final long serialVersionUID = 1L;

    private R rowKey;
    private int rowIndex = -1;
    private int rowOrdinal = -1;
    private XDataFrame<R,C> frame;
    private XDataFrameContent<R,C> data;

    /**
     * Constructor
     * @param frame     the frame reference
     * @param parallel  true for parallel implementation
     */
    XDataFrameRow(XDataFrame<R,C> frame, boolean parallel) {
        this(frame, parallel, -1);
    }

    /**
     * Constructor
     * @param frame         the frame reference
     * @param parallel      true for parallel implementation
     * @param rowOrdinal    the row ordinal in view space
     */
    XDataFrameRow(XDataFrame<R,C> frame, boolean parallel, int rowOrdinal) {
        super(frame, true, parallel);
        this.frame = frame;
        this.data = frame.content();
        if (rowOrdinal >= 0) {
            this.atOrdinal(rowOrdinal);
        }
    }


    @Override()
    public final DataFrameRow<R,C> forEachValue(Consumer<DataFrameValue<R,C>> consumer) {
        var colCount = frame.colCount();
        if (colCount > 0) {
            var rowOrdinal = ordinal();
            var cursor = frame.cursor().atOrdinals(rowOrdinal, 0);
            for (int i=0; i<colCount; ++i) {
                cursor.colAt(i);
                consumer.accept(cursor);
            }
        }
        return this;
    }


    @Override
    public final Iterator<DataFrameValue<R,C>> iterator() {
        var rowOrdinal = ordinal();
        var cursor = frame.cursor().rowAt(rowOrdinal);
        return new Iterator<>() {
            private int ordinal = 0;
            @Override
            public final DataFrameValue<R,C> next() {
                return cursor.colAt(ordinal++);
            }
            @Override
            public final boolean hasNext() {
                return ordinal < frame.colCount();
            }
        };
    }


    @Override
    public final Iterator<DataFrameValue<R,C>> iterator(Predicate<DataFrameValue<R,C>> predicate) {
        var rowOrdinal = ordinal();
        var cursor = frame.cursor().rowAt(rowOrdinal);
        return new Iterator<>() {
            private int ordinal = 0;
            @Override
            public final DataFrameValue<R,C> next() {
                return cursor.colAt(ordinal++);
            }
            @Override
            public final boolean hasNext() {
                while (ordinal < frame.colCount()) {
                    cursor.colAt(ordinal);
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
        var value = frame().cursor();
        value.rowAt(ordinal());
        for (int i=0; i<size(); ++i) {
            value.colAt(i);
            if (predicate.test(value)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }


    @Override()
    public final Optional<DataFrameValue<R,C>> last(Predicate<DataFrameValue<R,C>> predicate) {
        var value = frame().cursor();
        value.rowAt(ordinal());
        for (int i=size()-1; i>=0; --i) {
            value.colAt(i);
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
                var colStart = first.colOrdinal();
                var result = frame.cursor().atOrdinals(first.rowOrdinal(), first.colOrdinal());
                var value = frame.cursor().atOrdinals(first.rowOrdinal(), first.colOrdinal());
                for (int i=colStart+1; i<frame.colCount(); ++i) {
                    value.colAt(i);
                    if (predicate.test(value) && value.compareTo(result) < 0) {
                        result.colAt(value.colOrdinal());
                    }
                }
                return result;
            });
        }
    }


    @Override()
    public final Optional<DataFrameValue<R,C>> max(Predicate<DataFrameValue<R, C>> predicate) {
        if (frame.rowCount() == 0 || frame.colCount() == 0) {
            return Optional.empty();
        } else {
            return first(predicate).map(first -> {
                var colStart = first.colOrdinal();
                var result = frame.cursor().atOrdinals(first.rowOrdinal(), first.colOrdinal());
                var value = frame.cursor().atOrdinals(first.rowOrdinal(), first.colOrdinal());
                for (int i=colStart+1; i<frame.colCount(); ++i) {
                    value.colAt(i);
                    if (predicate.test(value) && value.compareTo(result) > 0) {
                        result.colAt(value.colOrdinal());
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
            var rowKey = key();
            var result = frame.cursor().row(rowKey);
            var value = frame.cursor().row(rowKey);
            for (int i=0; i<frame.colCount(); ++i) {
                value.colAt(i);
                if (comparator.compare(value, result) < 0) {
                    result.colAt(value.colOrdinal());
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
            var rowKey = key();
            var result = frame.cursor().row(rowKey);
            var value = frame.cursor().row(rowKey);
            for (int i=0; i<frame.colCount(); ++i) {
                value.colAt(i);
                if (comparator.compare(value, result) > 0) {
                    result.colAt(value.colOrdinal());
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
                var colStart = first.colOrdinal();
                var cursor = frame.cursor().atOrdinals(first.rowOrdinal(), first.colOrdinal());
                var minValue = frame.cursor().atOrdinals(first.rowOrdinal(), first.colOrdinal());
                var maxValue = frame.cursor().atOrdinals(first.rowOrdinal(), first.colOrdinal());
                for (int i=colStart+1; i<frame.colCount(); ++i) {
                    cursor.colAt(i);
                    if (predicate.test(cursor)) {
                        if (minValue.compareTo(cursor) < 0) {
                            minValue.colAt(cursor.colOrdinal());
                        }
                        if (maxValue.compareTo(cursor) > 0) {
                            maxValue.colAt(cursor.colOrdinal());
                        }
                    }
                }
                var lower = minValue.<V>getValue();
                var upper = maxValue.<V>getValue();
                return Bounds.of(lower, upper);
            });
        }
    }


    @Override
    public boolean isNull(C key) {
        var rowCoord = data.rowCoordinateOrFail(rowKey);
        var colCoord = data.colCoordinateOrFail(key);
        return data.isNullAt(rowCoord, colCoord);
    }


    @Override
    public boolean isNullAt(int ordinal) {
        var rowCoord = data.rowCoordinateOrFail(rowKey);
        var colCoord = data.colCoordinateAt(ordinal);
        return data.isNullAt(rowCoord, colCoord);
    }


    @Override()
    public final DataFrame<R,C> rank() {
        var values = toDoubleStream().toArray();
        var ranks = XDataFrameRank.rank(values);
        var colKeys = Index.of(frame.cols().keyArray());
        return DataFrame.ofDoubles(key(), colKeys).applyDoubles(v -> ranks[v.rowOrdinal()]);
    }


    @Override
    public final Cursor<R,C> add(R rowKey) {
        this.frame.rows().add(rowKey);
        return atKey(rowKey);
    }


    @Override
    public final boolean tryKey(R rowKey) {
        if (!data.rowKeys().contains(rowKey)) {
            return false;
        } else {
            this.atKey(rowKey);
            return true;
        }
    }


    @Override
    public final boolean tryOrdinal(int rowOrdinal) {
        if (rowOrdinal < 0 || rowOrdinal >= size()) {
            return false;
        } else {
            this.atOrdinal(rowOrdinal);
            return true;
        }
    }


    @Override()
    public final XDataFrameRow<R,C> atKey(R key) {
        try {
            this.rowOrdinal = data.rowKeys().getOrdinal(key);
            this.rowIndex = data.rowKeys().getCoordinateAt(rowOrdinal);
            this.rowKey = key;
            return this;
        } catch (Exception ex) {
            throw new DataFrameException(ex.getMessage(), ex);
        }
    }


    @Override()
    public final XDataFrameRow<R,C> atOrdinal(int ordinal) {
        try {
            this.rowKey = data.rowKeys().getKey(ordinal);
            this.rowIndex = data.rowKeys().getCoordinateAt(ordinal);
            this.rowOrdinal = ordinal;
            return this;
        } catch (Exception ex) {
            throw new DataFrameException(ex.getMessage(), ex);
        }
    }


    @Override()
    public final boolean isNumeric() {
        return ArrayType.of(dataClass()).isNumeric();
    }


    @Override
    public final DataFrameRow<R,C> parallel() {
        return new XDataFrameRow<>(frame, true, ordinal());
    }


    @Override
    public final R key() {
        return rowKey;
    }

    @Override
    public final int ordinal() {
        return rowOrdinal;
    }

    @Override
    public final Class<?> dataClass() {
        return data.rowType(rowKey);
    }

    @Override
    public final boolean getBoolean(C key) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final boolean getBooleanAt(int ordinal) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final int getInt(C key) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final int getIntAt(int ordinal) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final long getLong(C key) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final long getLongAt(int ordinal) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final double getDouble(C key) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final double getDoubleAt(int ordinal) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValue(C key) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValueAt(int ordinal) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final boolean setBooleanAt(int ordinal, boolean value) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final boolean setBoolean(C key, boolean value) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setIntAt(int ordinal, int value) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setInt(C key, int value) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLongAt(int ordinal, long value) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLong(C key, long value) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDoubleAt(int ordinal, double value) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDouble(C key, double value) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValueAt(int ordinal, V value) {
        final int colIndex = data.colCoordinateAt(ordinal);
        return data.valueAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValue(C key, V value) {
        final int colIndex = data.colCoordinateOrFail(key);
        return data.valueAt(rowIndex, colIndex, value);
    }

}
