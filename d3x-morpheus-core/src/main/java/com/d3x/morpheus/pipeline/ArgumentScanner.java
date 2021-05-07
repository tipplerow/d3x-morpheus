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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Parses text blocks containing pipeline arguments.
 *
 * @author Scott Shaffer
 */
public class ArgumentScanner {
    /**
     * The delimiter used to separate pipeline arguments.
     */
    public static final char ARGUMENT_SEPARATOR = ',';

    // Zero or more white-space characters...
    private static final String WHITE_SPACE = "\\s*";

    // Argument separator surrounded by ignorable white space...
    private static final Pattern ARGUMENT_PATTERN = Pattern.compile(WHITE_SPACE + ARGUMENT_SEPARATOR + WHITE_SPACE);

    // Dates presented in the standard YYYY-MM-DD format...
    private static final Pattern LOCAL_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    /**
     * The default argument scanner, which recognizes arguments of type
     * boolean, int, double, String, and LocalDate (YYYY-MM-DD).
     */
    public static final ArgumentScanner DEFAULT = new ArgumentScanner();

    /**
     * Scans a text block for pipeline arguments and creates their object
     * representations.
     *
     * @param text a text block to scan.
     *
     * @return the arguments encoded in the text block, with any primitive
     * types boxed as their Object counterparts.
     */
    public Object[] scan(String text) {
        List<Object> args = new ArrayList<>();
        Scanner scanner = new Scanner(text).useDelimiter(ARGUMENT_PATTERN);

        while (scanner.hasNext())
            args.add(getNext(scanner));

        return args.toArray();
    }

    /**
     * Retrieves the next argument from the active scanner.
     *
     * @param scanner the active argument scanner.
     *
     * @return the next argument from the given scanner.
     */
    protected Object getNext(Scanner scanner) {
        if (scanner.hasNextBoolean())
            return scanner.nextBoolean();

        if (scanner.hasNextInt())
            return scanner.nextInt();

        if (scanner.hasNextDouble())
            return scanner.nextDouble();

        return convert(scanner.next());
    }

    /**
     * Converts a string that encodes another object (a LocalDate, for
     * example, but not a primitive type) into that object, or simply
     * returns the string if there is no other object encoded.
     *
     * @param s the argument string to convert.
     *
     * @return the object encoded by the given string, or the string itself
     * if no other object is encoded.
     */
    protected Object convert(String s) {
        if (LOCAL_DATE_PATTERN.matcher(s).matches())
            return LocalDate.parse(s);
        else
            return s;
    }
}
