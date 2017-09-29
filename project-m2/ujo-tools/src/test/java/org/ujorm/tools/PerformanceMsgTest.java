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
     * MAX_COUNT: 5000000 <br>
     * MILISEC_U: 1237.0 100.00% <br>
     * MILISEC_L: 1259.0 98.25% <br>
     * MILISEC_J: 12895.0 9.59% <br>
     * MILISEC_S: 2291.0 53.99% <br>
     * MILISEC_M: 4948.0 25.00% <br>
     */
    @Test
    public void testMessagePerformance_3args() {
        System.out.println("testMessage_3args");
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
            String result = MsgFormatter.format(template, arguments);
            if (result != null) { continue; }

        }
        final double milisecU = beg.until(LocalDateTime.now(), ChronoUnit.MILLIS);

        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = MessageFormatter.arrayFormat(template, arguments).getMessage();
            if (result != null) { continue; }

        }
        final double milisecL = beg.until(LocalDateTime.now(), ChronoUnit.MILLIS);

        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = String.format(temp2ate, arguments);
            if (result != null) { continue; }

        }
        final double milisecJ = beg.until(LocalDateTime.now(), ChronoUnit.MILLIS);

        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = MessageFormat.format(temp3ate, arguments);
            if (result != null) { continue; }
        }
        final double milisecM = beg.until(LocalDateTime.now(), ChronoUnit.MILLIS);

        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = new MessageService().format(temp4ate, mapArgs);
            if (result != null) { continue; }
        }
        final double milisecS = beg.until(LocalDateTime.now(), ChronoUnit.MILLIS);


        System.out.println(PREFIX + "MAX_COUNT: " + MAX_COUNT + SUFFIX);
        System.out.println(PREFIX + "MILISEC_U: " + percent(milisecU, milisecU) + SUFFIX);
        System.out.println(PREFIX + "MILISEC_L: " + percent(milisecU, milisecL) + SUFFIX);
        System.out.println(PREFIX + "MILISEC_J: " + percent(milisecU, milisecJ) + SUFFIX);
        System.out.println(PREFIX + "MILISEC_S: " + percent(milisecU, milisecS) + SUFFIX);
        System.out.println(PREFIX + "MILISEC_M: " + percent(milisecU, milisecM) + SUFFIX);
    }

    /**
     * MAX_COUNT: 5000000 <br>
     * MILISEC_U: 1071.0 100.00% <br>
     * MILISEC_L: 1124.0 95.28% <br>
     */
    @Test
    public void testMessagePerformance_2args() {
        System.out.println("testMessage_2args");
        String template = "~~~{}~~~{}~~~";
        Object argument1 = "A";
        Object argument2 = "B";

        LocalDateTime beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = MsgFormatter.format(template, argument1, argument2);
            if (result != null) { continue; }

        }
        final double milisecU = beg.until(LocalDateTime.now(), ChronoUnit.MILLIS);


        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = MessageFormatter.format(template, argument1, argument2).getMessage();
            if (result != null) { continue; }

        }
        final double milisecL = beg.until(LocalDateTime.now(), ChronoUnit.MILLIS);


        System.out.println(PREFIX + "MAX_COUNT: " + MAX_COUNT + SUFFIX);
        System.out.println(PREFIX + "MILISEC_U: " + percent(milisecU, milisecU) + SUFFIX);
        System.out.println(PREFIX + "MILISEC_L: " + percent(milisecU, milisecL) + SUFFIX);
    }


    /** Calculate percent */
    private static String percent(final double value, final double base) {
        return MsgFormatter.format("{} {}%", base, DECIMAL.format(value/base*100));
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
