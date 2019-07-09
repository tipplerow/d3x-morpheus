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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.array.ArrayBuilder;
import com.d3x.morpheus.array.ArrayType;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.frame.DataFrameAxisStats;
import com.d3x.morpheus.frame.DataFrameColumn;
import com.d3x.morpheus.frame.DataFrameColumns;
import com.d3x.morpheus.frame.DataFrameCursor;
import com.d3x.morpheus.frame.DataFrameEvent;
import com.d3x.morpheus.frame.DataFrameException;
import com.d3x.morpheus.frame.DataFrameGrouping;
import com.d3x.morpheus.frame.DataFrameOptions;
import com.d3x.morpheus.frame.DataFrameValue;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.stats.StatType;
import com.d3x.morpheus.util.Asserts;
import com.d3x.morpheus.util.Parallel;
import com.d3x.morpheus.util.functions.ToBooleanFunction;

/**
 * The reference implementation of DataFrameOperator that operates in the row dimension of the DataFrame.
 *
 * @param <R>       the row key type
 * @param <C>       the column key type
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 *
 * @author  Xavier Witdouck
 */
class XDataFrameColumns<R,C> extends XDataFrameAxisBase<C,R,R,C,DataFrameColumn<R,C>,DataFrameColumns<R,C>,DataFrameGrouping.Cols<R,C>> implements DataFrameColumns<R,C> {


    /**
     * Constructor
     * @param frame     the frame to operate on
     * @param parallel  true for parallel implementation
     */
    XDataFrameColumns(XDataFrame<R,C> frame, boolean parallel) {
        super(frame, parallel, false);
    }


    @Override
    public final DataFrameColumns<R,C> parallel() {
        return isParallel() ? this : new XDataFrameColumns<>(frame(), true);
    }


    @Override
    public final XDataFrameColumns<R,C> sequential() {
        return !isParallel() ? this : new XDataFrameColumns<>(frame(), false);
    }


    @Override
    public final boolean add(C key, Iterable<?> values) {
        var colKeys = frame().colKeys();
        if (colKeys.contains(key)) {
            return false;
        } else {
            this.columnAdder().andThen(notifyEvent()).apply(columnMap -> {
                if (!contains(key)) {
                    columnMap.put(key, values);
                }
            });
            return true;
        }
    }


    @Override
    public final boolean add(C key, Class<?> type) {
        var colKeys = frame().colKeys();
        if (colKeys.contains(key)) {
            return false;
        } else {
            this.columnAdder().andThen(notifyEvent()).apply(columnMap -> {
                var rowCapacity = frame().content().rowCapacity();
                columnMap.put(key, Array.of(type, rowCapacity));
            });
            return true;
        }
    }


    @Override
    public final <T> boolean add(C key, Class<T> type, Function<DataFrameValue<R,C>,T> initials) {
        var colKeys = frame().colKeys();
        if (colKeys.contains(key)) {
            return false;
        } else {
            this.columnAdder().andThen(seed(initials)).andThen(notifyEvent()).apply(columnMap -> {
                var rowCapacity = frame().content().rowCapacity();
                columnMap.put(key, Array.of(type, rowCapacity));
            });
            return true;
        }
    }


    @Override
    public final Array<C> addAll(Iterable<C> colKeys, Class<?> type) {
        return columnAdder().andThen(notifyEvent()).apply(columnMap -> colKeys.forEach(colKey -> {
            if (!contains(colKey)) {
                columnMap.put(colKey, Array.of(type, frame().rowCount()));
            }
        }));
    }


    @Override
    public final Array<C> addAll(Consumer<Map<C,Iterable<?>>> consumer) {
        return columnAdder().andThen(notifyEvent()).apply(consumer);
    }


    @Override
    public final Array<C> addAll(DataFrame<R,C> other) {
        var frame = frame();
        var source = (XDataFrame<R,C>)other;
        var builder = ArrayBuilder.of(source.colCount(), source.cols().keyClass());
        var ignoreDuplicates = DataFrameOptions.isIgnoreDuplicates();
        source.cols().forEach(column -> {
            var colKey = column.key();
            var exists = frame.cols().contains(colKey);
            if (exists && !ignoreDuplicates) {
                throw new DataFrameException("A column for key already exists in this frame: " + colKey);
            } else if (!exists) {
                var type = source.content().colType(colKey);
                frame.cols().add(colKey, type);
                builder.append(colKey);
            }
        });
        var colKeys = builder.toArray();
        if (colKeys.length() > 0) {
            var rowKeys = source.rowKeys().intersect(frame.rowKeys());
            XDataFrameCopy.apply(source, frame, rowKeys, colKeys);
            notifyEvent().apply(colKeys);
        }
        return colKeys;
    }


    /**
     * Returns a function to seed values for a set of columns defined by the input keys
     * @param seeder        the function that will seed data for columns
     * @return              the seeder function
     */
    private <T> Function<Array<C>,Array<C>> seed(Function<DataFrameValue<R,C>,T> seeder) {
        var column = cursor();
        return keys -> {
            keys.forEach(key -> {
                column.atKey(key);
                column.applyValues(seeder);
            });
            return keys;
        };
    }


