/*
 * Copyright 2021-2026 Pavel Ponec
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

import java.math.BigDecimal;
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

    @Test
    void testFormatSeparator() throws Exception {
        assertEquals("", formatSeparator(null));
        assertEquals("0", formatSeparator(0));
        assertEquals("100", formatSeparator(100));
        assertEquals("1_000", formatSeparator(1000));
        assertEquals("1_000_000", formatSeparator(1000000));
        assertEquals("-5_000", formatSeparator(-5000));
        assertEquals("1_234.56", formatSeparator(1234.56D));
        assertEquals("0.1", formatSeparator(0.1F));
        assertEquals("0.005", formatSeparator(0.005F));
        assertEquals("123_456.789", formatSeparator(new BigDecimal("123456.789")));
    }

    /** Replace thousands separator by '_'. */
    private String formatSeparator(Number number) {
        final var thousendSeparator = '_';
        final var result = StringUtils.formatSeparator(number);
        return (result != null && result.indexOf(StringUtils.NARROW_NBSP) >= 0)
                ? result.replace(StringUtils.NARROW_NBSP, thousendSeparator)
                : result;
    }

    @Test
    void technicalLocale() {
        var nbsp = StringUtils.NBSP;
        var value = String.format(StringUtils.TECHNICAL_LOCALE, "%,d", 5_400);
        assertEquals("5" + nbsp + "400", value);

        value = String.format(StringUtils.TECHNICAL_LOCALE, "%,.3f", 5_400.3456789);
        assertEquals("5" + nbsp + "400,346", value);

        value = String.format(StringUtils.TECHNICAL_LOCALE, "%,.6f", 5_400.3456789);
        assertEquals("5" + nbsp + "400,345679", value);
    }

}
