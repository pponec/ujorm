/*
 * Copyright 2017-2018 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.msg;

import ch.qos.logback.classic.Logger;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import static java.util.Locale.ENGLISH;
import static org.junit.Assert.assertEquals;

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
     * MsgFormatter	: 1867.0 100.00% <br>
     * MessageFormatter	: 1243.0 150.20% <br>
     * Formatter	: 12185.0 15.32% <br>
     * MessageFormat	: 5047.0 36.99% <br>
     * MessageService	: 2778.0 67.21% <br>
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
     * MsgFormatter	: 2415.0 100.00% <br>
     * MessageFormatter	: 1147.0 210.55% <br>
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

    /** MsgFormatter.*/
    @Test
    public void testMsgFormatter() {
        System.out.println("MsgFormatter");

        String expected = "On 2017-01-15T12:30, we spent 254 EUR.";
        String template = "On {}, we spent {} EUR.";
        LocalDateTime day = LocalDateTime.of(2017, Month.JANUARY, 15, 12, 30);
        String result = MsgFormatter.format(template, day, new BigDecimal("254"));
        assertEquals(expected, result);
    }

    /** MsgFormatter.*/
    @Test
    public void testMessageService() {
        System.out.println("MessageService");

        String expected = "On 2017-01-15, we spent 254.00 EUR.";
        String template = "On ${DAY,%tF}, we spent ${PRICE,%.2f} EUR.";
        MessageService instance = new MessageService();
        Map<String,Object> params = instance.map
              ( "DAY", LocalDateTime.of(2017, Month.JANUARY, 15, 12, 30)
              , "PRICE", new BigDecimal("254"));
        String result = new MessageService().format(template, params);
        assertEquals(expected, result);
    }

    /** Logback cuts unmatched parameters.*/
    @Test
    public void testStringFormat() {
        System.out.println("MessageFormat");

        String expected = "On 2017-01-15, we spent 254.00 EUR.";
        String template = "On %tF, we spent %.2f EUR.";
        LocalDateTime day = LocalDateTime.of(2017, Month.JANUARY, 15, 12, 30);
        String result = String.format(ENGLISH, template, day, new BigDecimal("254"));
        assertEquals(expected, result);
    }

    /** Logback cuts unmatched parameters.*/
    @Test
    public void testMessageFormat() {
        System.out.println("MessageFormat");

        String expected = "On 2017-01-15, we spent 254.00 EUR.";
        String template = "On {0,date,yyyy-MM-dd}, we spent {1,number,#.00} EUR.";
        Date day = Date.from(LocalDateTime.of(2017, Month.JANUARY, 15, 12, 30)
                .atZone(ZoneId.systemDefault()).toInstant());
        Object[] params = { day, new BigDecimal("254")};
        String result = new MessageFormat(template, ENGLISH).format(params);
        assertEquals(expected, result);
    }

    /** Ujorm write all unmatched parameters.*/
    @Test
    public void testMessage2() {
        System.out.println("testMessage2");

        String expected = "~~A~~ B C";
        String template = "~~{}~~";
        Object[] arguments = {"A", "B", "C"};
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expected, result);
    }
}
