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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.d3x.morpheus.util.MorpheusException;

/**
 * Parses a string representation of a single or composite pipeline.
 *
 * @author Scott Shaffer
 */
public class PipelineScanner {
    private final String source;
    private final Matcher matcher;
    private final PipelineFactory factory;
    private final ArgumentScanner scanner;
    private final List<DataPipeline> pipelines = new ArrayList<>();

    // Index of the character in the source string where the last
    // match ended; used to ensure that only ignorable white-space
    // remains after all pipelines are matched...
    private int matchEnd;

    private PipelineScanner(String source, PipelineFactory factory, ArgumentScanner scanner) {
        this.source = source;
        this.factory = factory;
        this.scanner = scanner;
        this.matcher = PIPELINE_PATTERN.matcher(source);
    }

    /**
     * The delimiter used to separate pipelines in a composite pipeline.
     */
    public static final String PIPELINE_SEPARATOR = ",";

    private static final String START_GROUP = "(";  // Begin an indexed regex group
    private static final String END_GROUP   = ")";  // End an indexed regex group

    private static final String OPEN_PAREN  = "\\("; // Begin the pipeline argument(s)
    private static final String CLOSE_PAREN = "\\)"; // End the pipeline argument(s)

    private static final String FREE_TEXT   = ".*?";  // Any text, possibly empty (zero or more characters)...
    private static final String SINGLE_WORD = "\\w+"; // A valid Java word having at least one character

    // The delimiter and surrounding white space that will precede all pipelines except the first...
    private static final String DELIM_GROUP = START_GROUP + FREE_TEXT + END_GROUP;

    // The single Java word that specifies the pipeline name...
    private static final String NAME_GROUP = START_GROUP + SINGLE_WORD + END_GROUP;

    // The pipeline argument(s), enclosed in parentheses...
    private static final String ARGS_GROUP = OPEN_PAREN + START_GROUP + FREE_TEXT + END_GROUP + CLOSE_PAREN;

    // The final pipeline pattern with three indexed groups...
    private static final Pattern PIPELINE_PATTERN = Pattern.compile(DELIM_GROUP + NAME_GROUP + ARGS_GROUP);

    /**
     * Parses a pipeline string using the default pipeline factory and
     * argument scanner.
     *
     * @param source the string containing an encoded pipeline(s).
     *
     * @return the single or composite pipeline encoded in the given
     * source string.
     *
     * @throws RuntimeException unless the input string is properly
     * formatted and the default factory can create every encoded pipeline.
     */
    public static DataPipeline scan(String source) {
        return scan(source, PipelineFactory.DEFAULT, ArgumentScanner.DEFAULT);
    }

    /**
     * Parses a pipeline string using a customized pipeline factory and
     * argument scanner.
     *
     * @param source  the string containing an encoded pipeline(s).
     * @param factory a factory able to create the encoded pipeline(s).
     * @param scanner a customized scanner for argument parsing.
     *
     * @return the single or composite pipeline encoded in the given
     * source string.
     *
     * @throws RuntimeException unless the input string is properly
     * formatted and the factory can create every encoded pipeline.
     */
    public static DataPipeline scan(String source, PipelineFactory factory, ArgumentScanner scanner) {
        return new PipelineScanner(source, factory, scanner).scan();
    }

    private DataPipeline scan() {
        while (matcher.find())
            parsePipeline();

        validateMatchEnd();
        return resolvePipeline();
    }

    private void parsePipeline() {
        validateMatch();
        matchEnd = matcher.end();

        String name = matcher.group(2);
        Object[] args = scanner.scan(matcher.group(3));

        pipelines.add(factory.create(name, args));
    }

    private void validateMatch() {
        if (matcher.groupCount() != 3)
            throw new MorpheusException("Malformed pipeline: [%s].", matcher.group());

        // For the first pipeline, only ignorable white space is permitted in the
        // group 1 text; for additional pipelines, the group 1 text must contain
        // the pipeline separator and may contain ignorable white space...
        String group1 = matcher.group(1);
        String content = group1.strip();

        if (pipelines.isEmpty() && !content.isEmpty())
            throw new MorpheusException("Invalid pipeline text: [%s].", group1);

        if (!pipelines.isEmpty() && !content.equals(PIPELINE_SEPARATOR))
            throw new MorpheusException("Invalid pipeline text: [%s].", group1);
    }

    private void validateMatchEnd() {
        // Only ignorable white space may remain unparsed after all
        // pipelines have been matched...
        String unparsed = source.substring(matchEnd);

        if (!unparsed.strip().isEmpty())
            throw new MorpheusException("Incomplete pipeline text: [%s].", unparsed);
    }

    private DataPipeline resolvePipeline() {
        if (pipelines.isEmpty())
            throw new MorpheusException("No pipelines encoded in source [%s].", source);
        else if (pipelines.size() == 1)
            return pipelines.get(0);
        else
            return DataPipeline.composite(pipelines);
    }
}
