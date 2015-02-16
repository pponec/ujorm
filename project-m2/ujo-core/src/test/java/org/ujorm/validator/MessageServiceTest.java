/*
 * Copyright 2012-2015 Ponec
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
package org.ujorm.validator;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author ponec
 */
public class MessageServiceTest extends TestCase {

    /** Message Service */
    private static final MessageService service = new MessageService();
    /** Message Arguments */
    private static MessageArg<Integer> ID = new MessageArg<Integer>("ID");
    private static MessageArg<Date> DATE = new MessageArg<Date>("DATE");
    private static MessageArg<String> TEXT = new MessageArg<String>("TEXT");
    private static MessageArg<BigDecimal> NUMBER = new MessageArg<BigDecimal>("NUMBER");

    public MessageServiceTest() {
    }

    public MessageServiceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /** Test of map method, of class MessageService. */
    public void testTemplate_1() {
        System.out.println("testTemplate");
        String expResult = "A${ID}B";
        String result = service.template("A", ID, "B");
        assertEquals(expResult, result);
    }

    /** Test of map method, of class MessageService. */
    public void testTemplate_2() {
        System.out.println("testTemplate");
        String expResult = "${ID}AB";
        String result = service.template(ID, "AB");
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    public void testFormat_1a() {
        System.out.println("format");
        String expResult = "Number is 1";
        String template = ("Number is ${ID}");
        Map<String, Object> args = service.map(ID, 1);
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    public void testFormat_1b() {
        System.out.println("format");
        String expResult = "Number is 1";
        String template = ("${TEXT} is 1");
        Map<String, Object> args = service.map(TEXT, "Number");
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    public void testFormat_2() {
        System.out.println("format");
        String expResult = "Price is 1 CZK";
        String template = service.template("Price is ", ID, " ", TEXT);
        Map<String, Object> args = service.map(ID, 1, TEXT, "CZK");
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    public void testFormat_3() {
        System.out.println("format");
        String expResult = "1 CZK";
        String template = service.template(ID, " ", TEXT);
        Map<String, Object> args = service.map(ID, 1, TEXT, "CZK", "WRONG ARGUMENT");
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    public void testFormat_4() {
        System.out.println("format");
        String expResult = "Price is 1 ${TEXT}";
        String template = service.template("Price is ", ID, " ", TEXT);
        Map<String, Object> args = service.map(ID, 1, DATE, new Date());
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    public void testFormat_5() {
        System.out.println("format");
        String expResult = "Price is   +123.46 CZK";
        String template = service.template("Price is ${NUMBER,%+9.2f} ${TEXT}");
        Map<String, Object> args = service.map(NUMBER, new BigDecimal("123.456"), TEXT, "CZK");
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Test of format method, of class MessageService. */
    public void testFormat_6() {
        System.out.println("format");
        String expResult = "Date is: 2015-10-20T01:02:03";
        String template = "Date is: ${DATE,%tY-%tm-%tdT%tH:%tM:%tS}";
        Map<String, Object> args = service.map(DATE, getCalendar().getTime());
        String result = service.format(template, args, Locale.ENGLISH);
        assertEquals(expResult, result);
    }

    /** Create a new Calendar for date: 2015-10-20T01:02:03  */
    private Calendar getCalendar() {
        return getCalendar(2015, Calendar.OCTOBER, 20, 1, 2, 3);
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
