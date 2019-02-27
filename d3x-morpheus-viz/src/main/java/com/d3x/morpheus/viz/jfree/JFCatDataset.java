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
package com.d3x.morpheus.viz.jfree;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;

import com.d3x.morpheus.array.Array;
import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.index.Index;
import com.d3x.morpheus.range.Range;
import com.d3x.morpheus.viz.chart.xy.XyDataset;

/**
 * A JFreeChart dataset adapter for category plots
 *
 * @author Xavier Witdouck
 *
 * <p><strong>This is open source software released under the <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></strong></p>
 */
class JFCatDataset<X extends Comparable,S extends Comparable> extends AbstractDataset implements XyDataset<X,S>, CategoryDataset {

    private static final Double NAN = Double.NaN;

    private Index<S> seriesKeys;
    private Index<X> domainKeys;
    private DataFrame<?,S> frame;
    private Array<Integer> colOrdinals;
    private Supplier<Class<X>> domainType;
    private IntFunction<X> domainValueFunction;
    private Consumer<JFCatDataset<X,S>> refreshHandler;

    /**
     * Constructor
     * @param refreshHandler    the refresh handler
     */
    private JFCatDataset(Consumer<JFCatDataset<X,S>> refreshHandler) {
        this.refreshHandler = refreshHandler;
        this.refresh();
    }


    /**
     * Returns a newly created model using a frame supplier where the domain is presented by the DataFrame row keys
     * @param frameSupplier     the DataFrame supplier for this model
     * @param <X>               the domain key type
     * @param <S>               the series key type
     * @return                  the newly created model
     */
    public static <X extends Comparable,S extends Comparable> JFCatDataset<X,S> of(Supplier<DataFrame<X,S>> frameSupplier) {
        return new JFCatDataset<>(dataset -> {
            try {
                final DataFrame<X,S> frame = frameSupplier.get();
                if (frame != null) {
                    final Array<Integer> colOrdinals = Array.of(IntStream.range(0, frame.colCount()).toArray());
                    final Supplier<Class<X>> domainType = () -> frame.rows().keyType();
                    dataset.update(frame, colOrdinals, domainType, rowIndex -> frame.rows().key(rowIndex));
                } else {
                    dataset.clear(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }


    /**
     * Returns a newly created model using a frame supplier where the domain is presented by a column in the DataFrame
     * @param domainAxisKey     the DataFrame column key for the domain
     * @param frameSupplier     the DataFrame supplier for this model
     * @param <X>               the domain key type
     * @param <S>               the series key type
     * @return                  the newly created model
     */
    @SuppressWarnings("unchecked")
    public static <X extends Comparable,S extends Comparable> JFCatDataset<X,S> of(S domainAxisKey, Supplier<DataFrame<?,S>> frameSupplier) {
        return new JFCatDataset<>(dataset -> {
            try {
                final DataFrame<?,S> frame = frameSupplier.get();
                if (frame != null) {
                    final int domainAxisColOrdinal = frame.cols().ordinal(domainAxisKey);
                    final Supplier<Class<X>> domainType = () -> (Class<X>)frame.cols().type(domainAxisKey);
                    final Array<Integer> colOrdinals = Array.of(IntStream.range(0, frame.colCount()).filter(i -> i != domainAxisColOrdinal).toArray());
                    dataset.update(frame, colOrdinals, domainType, rowIndex -> frame.getValueAt(rowIndex, domainAxisColOrdinal));
                } else {
                    dataset.clear(true);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Updates this model with the DataFrame, series column ordinals and domain value function
     * @param frame                 the DataFrame to accept
     * @param colOrdinals           the series column ordinals
     * @param domainValueFunction   the domain value function
     */
    private void update(DataFrame<?,S> frame, Array<Integer> colOrdinals, Supplier<Class<X>> domainType, IntFunction<X> domainValueFunction) {
        try {
            this.frame = frame;
            this.colOrdinals = colOrdinals;
            this.domainType = domainType;
            this.domainValueFunction = domainValueFunction;
            this.domainKeys = Index.of(Range.of(0, frame.rowCount()).map(domainValueFunction::apply).toArray());
            this.seriesKeys = Index.of(colOrdinals.map(v -> frame.cols().key(v.getInt())));
        } finally {
            fireDatasetChanged();
        }
    }


    @Override
    public void refresh() {
        this.refreshHandler.accept(this);
    }


    @Override
    public boolean isEmpty() {
        return frame == null || seriesKeys == null || seriesKeys.size() == 0;
    }


    @Override
    public void clear(boolean notify) {
        this.frame = null;
        this.seriesKeys = null;
        this.domainKeys = null;
        if (notify) {
            fireDatasetChanged();
        }
    }


    @Override
    public Class<X> domainType() {
        return isEmpty() ? null : domainType.get();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <R> DataFrame<R,S> frame() {
        return (DataFrame<R,S>)frame;
    }


    @Override
    public boolean contains(S seriesKey) {
        return seriesKeys.contains(seriesKey);
    }


    @Override
    public IntFunction<X> domainFunction() {
        return domainValueFunction;
    }


    @Override
    public XyDataset<X,S> withLowerDomainInterval(Function<X, X> lowerIntervalFunction) {
        return this;
    }


    @Override
    public XyDataset<X,S> withUpperDomainInterval(Function<X, X> upperIntervalFunction) {
        return this;
    }


    @Override
    public int getRowCount() {
        return isEmpty() ? 0 : seriesKeys.size();
    }


    @Override
    public int getColumnCount() {
        return isEmpty() ? 0 : domainKeys.size();
    }


    @Override
    public List getRowKeys() {
        return isEmpty() ? Collections.emptyList() : seriesKeys.toList();
    }


    @Override
    public List getColumnKeys() {
        return isEmpty() ? Collections.emptyList() : domainKeys.toList();
    }


    @Override
    public Comparable getRowKey(int rowIndex) {
        return seriesKeys.getKey(rowIndex);
    }


    @Override
    public Comparable getColumnKey(int colIndex) {
        return domainKeys.getKey(colIndex);
    }


    @Override
    @SuppressWarnings("unchecked")
    public int getRowIndex(Comparable rowKey) {
        return seriesKeys.getCoordinate((S)rowKey);
    }


    @Override
    @SuppressWarnings("unchecked")
    public int getColumnIndex(Comparable colKey) {
        return domainKeys.getCoordinate((X)colKey);
    }


    @Override
    @SuppressWarnings("unchecked")
    public Number getValue(Comparable rowKey, Comparable colKey) {
        if (isEmpty()) {
            return NAN;
        } else {
            final int rowOrdinal = domainKeys.getCoordinate((X)colKey);
            final int colOrdinal = frame.cols().ordinal((S)rowKey);
            return frame.getDoubleAt(rowOrdinal, colOrdinal);
        }
    }


    @Override
    public Number getValue(int rowIndex, int colIndex) {
        if (isEmpty()) {
            return NAN;
        } else {
            final int colOrdinal = colOrdinals.getInt(rowIndex);
            return frame.getDoubleAt(colIndex, colOrdinal);
        }
    }
}