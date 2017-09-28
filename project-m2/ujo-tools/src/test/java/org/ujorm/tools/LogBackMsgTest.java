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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import static org.junit.Assert.*;

/**
 * Tests of the Message implementation
 * @author Pavel Ponec
 */
public class LogBackMsgTest {

    /** Iterations for s performance tests */
    private static final int MAX_COUNT = 1; // 10_000_000;

    /** Report message prefix */
    private static final String PREFIX = "     * ";

    /** Report message prefix */
    private static final String SUFFIX = " <br>";

    /** Logback cuts unmatched parameters.*/
    @Test
    public void testMessage1() {
        System.out.println("testMessage1");

        String expected = "~~A~~";
        String template = "~~{}~~";
        Object[] arguments = {"A", "B", "C"};
        String result = MessageFormatter.arrayFormat(template, arguments).getMessage();
        assertEquals(expected, result);

        Logger logger = (Logger) LoggerFactory.getLogger(LogBackMsgTest.class);
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


    /**
     * MAX_COUNT: 10000000 <br>
     * MILISEC_U: 2033.0 <br>
     * MILISEC_L: 2083.0 <br>
     * PERCENTS : 97.59961593855017
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
        LocalDateTime end = LocalDateTime.now();
        final double milisecU = beg.until(end, ChronoUnit.MILLIS);


        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = MessageFormatter.format(template, argument1, argument2).getMessage();
            if (result != null) { continue; }

        }
        end = LocalDateTime.now();
        final double milisecL = beg.until(end, ChronoUnit.MILLIS);


        System.out.println(PREFIX + "MAX_COUNT: " + MAX_COUNT + SUFFIX);
        System.out.println(PREFIX + "MILISEC_U: " + milisecU + SUFFIX);
        System.out.println(PREFIX + "MILISEC_L: " + milisecL + SUFFIX);
        System.out.println(PREFIX + "PERCENTS : " + milisecU/milisecL*100);
    }


    /**
     * MAX_COUNT: 10000000 <br>
     * MILISEC_U: 2250.0 <br>
     * MILISEC_L: 2428.0 <br>
     * PERCENTS : 92.66886326194398
     */
    @Test
    public void testMessagePerformance_3args() {
        System.out.println("testMessage_3args");
        String template = "~~~{}~~~{}~~~{}~~~";
        Object[] arguments = {"A","B","C"};

        LocalDateTime beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = MsgFormatter.format(template, arguments);
            if (result != null) { continue; }

        }
        LocalDateTime end = LocalDateTime.now();
        final double milisecU = beg.until(end, ChronoUnit.MILLIS);


        beg = LocalDateTime.now();
        for (int i = 0; i < MAX_COUNT; i++) {
            String result = MessageFormatter.arrayFormat(template, arguments).getMessage();
            if (result != null) { continue; }

        }
        end = LocalDateTime.now();
        final double milisecL = beg.until(end, ChronoUnit.MILLIS);


        System.out.println(PREFIX + "MAX_COUNT: " + MAX_COUNT + SUFFIX);
        System.out.println(PREFIX + "MILISEC_U: " + milisecU + SUFFIX);
        System.out.println(PREFIX + "MILISEC_L: " + milisecL + SUFFIX);
        System.out.println(PREFIX + "PERCENTS : " + milisecU/milisecL*100);
    }

}
