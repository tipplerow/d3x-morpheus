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
package com.d3x.morpheus.frame;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DataFrameExceptionTest {
    private void runFormatTest(String expectedMessage, String format, Object... arguments) {
        Exception ex = new DataFrameException(format, arguments);
        assertEquals(ex.getMessage(), expectedMessage);
    }

    @Test
    public void testNoArguments() {
        runFormatTest("No arguments.", "No arguments.");
    }

    @Test
    public void testIntArgument() {
        runFormatTest("Bad int value [3].", "Bad int value [%d].", 3);
    }

    @Test
    public void testDoubleArgument() {
        runFormatTest("Bad double value [1.23].", "Bad double value [%.2f].", 1.2345678);
    }

    @Test
    public void testStringArgument() {
        runFormatTest("Bad string [abc].", "Bad string [%s].", "abc");
    }

    @Test
    public void testObjectArgument() {
        runFormatTest("Bad object [foo].", "Bad object [%s].", new Foo());
    }

    private static class Foo {
        @Override
        public String toString() {
            return "foo";
        }
    }
}
