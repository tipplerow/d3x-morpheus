/*
 * Copyright (C) 2014-2023 Talos Trading - All Rights Reserved
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
package com.d3x.morpheus.pipeline;

/**
 * Provides a base class for data pipeline implementations.
 *
 * @author Scott Shaffer
 */
public abstract class AbstractDataPipeline implements DataPipeline {
    @Override
    public boolean equals(Object other) {
        return (other instanceof DataPipeline) && equalsPipeline((DataPipeline) other);
    }

    private boolean equalsPipeline(DataPipeline that) {
        return this.encode().equals(that.encode());
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Pipelines may not be used as hash keys.");
    }

    @Override
    public String toString() {
        return String.format("DataPipeline([%s])", encode());
    }
}
