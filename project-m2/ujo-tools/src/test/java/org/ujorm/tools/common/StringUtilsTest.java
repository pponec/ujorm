/*
 * Copyright 2021-2022 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/JdbcBuilder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Pavel Ponec
 */
public class StringUtilsTest {

    @Test
    public void testRead() {
        String result = StringUtils.read(StringUtils.class, "text", "dummy.txt");
        assertTrue(result.startsWith("abc"));
    }

    @Test
    public void testReadLines() throws Exception {
        try (Stream<String> stream = StringUtils.readLines(StringUtils.class,  "text", "dummy.txt")) {
            String[] result = stream.toArray(String[]::new);
            assertEquals(3, result.length);
            assertEquals("abc", result[0]);
            assertEquals("def", result[1]);
            assertEquals("ghi", result[2]);
        }
    }

    @Test
    public void testBuildResource() {
        StringUtils instance = new StringUtils();

        String s1 = instance.buildResource(StringUtils.class, "/text", "dummy.txt");
        String s2 = instance.buildResource(StringUtils.class, "text", "dummy.txt");

        assertEquals("/text/dummy.txt", s1);
        assertEquals("/org/ujorm/tools/common/text/dummy.txt", s2);
    }

//    @Test
//    public void shortSample() throws IOException {
//        Stream<String> rows = Files.lines(Path.of("File.txt"), StandardCharsets.UTF_8);
//    }

}