    @Override
    public DataFrameColumn.Cursor<R, C> cursor() {
        return new XDataFrameColumn<>(frame(), isParallel());
    }


    @Override
    public final DataFrameAxisStats<C,R,C,C,StatType> stats() {
        return new XDataFrameAxisStats<>(frame(), isParallel(), true);
    }


    @Override
    @SafeVarargs
    public final DataFrame<Double,C> hist(int binCount, C... columns) {
        return hist(binCount, (columns == null || columns.length == 0) ? keyArray() : Array.of(Stream.of(columns)));
    }


    @Override
    public final DataFrame<Double,C> hist(int binCount, Iterable<C> columns) {
        Asserts.check(binCount > 0, "The bin count must be > 0");
        final DataFrame<R,C> filter = frame().cols().select(columns);
        var minValue = filter.stats().min();
        var maxValue = filter.stats().max();
        var stepSize = (maxValue - minValue) / binCount;
        final Range<Double> rowKeys = Range.of(minValue, maxValue + stepSize, stepSize);
        final DataFrame<Double,C> hist = DataFrame.ofInts(rowKeys, columns);
        final DataFrameCursor<Double,C> cursor = hist.cursor();
        final XDataFrameColumn<R,C> column = new XDataFrameColumn<>(frame(), false);
        columns.forEach(colKey -> {
            column.atKey(colKey);
            cursor.col(colKey);
            column.forEachValue(v -> {
                var value = v.getDouble();
                hist.rows().lowerKey(value).ifPresent(lowerKey -> {
                    var rowOrdinal = hist.rows().ordinal(lowerKey);
                    var count = cursor.rowAt(rowOrdinal).getInt();
                    cursor.setInt(count + 1);
                });
            });
        });
        return hist;
    }


    @Override @Parallel
    public final DataFrame<R,C> sort(boolean ascending) {
        return XDataFrameSorter.sortCols(frame(), ascending, isParallel());
    }


    @Override @Parallel
    public final DataFrame<R,C> sort(boolean ascending, R key) {
        return XDataFrameSorter.sortCols(frame(), key, ascending, isParallel());
    }


    @Override @Parallel
    public final DataFrame<R,C> sort(boolean ascending, List<R> keys) {
        return XDataFrameSorter.sortCols(frame(), keys, ascending, isParallel());
    }


    @Override @Parallel
    public final DataFrame<R,C> sort(Comparator<DataFrameColumn<R,C>> comparator) {
        return XDataFrameSorter.sortCols(frame(), isParallel(), comparator);
    }

    @Override @Parallel
    public final DataFrame<R,C> apply(Consumer<DataFrameColumn<R,C>> consumer) {
        this.forEach(consumer);
        return frame();
    }

    @Override @Parallel
    public final DataFrame<R,C> demean(boolean inPlace) {
        if (!inPlace) {
            return frame().copy().cols().demean(true);
        } else {
            frame().cols().forEach(column -> {
                final ArrayType type = ArrayType.of(column.dataClass());
                if (type.isInteger()) {
                    var mean = column.stats().mean().intValue();
                    column.applyInts(v -> v.getInt() - mean);
                } else if (type.isLong()) {
                    final long mean = column.stats().mean().longValue();
                    column.applyLongs(v -> v.getLong() - mean);
                } else {
                    var mean = column.stats().mean();
                    column.applyDoubles(v -> v.getDouble() - mean);
                }
            });
            return frame();
        }
    }


    @Override
    public final <X> DataFrame<R,X> mapKeys(Function<DataFrameColumn<R,C>,X> mapper) {
        if (frame().colKeys().isFilter()) {
            throw new DataFrameException("Column axis is immutable for this frame, call copy() first");
        } else {
            final XDataFrameColumn<R,C> column = new XDataFrameColumn<>(frame(), false);
            return frame().mapColKeys((key, ordinal) -> mapper.apply(column.atOrdinal(ordinal)));
        }
    }


    /**
     * Returns a function that will publish notification events when called
     * @return      the function to send notification events
     */
    private Function<Array<C>,Array<C>> notifyEvent() {
        return keys -> {
            if (keys.length() > 0 && frame().events().isEnabled()) {
                final XDataFrame<R,C> frame = frame();
                final DataFrameEvent<R,C> event = DataFrameEvent.createColumnAdd(frame, keys);
                frame.events().fireDataFrameEvent(event);
            }
            return keys;
        };
    }


    /**
     * Returns a function that will add columns to the frame
     * @return  the function to add columns to the frame
     */
    private Function<Consumer<Map<C,Iterable<?>>>,Array<C>> columnAdder() {
        return consumer -> {
            var keyType = keyClass();
            var columnMap = new LinkedHashMap<C,Iterable<?>>();
            consumer.accept(columnMap);
            var builder = ArrayBuilder.of(columnMap.size(), keyType);
            columnMap.keySet().forEach(colKey -> {
                var values = columnMap.get(colKey);
                var content = frame().content();
                var added = content.addColumn(colKey, values);
                if (added) {
                    builder.append(colKey);
                }
            });
            return builder.toArray();
        };
    }


