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
package com.d3x.morpheus.pipeline;

import com.d3x.morpheus.util.MorpheusException;

/**
 * Creates pipelines from a name and argument string.
 *
 * @author Scott Shaffer
 */
public class PipelineFactory {
    /**
     * The default pipeline factory, which recognizes all pipelines defined
     * in the DataPipeline interface.
     */
    public static final PipelineFactory DEFAULT = new PipelineFactory();

    /**
     * Creates a new pipeline given its name and argument(s).
     *
     * @param name the name of the pipeline.
     * @param args the arguments required to create the pipeline.
     *
     * @return the pipeline with the specified name and argument(s).
     */
    public DataPipeline create(String name, Object... args) {
        switch (name) {
            case "abs":
                assertNoArgs(name, args);
                return DataPipeline.abs;

            case "add":
                assertArgs(name, args, Number.class);
                return DataPipeline.add(doubleArg(args, 0));

            case "bound":
                assertArgs(name, args, Number.class, Number.class);
                return DataPipeline.bound(doubleArg(args, 0), doubleArg(args, 1));

            case "demean":
                assertNoArgs(name, args);
                return DataPipeline.demean;

            case "divide":
                assertArgs(name, args, Number.class);
                return DataPipeline.divide(doubleArg(args, 0));

            case "exp":
                assertNoArgs(name, args);
                return DataPipeline.exp;

            case "flip":
                assertNoArgs(name, args);
                return DataPipeline.flip;

            case "identity":
                assertNoArgs(name, args);
                return DataPipeline.identity;

            case "invert":
                assertNoArgs(name, args);
                return DataPipeline.invert;

            case "log":
                assertNoArgs(name, args);
                return DataPipeline.log;

            case "multiply":
                assertArgs(name, args, Number.class);
                return DataPipeline.multiply(doubleArg(args, 0));

            case "pow":
                assertArgs(name, args, Number.class);
                return DataPipeline.pow(doubleArg(args, 0));

            case "replaceNA": // Fall-through
            case "replaceNaN":
                assertArgs(name, args, Number.class);
                return DataPipeline.replaceNaN(doubleArg(args, 0));

            case "sign":
                assertNoArgs(name, args);
                return DataPipeline.sign;

            case "sqrt":
                assertNoArgs(name, args);
                return DataPipeline.sqrt;

            case "square":
                assertNoArgs(name, args);
                return DataPipeline.square;

            case "standardize":
                assertNoArgs(name, args);
                return DataPipeline.standardize;

            case "subtract":
                assertArgs(name, args, Number.class);
                return DataPipeline.subtract(doubleArg(args, 0));

            case "trim":
                assertArgs(name, args, Number.class);
                return DataPipeline.trim(doubleArg(args, 0));

            default:
                throw new MorpheusException("Unknown pipeline: [%s].", name);
        }
    }

    /**
     * Ensures that the arguments that were supplied to this factory have
     * the expected number and type.
     *
     * @param name  the name of the pipeline.
     * @param args  the supplied arguments.
     * @param types the expected argument types.
     *
     * @throws RuntimeException unless the number and type of the actual
     * arguments match those that are expected.
     */
    protected void assertArgs(String name, Object[] args, Class<?>... types) {
        if (args.length != types.length)
            throw new MorpheusException("Expected [%d] arguments for pipeline [%s] but found [%d].", name, args.length, types.length);

        for (int index = 0; index < args.length; ++index)
            if (!types[index].isInstance(args[index]))
                throw new MorpheusException("Expected class [%s] for argument [%d] of pipeline [%s] but found [%s].",
                        types[index].getSimpleName(), index, name, args[index].getClass().getSimpleName());
    }

    /**
     * Ensures that no arguments were specified when none are expected.
     *
     * @param name the name of the pipeline.
     * @param args the supplied arguments.
     *
     * @throws RuntimeException unless the argument array is empty.
     */
    protected void assertNoArgs(String name, Object[] args) {
        if (args.length > 0)
            throw new MorpheusException("Pipeline [%s] takes no arguments.", name);
    }

    /**
     * Returns a floating-point argument from an argument list.
     *
     * @param args the arguments supplied to this factory.
     * @param index the (zero-offset) index of the desired argument.
     *
     * @return the floating-point argument at the specified position.
     *
     * @throws RuntimeException unless a numeric argument is present
     * at the specified position.
     */
    protected double doubleArg(Object[] args, int index) {
        return ((Number) args[index]).doubleValue();
    }
}
