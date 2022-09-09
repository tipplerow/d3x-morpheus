/*
 * Copyright (C) 2014-2022 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.regr;

import java.util.Arrays;

import com.d3x.morpheus.vector.D3xVectorView;

import lombok.Getter;
import lombok.NonNull;

/**
 * Provides a base class for univariate regressions with data that may
 * be updated sequentially.
 *
 * @author Scott Shaffer
 */
public abstract class UnivariateRegression {
    /**
     * The number of observations in this model.
     */
    @Getter
    protected int numObs = 0;

    /**
     * The estimated y-intercept.
     */
    @Getter
    protected double intercept = Double.NaN;

    /**
     * The estimated slope.
     */
    @Getter
    protected double slope = Double.NaN;

    /**
     * The vector of independent variables.
     */
    protected double[] dataX;

    /**
     * The vector of dependent observations.
     */
    protected double[] dataY;

    /**
     * The vector of regression weights.
     */
    protected double[] weight;

    private static final int INITIAL_CAPACITY = 16;
    private static final int HALF_MAX_CAPACITY = Integer.MAX_VALUE / 2;

    /**
     * Creates an empty regression model with the default capacity.
     */
    protected UnivariateRegression() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Creates an empty regression model with a given capacity.
     *
     * @param capacity the initial capacity of the model (the number of
     *                 observations that may be added before the internal
     *                 storage needs to be resized).
     */
    protected UnivariateRegression(int capacity) {
        this.dataX = new double[capacity];
        this.dataY = new double[capacity];
        this.weight = new double[capacity];
    }

    /**
     * Creates a regression model populated with observations.
     *
     * @param x the independent variables.
     * @param y the dependent observations.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    protected UnivariateRegression(@NonNull D3xVectorView x,
                                   @NonNull D3xVectorView y) {
        this(x.length());
        add(x, y);
    }

    /**
     * Creates a regression model populated with observations.
     *
     * @param x  the independent variables.
     * @param y  the dependent observations.
     * @param wt the observation weights.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    protected UnivariateRegression(@NonNull D3xVectorView x,
                                   @NonNull D3xVectorView y,
                                   @NonNull D3xVectorView wt) {
        this(x.length());
        add(x, y, wt);
    }

    /**
     * Fits the regression model to the current data set.
     */
    public abstract void fit();

    /**
     * Updates the internal state of this model after the addition of
     * an observation.
     *
     * @param x  the new independent variable.
     * @param y  the new dependent observation.
     * @param wt the new observation weight.
     */
    protected abstract void update(double x, double y, double wt);

    /**
     * Adds an observation to this model with unit weight.
     *
     * @param x the new independent variable.
     * @param y the new dependent observation.
     */
    public void add(double x, double y) {
        add(x, y, 1.0);
    }

    /**
     * Adds a weighted observation to this model.
     *
     * @param x  the new independent variable.
     * @param y  the new dependent observation.
     * @param wt the new observation weight.
     */
    public synchronized void add(double x, double y, double wt) {
        var newObs = 1;

        if (atCapacity(newObs))
            increaseCapacity(newObs);

        dataX[numObs] = x;
        dataY[numObs] = y;
        weight[numObs] = wt;
        ++numObs;

        reset();
        update(x, y, wt);
    }

    /**
     * Adds observations to this model with unit weight.
     *
     * @param vecX the new independent variables.
     * @param vecY the new dependent observations.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    public synchronized void add(@NonNull D3xVectorView vecX,
                                 @NonNull D3xVectorView vecY) {
        vecX.validateCongruent(vecY);
        var newObs = vecX.length();

        if (atCapacity(newObs))
            increaseCapacity(newObs);

        for (int index = 0; index < newObs; ++index) {
            var x = vecX.get(index);
            var y = vecY.get(index);

            dataX[numObs] = x;
            dataY[numObs] = y;
            weight[numObs] = 1.0;
            ++numObs;
            update(x, y, 1.0);
        }

        reset();
    }

    /**
     * Adds weighted observations to this model.
     *
     * @param vecX the new independent variables.
     * @param vecY the new dependent observations.
     * @param vecW the new observation weights.
     *
     * @throws RuntimeException unless the vectors have the same length.
     */
    public synchronized void add(@NonNull D3xVectorView vecX,
                                 @NonNull D3xVectorView vecY,
                                 @NonNull D3xVectorView vecW) {
        vecX.validateCongruent(vecY);
        vecX.validateCongruent(vecW);
        var newObs = vecW.length();

        if (atCapacity(newObs))
            increaseCapacity(newObs);

        for (int index = 0; index < newObs; ++index) {
            var x = vecX.get(index);
            var y = vecY.get(index);
            var wt = vecW.get(index);

            dataX[numObs] = x;
            dataY[numObs] = y;
            weight[numObs] = wt;
            ++numObs;
            update(x, y, wt);
        }

        reset();
    }

    /**
     * Returns the independent variable for an observation.
     *
     * @param index the index of the observation.
     *
     * @return the independent variable for the given observation.
     *
     * @throws RuntimeException unless the index is in bounds.
     */
    public double getX(int index) {
        return dataX[index];
    }

    /**
     * Returns the dependent variable for an observation.
     *
     * @param index the index of the observation.
     *
     * @return the dependent variable for the given observation.
     *
     * @throws RuntimeException unless the index is in bounds.
     */
    public double getY(int index) {
        return dataY[index];
    }

    /**
     * Returns the regression weight for an observation.
     * 
     * @param index the index of the observation.
     *              
     * @return the regression weight for the given observation.
     * 
     * @throws RuntimeException unless the index is in bounds.
     */
    public double getWeight(int index) {
        return weight[index];
    }

    /**
     * Reset the parameter estimates following the addition of one or
     * more new observations.
     */
    protected void reset() {
        slope = Double.NaN;
        intercept = Double.NaN;
    }

    private boolean atCapacity(int newObs) {
        return numObs + newObs > getCapacity();
    }

    private int getCapacity() {
        return dataX.length;
    }

    private void increaseCapacity(int newObs) {
        var newLength = newCapacity(newObs);
        dataX = Arrays.copyOf(dataX, newLength);
        dataY = Arrays.copyOf(dataY, newLength);
        weight = Arrays.copyOf(weight, newLength);
    }

    private int newCapacity(int newObs) {
        var oldCapacity = getCapacity();

        if (oldCapacity < HALF_MAX_CAPACITY)
            return Math.max(2 * oldCapacity, oldCapacity + newObs);
        else
            return Integer.MAX_VALUE;
    }
}
