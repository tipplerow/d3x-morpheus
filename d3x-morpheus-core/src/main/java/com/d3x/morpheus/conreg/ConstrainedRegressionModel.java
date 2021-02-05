/*
 * Copyright (C) 2014-2021 D3X Systems - All Rights Reserved
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
package com.d3x.morpheus.conreg;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;

import com.d3x.morpheus.frame.DataFrame;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.util.LazyValue;

/**
 * Defines a linear regression model that enforces linear equality constraints
 * among the regression coefficients.  The constraints are indexed by a unique
 * string that is used to assign and retrieve the constraint terms.
 *
 * <p><b>Intercept term.</b> This class does not support the addition of an
 * intercept term external to the regressor variables. If an intercept term
 * is required in the model, one of the regressor variables must refer to a
 * column of ones in the observation set, like the market factor in an asset
 * risk model.</p>
 *
 * @param <R> the runtime type for the keys of the observations (rows).
 * @param <C> the runtime type for the keys of the regressand and regressors (columns).
 *
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public final class ConstrainedRegressionModel<R,C> {
    /**
     * The DataFrame containing the independent (left-hand side) values.
     */
    @Getter @NonNull
    private final DataFrame<R,C> regressorFrame;

    /**
     * The DoubleSeries containing the dependent (right-hand side) values.
     */
    @Getter @NonNull
    private final DoubleSeries<R> regressandSeries;

    /**
     * The keys for the regressors that will be used in the regression.
     */
    @Getter @NonNull
    private List<C> regressorKeys;

    /**
     * The keys for the observations that will be used in the regression
     * (the elements of the regressand and rows of the regressor frame).
     */
    @Getter @NonNull
    private List<R> observationKeys;

    /**
     * The DoubleSeries containing the regression weights to apply to
     * each observation.
     */
    @Getter @NonNull
    private DoubleSeries<R> observationWeights;

    private final List<RegressionConstraint<C>> constraintList = new ArrayList<>();
    private final LazyValue<RegressionConstraintSet<C>> constraintSet = LazyValue.of(this::buildConstraintSet);

    private RegressionConstraintSet<C> buildConstraintSet() {
        return RegressionConstraintSet.create(constraintList);
    }

    private ConstrainedRegressionModel(DataFrame<R,C> regressorFrame, DoubleSeries<R> regressandSeries) {
        this.regressorFrame = regressorFrame;
        this.regressandSeries = regressandSeries;

        this.regressorKeys = List.copyOf(regressorFrame.listColumnKeys());
        this.observationKeys = List.copyOf(regressorFrame.listRowKeys());
        this.observationWeights = createDefaultWeights();

        validateKeys();
    }

    private DoubleSeries<R> createDefaultWeights() {
        return DoubleSeries.ones(regressorFrame.rows().keyClass(), observationKeys);
    }

    private void validateKeys() {
        regressorFrame.requireRows(observationKeys);
        regressorFrame.requireColumns(regressorKeys);
        regressandSeries.requireKeys(observationKeys);
        observationWeights.requireKeys(observationKeys);
    }

    private void validateConstraint(RegressionConstraint<C> constraint) {
        regressorFrame.requireNumericColumns(constraint.listRegressors());
    }

    /**
     * Builds a new constrained regression model with fixed regressand and
     * regressor variables.  The constraints should be added by calling the
     * {@code withConstraint()} method. By default, all observations in the
     * observation frame are used and weighted equally in the regression.
     * Call {@code withObservations()} to restrict the regression to a
     * subset of the observations; call {@code withWeights()} to supply a
     * series of weights for the observations; call {@code withRegressors()}
     * to restrict the explanatory variables to a subset of those found in
     * the regressor frame.
     *
     * @param regressorFrame a DataFrame containing the independent variables
     *                       (the left-hand side).
     *
     * @param regressandSeries a DoubleSeries containing the dependent variables
     *                         (the right-hand side).
     *
     * @return a new constrained regression model with the specified regressand
     * and regressor variables.
     */
    public static <R,C> ConstrainedRegressionModel<R,C> create(DataFrame<R, C> regressorFrame,
                                                               DoubleSeries<R> regressandSeries) {
        return new ConstrainedRegressionModel<>(regressorFrame, regressandSeries);
    }

    /**
     * Builds a new constrained regression model with fixed regressand and
     * regressor variables.  The constraints should be added by calling the
     * {@code withConstraint()} method. By default, all observations in the
     * observation frame are used and weighted equally in the regression.
     * Call {@code withObservations()} to restrict the regression to a
     * subset of the observations; call {@code withWeights()} to supply a
     * series of weights for the observations.
     *
     * @param regressandColumn the key of the column that contains the dependent
     *                         (right-hand side) variable.
     *
     * @param regressorColumns the keys of the columns that contain the independent
     *                         (left-hand side) variables.
     *
     * @param regressorFrame a DataFrame containing the both the regressand and
     *                       regressor variables.
     *
     * @return a new constrained regression model with the specified regressand
     * and regressor variables.
     *
     * @throws RuntimeException unless the DataFrame contains all column keys.
     */
    public static <R,C> ConstrainedRegressionModel<R,C> create(C regressandColumn,
                                                               List<C> regressorColumns,
                                                               DataFrame<R, C> regressorFrame) {

        DoubleSeries<R> regressandSeries =
                DoubleSeries.from(regressorFrame, regressandColumn);

        return create(regressorFrame, regressandSeries).withRegressors(regressorColumns);
    }

    /**
     * Builds the linear system that must be solved to estimate this
     * constrained regression model.
     *
     * @return the linear system that must be solved to estimate this
     * constrained regression model.
     */
    public ConstrainedRegressionSystem<R,C> build() {
        return ConstrainedRegressionSystem.build(this);
    }

    /**
     * Adds a constraint on the regression coefficients.
     *
     * @param constraint the constraint to add.
     *
     * @return this model, updated, for operator chaining.
     *
     * @throws RuntimeException unless the regressor frame contains a column
     * for every regressor affected by the constraint.
     */
    ConstrainedRegressionModel<R,C> withConstraint(RegressionConstraint<C> constraint) {
        validateConstraint(constraint);
        this.constraintSet.reset();
        this.constraintList.add(constraint);
        return this;
    }

    /**
     * Adds a constraint on the regression coefficients.
     *
     * @param name  a unique name for the constraint.
     * @param value the right-hand side value for the constraint.
     * @param terms the left-hand side terms for the constraint.
     *
     * @return this model, updated, for operator chaining.
     *
     * @throws RuntimeException unless the regressor frame contains a column
     * for every regressor affected by the constraint.
     */
    ConstrainedRegressionModel<R,C> withConstraint(String name, double value, DoubleSeries<C> terms) {
        return withConstraint(new RegressionConstraint<>(name, value, terms));
    }

    /**
     * Restricts the regression to a subset of the observations.
     *
     * @param regressorKeys the keys of the explanatory variables to include
     *                      in the regression.
     *
     * @return this model, updated, for operator chaining.
     *
     * @throws RuntimeException unless all regressor keys refer to columns in
     * the regressor frame.
     */
    ConstrainedRegressionModel<R,C> withRegressors(List<C> regressorKeys) {
        this.regressorKeys = List.copyOf(regressorKeys);
        validateKeys();
        return this;
    }

    /**
     * Restricts the regression to a subset of the observations.
     *
     * @param observationKeys the observations to include in the regression
     *                        (the keys for the regressand and regressors).
     *
     * @return this model, updated, for operator chaining.
     *
     * @throws RuntimeException unless the observations are present in the
     * regressand series, regressor frame, and weight series.
     */
    ConstrainedRegressionModel<R,C> withObservations(List<R> observationKeys) {
        this.observationKeys = List.copyOf(observationKeys);
        validateKeys();
        return this;
    }

    /**
     * Adds observation weights from the regressor frame to this model.
     *
     * @param weightColumn the key of the regressor frame that contains observation weights.
     *
     * @return this model, updated, for operator chaining.
     *
     * @throws RuntimeException unless the regressor frame contains the specified column.
     */
    ConstrainedRegressionModel<R,C> withWeights(C weightColumn) {
        return withWeights(DoubleSeries.from(regressorFrame, weightColumn));
    }

    /**
     * Adds observation weights to this model.
     *
     * @param observationWeights the observation weights to apply during the regression.
     *
     * @return this model, updated, for operator chaining.
     *
     * @throws RuntimeException unless there is a weight for each observation in this model.
     */
    ConstrainedRegressionModel<R,C> withWeights(DoubleSeries<R> observationWeights) {
        this.observationWeights = observationWeights;
        validateKeys();
        return this;
    }

    /**
     * Returns the number of constraints in this model.
     * @return the number of constraints in this model.
     */
    public int countConstraints() {
        return getConstraintSet().countConstraints();
    }

    /**
     * Returns the number of regressor variables in this model.
     * @return the number of regressor variables in this model.
     */
    public int countRegressors() {
        return regressorKeys.size();
    }

    /**
     * Returns the number of observations in this model.
     * @return the number of observations in this model.
     */
    public int countObservations() {
        return observationKeys.size();
    }

    /**
     * Returns the keys of the constraints on the regression coefficients.
     *
     * @return the keys of the constraints on the regression coefficients.
     *
     * @throws RuntimeException unless the constraints that have been
     * added to this model form a valid constraint set.
     */
    public List<String> getConstraintKeys() {
        return getConstraintSet().getConstraintNames();
    }

    /**
     * Returns the set of constraints on the regression coefficients.
     *
     * @return the set of constraints on the regression coefficients.
     *
     * @throws RuntimeException unless the constraints that have been
     * added to this model form a valid constraint set.
     */
    public RegressionConstraintSet<C> getConstraintSet() {
        return constraintSet.get();
    }
}
