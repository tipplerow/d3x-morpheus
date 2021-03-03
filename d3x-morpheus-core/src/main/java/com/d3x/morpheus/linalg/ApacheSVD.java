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
package com.d3x.morpheus.linalg;

import com.d3x.morpheus.matrix.ApacheMatrix;
import com.d3x.morpheus.matrix.D3xMatrix;
import com.d3x.morpheus.util.MorpheusException;
import com.d3x.morpheus.vector.D3xVector;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

final class ApacheSVD implements SVD {
    private final D3xMatrix A;
    private final D3xMatrix U;
    private final D3xMatrix V;
    private final D3xVector dvec;
    private final D3xMatrix dmat;

    private ApacheSVD(D3xMatrix A, D3xMatrix U, D3xMatrix V, D3xVector wvec) {
        this.A = A;
        this.U = U;
        this.V = V;
        this.dvec = wvec;
        this.dmat = D3xMatrix.diagonal(dvec);
    }

    static ApacheSVD create(D3xMatrix A) {
        //
        // For an (M x N) matrix A, the decomposition takes O(M * N^2) operations,
        // so do not worry about some additional copying, which is only O(M * N)...
        //
        if (!A.all(Double::isFinite))
            throw new MorpheusException("Non-finite values in target matrix.");

        BlockRealMatrix blockA = new BlockRealMatrix(A.toArray());
        SingularValueDecomposition svd = new SingularValueDecomposition(blockA);

        D3xMatrix apacheA = ApacheMatrix.wrap(blockA);
        D3xMatrix apacheU = ApacheMatrix.wrap(svd.getU());
        D3xMatrix apacheV = ApacheMatrix.wrap(svd.getV());
        D3xVector singVec = D3xVector.copyOf(svd.getSingularValues());

        return new ApacheSVD(apacheA, apacheU, apacheV, singVec);
    }

    @Override
    public D3xMatrix getA() {
        return A.copy(); // Defensive copy...
    }

    @Override
    public D3xMatrix getU() {
        return U.copy(); // Defensive copy...
    }

    @Override
    public D3xMatrix getUT() {
        return U.transpose(); // Creates a new matrix...
    }

    @Override
    public D3xMatrix getD() {
        return dmat.copy(); // Defensive copy...
    }

    @Override
    public D3xVector getSingularValueVector() {
        return dvec.copy(); // Defensive copy...
    }

    @Override
    public D3xMatrix getV() {
        return V.copy(); // Defensive copy...
    }

    @Override
    public D3xMatrix getVT() {
        return V.transpose(); // Creates a new matrix...
    }
}
