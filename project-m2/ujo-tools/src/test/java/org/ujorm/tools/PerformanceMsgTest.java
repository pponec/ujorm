package org.ujorm.tools;

/*
 * Copyright 2017-2017 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import ch.qos.logback.classic.Logger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import static org.junit.Assert.*;

/**
 * Tests of the Message implementation
 * @author Pavel Ponec
 */
public class PerformanceMsgTest {

    /** Iterations for s performance tests */
    private static final int MAX_COUNT = 1; // 5_000_000;

    /** Report message prefix */
    private static final String PREFIX = "     * ";

    /** Report message prefix */
    private static final String SUFFIX = " <br>";

    /** Decimal formatter */
    private static final DecimalFormat DECIMAL = new DecimalFormat("#0.00"
            , DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    /**
     * MAX_COUNT	: 5000000 <br>
     * MsgFormatter	: 1158.0 100.00% <br>
     * MessageFormatter	: 1246.0 92.94% <br>
     * Formatter	: 12243.0 9.46% <br>
     * MessageFormat	: 5023.0 23.05% <br>
     * MessageService	: 2268.0 51.06% <br>
     */
    @Test
    public void testMessagePerformance_3args() {
        System.out.println("testMessage_3args");
        Map<Class, Long> result = new LinkedHashMap<>();
        String template = "~~~{}~~~{}~~~{}~~~";
        String temp2ate = "~~~%s~~~%s~~~%s~~~";
        String temp3ate = "~~~{0}~~~{1}~~~{2}~~~";
        String temp4ate = "~~~${0}~~~${1}~~~${2}~~~";
        Object[] arguments = {"A","B","C"};
        Map<String, Object> mapArgs = new MessageService().map
               ( "0", arguments[0]
               , "1", arguments[1]
               , "2", arguments[2]);

        LocalDateTime beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String msg = MsgFormatter.format(template, arguments);
            if (msg != null) { continue; }

        }
        result.put(MsgFormatter.class, beg.until(LocalDateTime.now(), ChronoUnit.MILLIS));

        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String msg = MessageFormatter.arrayFormat(template, arguments).getMessage();
            if (msg != null) { continue; }

        }
        result.put(MessageFormatter.class, beg.until(LocalDateTime.now(), ChronoUnit.MILLIS));

        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String msg = new Formatter().format(temp2ate, arguments).toString();
            if (msg != null) { continue; }

        }
        result.put(Formatter.class, beg.until(LocalDateTime.now(), ChronoUnit.MILLIS));

        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String msg = MessageFormat.format(temp3ate, arguments);
            if (msg != null) { continue; }
        }
        result.put(MessageFormat.class, beg.until(LocalDateTime.now(), ChronoUnit.MILLIS));

        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String msg = new MessageService().format(temp4ate, mapArgs);
            if (msg != null) { continue; }
        }
        result.put(MessageService.class, beg.until(LocalDateTime.now(), ChronoUnit.MILLIS));

        System.out.println(PREFIX + "MAX_COUNT\t: " + MAX_COUNT + SUFFIX);
        for (Class formatter : result.keySet()) {
            System.out.println(buildResult(formatter, result));
        }
    }

    /**
     * MAX_COUNT	: 5000000 <br>
     * MsgFormatter	: 1065.0 100.00% <br>
     * MessageFormatter	: 1290.0 82.56% <br>
     */
    @Test
    public void testMessagePerformance_2args() {
        System.out.println("testMessage_2args");
        Map<Class, Long> result = new LinkedHashMap<>();
        String template = "~~~{}~~~{}~~~";
        Object argument1 = "A";
        Object argument2 = "B";

        LocalDateTime beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String msg = MsgFormatter.format(template, argument1, argument2);
            if (msg != null) { continue; }

        }
        result.put(MsgFormatter.class, beg.until(LocalDateTime.now(), ChronoUnit.MILLIS));


        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String msg = MessageFormatter.format(template, argument1, argument2).getMessage();
            if (msg != null) { continue; }

        }
        result.put(MessageFormatter.class, beg.until(LocalDateTime.now(), ChronoUnit.MILLIS));


        System.out.println(PREFIX + "MAX_COUNT\t: " + MAX_COUNT + SUFFIX);
        for (Class formatter : result.keySet()) {
            System.out.println(buildResult(formatter, result));
        }
    }


    /** Calculate percent */
    private static String buildResult(Class<?> item, Map<Class, Long> result) {
        final double value = result.get(item);
        final double base = result.get(MsgFormatter.class);
        return MsgFormatter.format("{}{}\t: {} {}%{}"
                , PREFIX
                , item.getSimpleName()
                , value
                , DECIMAL.format(base/value*100)
                , SUFFIX)
                ;
    }

    /** Logback cuts unmatched parameters.*/
    @Test
    public void testMessage1() {
        System.out.println("testMessage1");

        String expected = "~~A~~";
        String template = "~~{}~~";
        Object[] arguments = {"A", "B", "C"};
        String result = MessageFormatter.arrayFormat(template, arguments).getMessage();
        assertEquals(expected, result);

        Logger logger = (Logger) LoggerFactory.getLogger(PerformanceMsgTest.class);
        logger.info(template, arguments);
    }

    /** Ujorm write all unmatched parameters.*/
    @Test
    public void testMessage2() {
        System.out.println("testMessage2");

        String expected = "~~A~~, B, C";
        String template = "~~{}~~";
        Object[] arguments = {"A", "B", "C"};
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expected, result);
    }
}