    @Override
    public final DataFrame<C,StatType> describe(StatType... stats) {
        var statKeys = Array.of(Stream.of(stats));
        var colKeys = filter(DataFrameColumn::isNumeric).keyArray();
        var result = DataFrame.ofDoubles(colKeys, statKeys);
        var cursor = result.cursor();
        this.filter(DataFrameColumn::isNumeric).forEach(column -> {
            var key = column.key();
            cursor.row(key);
            var colStats = column.stats();
            for (int j = 0; j < statKeys.length(); ++j) {
                var stat = statKeys.getValue(j);
                var value = stat.apply(colStats);
                cursor.colAt(j).setDouble(value);
            }
        });
        return result;
    }


    @Override
    public final void forEachValue(C key, Consumer<DataFrameValue<R, C>> consumer) {
        var rowCount = frame().rowCount();
        var cursor = frame().cursor().col(key);
        for (int i=0; i<rowCount; ++i) {
            cursor.rowAt(i);
            consumer.accept(cursor);
        }
    }


    @Override
    public final DataFrame<R,C> applyBooleans(C key, ToBooleanFunction<DataFrameValue<R,C>> function) {
        var rowCount = frame().rowCount();
        var cursor = frame().cursor().col(key);
        for (int i=0; i<rowCount; ++i) {
            var value = function.applyAsBoolean(cursor.rowAt(i));
            cursor.setBoolean(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyInts(C key, ToIntFunction<DataFrameValue<R,C>> function) {
        var rowCount = frame().rowCount();
        var cursor = frame().cursor().col(key);
        for (int i=0; i<rowCount; ++i) {
            var value = function.applyAsInt(cursor.rowAt(i));
            cursor.setInt(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyLongs(C key, ToLongFunction<DataFrameValue<R,C>> function) {
        var rowCount = frame().rowCount();
        var cursor = frame().cursor().col(key);
        for (int i=0; i<rowCount; ++i) {
            var value = function.applyAsLong(cursor.rowAt(i));
            cursor.setLong(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R,C> applyDoubles(C key, ToDoubleFunction<DataFrameValue<R,C>> function) {
        var rowCount = frame().rowCount();
        var cursor = frame().cursor().col(key);
        for (int i=0; i<rowCount; ++i) {
            var value = function.applyAsDouble(cursor.rowAt(i));
            cursor.setDouble(value);
        }
        return frame();
    }


    @Override
    public final <T> DataFrame<R,C> applyValues(C key, Function<DataFrameValue<R,C>, T> function) {
        var rowCount = frame().rowCount();
        var cursor = frame().cursor().col(key);
        for (int i=0; i<rowCount; ++i) {
            var value = function.apply(cursor.rowAt(i));
            cursor.setValue(value);
        }
        return frame();
    }


    @Override
    public final DataFrame<R, C> remove(Predicate<DataFrameColumn<R,C>> predicate) {
        return frame().content().isColumnStore() ? select(predicate) : select(predicate).copy();
    }


    @Override
    public final boolean getBoolean(C colKey, int rowOrdinal) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final boolean getBooleanAt(int colOrdinal, R rowKey) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.booleanAt(rowIndex, colIndex);
    }

    @Override
    public final int getInt(C colKey, int rowOrdinal) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final int getIntAt(int colOrdinal, R rowKey) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.intAt(rowIndex, colIndex);
    }

    @Override
    public final long getLong(C colKey, int rowOrdinal) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final long getLongAt(int colOrdinal, R rowKey) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.longAt(rowIndex, colIndex);
    }

    @Override
    public final double getDouble(C colKey, int rowOrdinal) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final double getDoubleAt(int colOrdinal, R rowKey) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.doubleAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValue(C colKey, int rowOrdinal) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final <V> V getValueAt(int colOrdinal, R rowKey) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.valueAt(rowIndex, colIndex);
    }

    @Override
    public final boolean setBoolean(C colKey, int rowOrdinal, boolean value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final boolean setBooleanAt(int colOrdinal, R rowKey, boolean value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.booleanAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setInt(C colKey, int rowOrdinal, int value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final int setIntAt(int colOrdinal, R rowKey, int value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.intAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLong(C colKey, int rowOrdinal, long value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final long setLongAt(int colOrdinal, R rowKey, long value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.longAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDouble(C colKey, int rowOrdinal, double value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final double setDoubleAt(int colOrdinal, R rowKey, double value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.doubleAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValue(C colKey, int rowOrdinal, V value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinateAt(rowOrdinal);
        var colIndex = data.colCoordinate(colKey);
        return data.valueAt(rowIndex, colIndex, value);
    }

    @Override
    public final <V> V setValueAt(int colOrdinal, R rowKey, V value) {
        var data = frame().content();
        var rowIndex = data.rowCoordinate(rowKey);
        var colIndex = data.colCoordinateAt(colOrdinal);
        return data.valueAt(rowIndex, colIndex, value);
    }
}
