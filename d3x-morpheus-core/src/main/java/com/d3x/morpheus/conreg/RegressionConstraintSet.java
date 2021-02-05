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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.d3x.morpheus.linalg.SVD;
import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.series.DoubleSeries;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;

/**
 * Encapsulates a unique set of linear equality constraints on the estimated
 * coefficients in a linear regression.
 *
 * @param <C> the runtime type of the regressor (column) variables.
 *
 * <p>This is open source software released under the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache 2.0 License</a></p>
 *
 * @author  Scott Shaffer
 */
public final class RegressionConstraintSet<C> {
    private final Set<C> regressorKeys = new LinkedHashSet<>();
    private final Map<String, RegressionConstraint<C>> constraintMap = new LinkedHashMap<>();

    private RegressionConstraintSet(Iterable<RegressionConstraint<C>> constraints) {
        addConstraints(constraints);
        validateConstraints();
    }

    private void addConstraints(Iterable<RegressionConstraint<C>> constraints) {
        for (RegressionConstraint<C> constraint : constraints)
            addConstraint(constraint);
    }

    private void addConstraint(RegressionConstraint<C> constraint) {
        if (constraintMap.containsKey(constraint.getName()))
            throw new MorpheusException("Duplicate regression constraint: [%s].", constraint.getName());

        regressorKeys.addAll(constraint.listRegressors());
        constraintMap.put(constraint.getName(), constraint);
    }

    private void validateConstraints() {
        if (constraintMap.isEmpty())
            return;

        D3xMatrix matrix = getConstraintMatrix(List.copyOf(regressorKeys));
        SVD svd = SVD.apache(matrix);

        int matrixRank = svd.getRank();
        int requiredRank = countConstraints();

        if (matrixRank != requiredRank)
            throw new MorpheusException("The constraint set is rank-deficient: [%d < %d].", matrixRank, requiredRank);
    }

    /**
     * Creates a new regression constraint set with fixed constraints.
     *
     * @return a new regression constraint set containing the specified constraints.
     *
     * @throws MorpheusException unless the constraints have unique names and their terms
     * are linearly independent (they form a constraint matrix with full rank).
     */
    public static <C> RegressionConstraintSet<C> create(Iterable<RegressionConstraint<C>> constraints) {
        return new RegressionConstraintSet<>(constraints);
    }

    /**
     * Identifies the constraints in this set.
     *
     * @param constraintName the name of the constraint in question.
     *
     * @return {@code true} iff this model contains the specified constraint.
     */
    public boolean containsConstraint(String constraintName) {
        return constraintMap.containsKey(constraintName);
    }

    /**
     * Identifies regressor variables that are constrained by the constraints
     * in this set.
     *
     * @param regressor the key of the regressor in question.
     *
     * @return {@code true} iff this model contains at least one constraint
     * that affects the specified regressor variable.
     */
    public boolean containsRegressor(C regressor) {
        return regressorKeys.contains(regressor);
    }

    /**
     * Returns the number of constraints in this set.
     * @return the number of constraints in this set.
     */
    public int countConstraints() {
        return constraintMap.size();
    }

    /**
     * Returns the number of unique regressors that are constrained by the
     * constraints in this set.
     *
     * @return the number of unique regressors that are constrained by the
     * constraints in this set.
     */
    public int countRegressors() {
        return regressorKeys.size();
    }

    /**
     * Returns the names of the linear constraints in the order that they
     * appear in the constraint matrix and value vector.
     *
     * @return the names of the linear constraints in the order that they
     * appear in the constraint matrix and value vector.
     */
    public List<String> getConstraintNames() {
        return List.copyOf(constraintMap.keySet());
    }

    /**
     * Returns the linear constraint coefficient matrix (the left-hand side terms)
     * with rows having the same order as the keys in {@code getConstraintNames()}.
     * The matrix has dimensions {@code P x N}, where {@code P} is the number of
     * constraints and {@code N} is the number of regressor variables.
     *
     * @param columnKeys a list of regressor keys corresponding to the columns of
     *                   the constraint matrix; this list may include regressors
     *                   that are not affected by the constraints in this set.
     *
     * @return the linear constraint coefficient matrix (the left-hand side terms).
     */
    public D3xMatrix getConstraintMatrix(List<C> columnKeys) {
        if (constraintMap.isEmpty())
            return D3xMatrix.empty();

        List<String> rowKeys = getConstraintNames();
        D3xMatrix matrix = D3xMatrix.dense(rowKeys.size(), columnKeys.size());

        for (int row = 0; row < matrix.nrow(); ++row) {
            DoubleSeries<C> terms = constraintMap.get(rowKeys.get(row)).getTerms();

            for (int col = 0; col < columnKeys.size(); ++col)
                matrix.set(row, col, terms.getDoubleOrZero(columnKeys.get(col)));
        }

        return matrix;
    }

    /**
     * Returns the linear constraint value vector (the right-hand side values) with
     * elements having the same order as the keys in {@code getConstraintNames()}.
     * The vector length is equal to the number of constraints.
     *
     * @return the linear constraint value vector (the right-hand side values).
     */
    public D3xVector getConstraintValues() {
        if (countConstraints() < 1)
            return D3xVector.empty();

        List<String> rowKeys = getConstraintNames();
        D3xVector values = D3xVector.dense(countConstraints());

        for (int index = 0; index < rowKeys.size(); ++index)
            values.set(index, constraintMap.get(rowKeys.get(index)).getValue());

        return values;
    }
}
