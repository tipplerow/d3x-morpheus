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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import com.d3x.core.lang.D3xException;
import com.d3x.morpheus.apache.ApacheMatrix;
import com.d3x.morpheus.frame.DataFrame;

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
 * @param <C> the runtime type for the keys of the columns in the observation frame.
 *
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public final class ConstrainedRegressionModel<C> {
    /**
     * The column in the observation DataFrame that contains the independent
     * variable (the right-hand side).
     */
    @Getter @NonNull
    private final C regressand;

    /**
     * The columns in the observation DataFrame that contain the regressor
     * variables (the left-hand side).
     */
    @Getter @NonNull
    private final List<C> regressors;

    /**
     * An optional column in the observation DataFrame that contains regression
     * weights; if {@code null}, all observations will be weighted equally.
     */
    @Getter
    private C weight = null;

    // To allow constant-time inspection of the regressors present in this model...
    private final Set<C> regressorSet;

    // The constraints indexed by key with insertion order maintained...
    private final Map<String, Constraint<C>> constraints = new LinkedHashMap<>();

    @AllArgsConstructor
    private static final class Constraint<C> {
        @Getter @NonNull final String key;
        @Getter @NonNull final double value;
        @Getter @NonNull final DataFrame<String,C> coeffs;
    }

    private ConstrainedRegressionModel(@NonNull C regressand, @NonNull List<C> regressors) {
        this.regressand = regressand;
        this.regressors = List.copyOf(regressors); // Immutable copy...
        this.regressorSet = Set.copyOf(regressors); // Immutable copy...

        validateModel();
    }

    private void validateModel() {
        validateModelRegressand();
        validateModelRegressors();
        validateModelConstraints();
    }

    private void validateModelRegressand() {
        if (containsRegressor(regressand))
            throw new D3xException("The regressand [%s] is also a regressor.");
    }

    private void validateModelRegressors() {
        if (regressors.isEmpty())
            throw new D3xException("No regressors were specified.");

        if (regressorSet.size() != regressors.size())
            throw new D3xException("At least one regressor is duplicated.");
    }

    private void validateModelConstraints() {
        for (Constraint<C> constraint : constraints.values())
            validateModelConstraint(constraint);
    }

    private void validateModelConstraint(Constraint<C> constraint) {
        if (constraint.coeffs.rowCount() != 1)
            throw new D3xException("A single-row data frame is required for regression constraints.");

        requireRegressors(constraint.coeffs.listColumnKeys());
    }

    private void requireRegressors(Iterable<C> regressors) {
        for (C regressor : regressors)
            requireRegressor(regressor);
    }

    private void requireRegressor(C regressor) {
        if (!containsRegressor(regressor))
            throw new D3xException("Missing regressor [%s].", regressor);
    }

    /**
     * Builds a new constrained regression model with fixed regressand and
     * regressor variables. The constraints should be added by calling the
     * {@code addConstraint()} method.  When the parameters are estimated,
     * the observations will be weighted equally unless a column of weights
     * is specified by calling {@code withWeight()}.
     *
     * @param regressand the column in the observation DataFrame that contains
     *                   the independent variable (the right-hand side).
     *
     * @param regressors the columns in the observation DataFrame that contain
     *                   the dependent variables (the left-hand side).
     *
     * @return a new constrained regression model with the specified regressand
     * and regressor variables.
     */
    public static <C> ConstrainedRegressionModel<C> build(C regressand, List<C> regressors) {
        return new ConstrainedRegressionModel<>(regressand, regressors);
    }

    /**
     * Adds <em>category variables</em> to this constrained regression model.
     *
     * <p>Category variables (often called dummy variables) are used to place observations
     * into discrete groups, like countries or industries in a model of asset returns. The
     * category variables must sum to one for each observation.  They are often restricted
     * to be exactly zero or one, but that restriction may be relaxed for observations that
     * span multiple categories (for a company that conducts business across more than one
     * industry, for example).</p>
     *
     * <p>This method may be called multiple times (with different category names) to add
     * multiple categories (which must be linearly independent of all other categories).</p>
     *
     * <p>The model must contain an added intercept or a regressor variable that serves as
     * an intercept (a column of ones). The regression coefficients (betas) for the category
     * variables may be interpreted as offsets relative to the overall level quantified by
     * the intercept. The regression coefficients (betas) will sum to zero when weighted by
     * the regression weights.</p>
     *
     * @param categoryKey the unique name for the category (e.g., COUNTRY).
     * @param regressors  the columns that contain the category regressor variables.
     *
     * @return this constrained regression model, updated.
     *
     * @throws RuntimeException unless all category variables are regressors in this model.
     */
    ConstrainedRegressionModel<C> addCategory(String categoryKey, Set<C> regressors) {
        return addConstraint(DataFrame.onesRow(categoryKey, regressors), 0.0);
    }

    /**
     * Adds a linear constraint on the regression coefficients in this model.
     *
     * @param constraintLHS a single-row DataFrame containing the constraint
     *                      key as the row key and the constraint coefficients
     *                      (the left-hand side terms) as the column data.
     *
     * @param constraintRHS the right-hand side value for the constraint.

     * @return this constrained regression model, updated.
     *
     * @throws RuntimeException unless the constraint key is unique and all
     * column keys in {@code constraintLHS} are regressor variables in this
     * model.
     */
    ConstrainedRegressionModel<C> addConstraint(DataFrame<String,C> constraintLHS, double constraintRHS) {
        if (constraintLHS.rowCount() != 1)
            throw new D3xException("A single-row data frame is required for regression constraints.");

        String constraintKey = constraintLHS.listRowKeys().get(0);

        if (containsConstraint(constraintKey))
            throw new D3xException("Duplicate constraint: [%s].", constraintKey);

        constraints.put(constraintKey, new Constraint<C>(constraintKey, constraintRHS, constraintLHS));
        validateModel();

        return this;
    }

    /**
     * Specifies a column of regression weights for this model.
     *
     * @param weight the column in the observation frame that contains the weights.
     *
     * @return this constrained regression model, updated.
     */
    ConstrainedRegressionModel<C> withWeight(C weight) {
        this.weight = weight;
        return this;
    }

    /**
     * Identifies named constraints in this model.
     *
     * @param constraintKey the key of the constraint in question.
     *
     * @return {@code true} iff this model contains the specified constraint.
     */
    public boolean containsConstraint(String constraintKey) {
        return constraints.containsKey(constraintKey);
    }

    /**
     * Identifies regressor variables in this model.
     *
     * @param regressor the key of the regressor in question.
     *
     * @return {@code true} iff this model contains the specified regressor variable.
     */
    public boolean containsRegressor(C regressor) {
        return regressorSet.contains(regressor);
    }

    /**
     * Returns the number of constraints in this model.
     *
     * @return the number of constraints in this model.
     */
    public int countConstraints() {
        return constraints.size();
    }

    /**
     * Returns the number of regressors in this model.
     *
     * @return the number of regressors in this model.
     */
    public int countRegressors() {
        return regressors.size();
    }

    /**
     * Returns the keys of the linear constraints, in the order that they were
     * added to this model.
     *
     * @return the keys of the linear constraints, in the order that they were
     * added to this model.
     */
    public List<String> getConstraintKeys() {
        return List.copyOf(constraints.keySet());
    }

    /**
     * Returns the linear constraint coefficient matrix (the left-hand side terms)
     * with rows having the same order as the keys in {@code getConstraintKeys()}.
     * The matrix has dimensions {@code P x N}, where {@code P} is the number of
     * constraints and {@code N} is the number of regressor variables.
     *
     * @return the linear constraint coefficient matrix (the left-hand side terms).
     */
    public RealMatrix getConstraintMatrix() {
        DataFrame<String,C> frame = DataFrame.zeros(constraints.keySet(), regressors);

        for (Constraint<C> constraint : constraints.values())
            frame = frame.update(constraint.coeffs, false, false);

        return ApacheMatrix.wrap(frame);
    }

    /**
     * Returns the linear constraint value vector (the right-hand side values) with
     * elements having the same order as the keys in {@code getConstraintKeys()}.
     * The vector length is equal to the number of constraints.
     *
     * @return the linear constraint value vector (the right-hand side values).
     */
    public RealVector getConstraintValues() {
        int index = 0;
        RealVector values = new ArrayRealVector(countConstraints());

        for (Constraint<C> constraint : constraints.values()) {
            values.setEntry(index, constraint.value);
            index++;
        }

        return values;
    }

    /**
     * Identifies models with regression weights.
     *
     * @return {@code true} iff this model contains a column of regression weights.
     */
    public boolean hasWeights() {
        return weight != null;
    }
}
