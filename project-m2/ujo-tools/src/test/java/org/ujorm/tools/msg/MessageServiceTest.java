/*
 * Copyright 2012-2022 Pavel Ponec, https://github.com/pponec
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the class MessageService
 * @author Pavel Ponec
 */
public class MessageServiceTest {

    /** Message Argument */
    private static final MessageArg ID = new MessageArg("ID");
    /** Message Argument */
    private static final MessageArg DATE = new MessageArg("DATE");
    /** Message Argument */
    private static final MessageArg TEXT = new MessageArg("TEXT");
    /** Message Argument */
    private static final MessageArg NUMBER = new MessageArg("NUMBER");

    /** Demo test 1. */
    @Test
    public void testDemo1() {
        final MessageArg TYPE = MessageArg.of("TYPE");
        final MessageArg NAME = MessageArg.of("NAME");

        String expResult = "The ORM framework Ujorm.";
        String template = "The " + TYPE + " framework " + NAME + ".";
        String result = MessageService.formatMsg(template, TYPE, "ORM", NAME, "Ujorm");
        assertEquals(expResult, result);
    }

    /** Demo test 2. */
    @Test
    public void testDemo2() {
        final MessageService service = new MessageService();
        final MessageArg TYPE = MessageArg.of("TYPE");
        final MessageArg NAME = MessageArg.of("NAME");

        String expResult = "The ORM framework Ujorm.";
        String expTemplate = "The ${TYPE} framework ${NAME}.";
        String template = service.template("The ", TYPE, " framework ", NAME, ".");

        Map<String, Object> args = new HashMap<>();
        args.put(TYPE.name(), "ORM");
        args.put(NAME.name(), "Ujorm");

        String result = service.format(template, args);
        assertEquals(expTemplate, template);
        assertEquals(expResult, result);
    }

    /** Test of map method, of class MessageService. */
    @Test
    public void testFormatTemplate() {
        final MessageService service = new MessageService();
        final MessageArg DAY = new MessageArg("DAY", "%tY-%tm-%td");
        final MessageArg PRICE = new MessageArg("PRICE", "%.2f");

        String expResult = "On 2017-01-15, we spent 254.00 EUR.";
        String expTemplate = "On ${DAY,%tY-%tm-%td}, we spent ${PRICE,%.2f} EUR.";
        String template = service.template("On ", DAY, ", we spent ", PRICE, " EUR.");
        Map<String, Object> args = service.map
               ( DAY, LocalDateTime.of(2017, Month.JANUARY, 15, 12, 30)
               , PRICE, new BigDecimal("254"));
        String result = service.format(template, args);
        assertEquals(expTemplate, template);
        assertEquals(expResult, result);
    }

    /** Test of map method, of class MessageService. */
    @Test
    public void testTemplate_1() {
        System.out.println("testTemplate");

        MessageService service = new MessageService();
        String expResult = "A${ID}B";
        String result = service.template("A", ID, "B");
        assertEquals(expResult, result);
    }

    /** Test of map method, of class MessageService. */
    @Test
    public void testTemplate_2() {
        System.out.println("testTemplate");

        MessageService service = new MessageService();
        String expResult = "${ID}AB";
        String result = service.template(ID, "AB");
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_1a() {
        System.out.println("format");

        MessageService service = new MessageService();
        String expResult = "Number is 1";
        String template = ("Number is ${ID}");
        Map<String, Object> args = service.map(ID, 1);
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_1b() {
        System.out.println("format");

        MessageService service = new MessageService();
        String expResult = "Number is 1";
        String template = ("${TEXT} is 1");
        Map<String, Object> args = service.map(TEXT, "Number");
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_1c() {
        System.out.println("format");

        String expResult = "Number is 1";
        String template = ("Number is ${ID}");
        String result = MessageService.formatMsg(template, ID.getName(), 1);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_2() {
        System.out.println("format");

        MessageService service = new MessageService();
        String expResult = "Price is 1 CZK";
        String template = service.template("Price is ", ID, " ", TEXT);
        Map<String, Object> args = service.map(ID, 1, TEXT, "CZK");
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_3() {
        System.out.println("format");

        MessageService service = new MessageService();
        String expResult = "1 CZK";
        String template = service.template(ID, " ", TEXT);
        Map<String, Object> args = service.map(ID, 1, TEXT, "CZK", "WRONG ARGUMENT");
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_4() {
        System.out.println("format");

        MessageService service = new MessageService();
        String expResult = "Price is 1 ${TEXT}";
        String template = service.template("Price is ", ID, " ", TEXT);
        Map<String, Object> args = service.map(ID, 1, DATE, new Date());
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_5() {
        System.out.println("format");

        MessageService service = new MessageService();
        String expResult = "Price is   +123.46 CZK";
        String template = service.template("Price is ${NUMBER,%+9.2f} ${TEXT}");
        Map<String, Object> args = service.map(NUMBER, new BigDecimal("123.456"), TEXT, "CZK");
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_6() {
        System.out.println("format");

        MessageService service = new MessageService();
        String expResult = "Date is: 2016-05-04 03:02:01";
        Map<String, Object> args = service.map(DATE, getCalendar().getTime());
        //
        String template1 = "Date is: ${DATE,%tY-%tm-%td %tH:%tM:%tS}";
        String result1 = service.format(template1, args, Locale.ENGLISH);
        assertEquals(expResult, result1);
        //
        String template2 = "Date is: ${DATE,%tF %tT}";
        String result2 = service.format(template2, args, Locale.ENGLISH);
        assertEquals(expResult, result2);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testFormat_7() {
        System.out.println("format");

        MessageService service = new MessageService();
        String expResult = "Date is: 2016-05-04 03:02:01";
        Map<String, Object> args = service.map(DATE, LocalDateTime.parse("2016-05-04T03:02:01"));
        //
        String template1 = "Date is: ${DATE,%tY-%tm-%td %tH:%tM:%tS}";
        String result1 = service.format(template1, args, Locale.ENGLISH);
        assertEquals(expResult, result1);
        //
        String template2 = "Date is: ${DATE,%tF %tT}";
        String result2 = service.format(template2, args, Locale.ENGLISH);
        assertEquals(expResult, result2);
    }

    /** Test of format method, of class MessageService. */
    @Test
    public void testEquals() {
        System.out.println("equals");
        assertEquals(ID, ID);
        assertNotEquals(ID, DATE);
    }

    /** Create a new Calendar for date: 2016-05-04T03:02:01  */
    private Calendar getCalendar() {
        return getCalendar(2016, Calendar.MAY, 4, 3, 2, 1);
    }

    /** Create a new Calendar */
    private Calendar getCalendar(int year, int month, int day, int hour, int minute, int sec) {
        final Calendar result = Calendar.getInstance(Locale.ENGLISH);
        result.set(Calendar.YEAR, year);
        result.set(Calendar.MONTH, month);
        result.set(Calendar.DAY_OF_MONTH, day);
        result.set(Calendar.HOUR_OF_DAY, hour);
        result.set(Calendar.MINUTE, minute);
        result.set(Calendar.SECOND, sec);
        result.set(Calendar.MILLISECOND, 0);
        //
        return result;
    }

}
