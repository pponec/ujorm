/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.validator;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import junit.framework.*;
import org.ujorm.Key;
import org.ujorm.MyTestCase;
import org.ujorm.Validator;
import static org.ujorm.validator.impl.BetweenValidator.*;

/**
 * TextCase
 * @author Pavel Ponec
 */
public class ValidatorTest extends MyTestCase {
    private static final Class CLASS = ValidatorTest.class;


    public ValidatorTest(String testName) {
        super(testName);
    }

    public static TestSuite suite() {
        return new TestSuite(CLASS);
    }

    /** Check the input must not be great than 10. */
    public void test_MAX_10() {
        System.out.println(String.format("test_MAX_10"));

        Integer maxValidValue = 10;
        Integer wrongValue = 120;
        Validator<Integer> maxValidator = Validator.Build.max(maxValidValue);
        ValidationError error = maxValidator.validate(wrongValue, null, null);

        // Check the localized message using a template:
        String myTemplate = "My input must not be great than ${LIMIT}.";
        String message = error.getMessage(myTemplate);
        assertEquals("My input must not be great than 10.", message);

        // Or check the default message:
        String expectedMesage = "An attribute ''"
                + " must be less than or equals to the 10"
                + ", but the input is: 120";
        String defaultMessage = error.getDefaultMessage();
        assertEquals(expectedMesage, defaultMessage);
    }

    /** Check the input must be in a range from 1 to 10 */
    public void test_RANGE_from_1_to_10() {
        System.out.println(String.format("test_RANGE_from_1_to_10"));

        Integer minValidValue = 1;
        Integer maxValidValue = 9;
        Integer wrongValue = 130;
        Validator<Integer> rangeValidator = Validator.Build.range(minValidValue, maxValidValue);
        ValidationError error = rangeValidator.validate(wrongValue, ValidBo.CODE, null);
        //
        String expectedDefaultMesage = "An attribute ValidBo.code must be between 1 and 9 (including)"
                + ", but the input is: 130";
        assertEquals(expectedDefaultMesage, error.getDefaultMessage());
        //
        String expectedMyMesage = "My input is not between 1 and 9";
        String myTemplate = "My input is not between ${MIN} and ${MAX}";
        String message = error.getMessage(myTemplate, Locale.ENGLISH);
        assertEquals(expectedMyMesage, message);
        //
        Object[] arrayTemplate = {"My input is not between ", MIN, " and ", MAX};
        assertEquals(expectedMyMesage, error.getMessage(arrayTemplate, Locale.ENGLISH));
        assertEquals(myTemplate, new MessageService().template(myTemplate));
    }

    /** Check the input must not be great than 10. */
    public void test_DATE_format() {
        System.out.println(String.format("test_DATE"));

        Date maxDay = getPastDate();
        Date wrongDay = getNextDate(0);

        Validator<Date> maxValidator = Validator.Build.max(maxDay);
        ValidationError error = maxValidator.validate(wrongDay, null, null);

        // Check the localized message using the template:
        String expectedMessage = "The variable ${LIMIT} = 1971-12-31 22:50:10.";
        String myTemplate = "The variable ${MARK}LIMIT} = ${LIMIT,%1$tF %1$tT}.";
        String message = error.getMessage(myTemplate);
        assertEquals(expectedMessage, message);
    }

    /** Test of readValue method, */
    public void testMayAssigns() {
        assertAssign(true , ValidBo.NOT_NULL, "");
        assertAssign(true , ValidBo.NOT_NULL, "TEXT");
        assertAssign(false, ValidBo.NOT_NULL, null);
        //
        assertAssign(true , ValidBo.NOT_EMPTY, " ");
        assertAssign(true , ValidBo.NOT_EMPTY, "TEXT");
        assertAssign(false, ValidBo.NOT_EMPTY, "");
        assertAssign(false, ValidBo.NOT_EMPTY, null);
        //
        assertAssign(false, ValidBo.NOT_BLANK, " ");
        assertAssign(false, ValidBo.NOT_BLANK, "  ");
        assertAssign(false, ValidBo.NOT_BLANK, "\t");
        assertAssign(true , ValidBo.NOT_BLANK, "TEXT");
        assertAssign(false, ValidBo.NOT_BLANK, "");
        assertAssign(false, ValidBo.NOT_BLANK, null);
        //
        assertAssign(true , ValidBo.MAX_10, -1.0);
        assertAssign(true , ValidBo.MAX_10, 0.0);
        assertAssign(true , ValidBo.MAX_10, 9.99);
        assertAssign(true , ValidBo.MAX_10, 10.0);
        assertAssign(false, ValidBo.MAX_10, 10.0001);
        assertAssign(false, ValidBo.MAX_10, 20.0);
        assertAssign(false, ValidBo.MAX_10, 30.0001);
        assertAssign(true , ValidBo.MAX_10, null);
        //
        assertAssign(true , ValidBo.FORBIDDEN_1_3, 0);
        assertAssign(false, ValidBo.FORBIDDEN_1_3, 1);
        assertAssign(true , ValidBo.FORBIDDEN_1_3, 2);
        assertAssign(false, ValidBo.FORBIDDEN_1_3, 3);
        assertAssign(true , ValidBo.FORBIDDEN_1_3, 4);
        assertAssign(true , ValidBo.FORBIDDEN_1_3, null);
        //
        assertAssign(false, ValidBo.REQUIRED_1_3, 0);
        assertAssign(true , ValidBo.REQUIRED_1_3, 1);
        assertAssign(false, ValidBo.REQUIRED_1_3, 2);
        assertAssign(true , ValidBo.REQUIRED_1_3, 3);
        assertAssign(false, ValidBo.REQUIRED_1_3, 4);
        assertAssign(true , ValidBo.REQUIRED_1_3, null);
        //
        assertAssign(true , ValidBo.MIN_10, 20.0);
        assertAssign(false, ValidBo.MIN_10, 9.9);
        assertAssign(false, ValidBo.MIN_10, 0.1);
        assertAssign(true , ValidBo.MIN_10, null);
        //
        assertAssign(true , ValidBo.LENGTH_MIN_3, "");
        assertAssign(true , ValidBo.LENGTH_MIN_3, "1");
        assertAssign(true , ValidBo.LENGTH_MIN_3, "12");
        assertAssign(true , ValidBo.LENGTH_MIN_3, "123");
        assertAssign(false, ValidBo.LENGTH_MIN_3, "1234");
        assertAssign(true , ValidBo.LENGTH_MIN_3, null);
        //
        assertAssign(false, ValidBo.LENGTH_2_4, "");
        assertAssign(false, ValidBo.LENGTH_2_4, "1");
        assertAssign(true , ValidBo.LENGTH_2_4, "12");
        assertAssign(true , ValidBo.LENGTH_2_4, "123");
        assertAssign(true , ValidBo.LENGTH_2_4, "1234");
        assertAssign(false, ValidBo.LENGTH_2_4, "12345");
        assertAssign(true , ValidBo.LENGTH_2_4, null);
        //
        assertAssign(false, ValidBo.BETWEEN_1_10, 0);
        assertAssign(true , ValidBo.BETWEEN_1_10, 9);
        assertAssign(false, ValidBo.BETWEEN_1_10, 10); // !!!
        assertAssign(false, ValidBo.BETWEEN_1_10, 11);
        assertAssign(true , ValidBo.BETWEEN_1_10, null);
        //
        assertAssign(false, ValidBo.RANGE_1_10, 0);
        assertAssign(true , ValidBo.RANGE_1_10, 9);
        assertAssign(true , ValidBo.RANGE_1_10, 10); // !!!
        assertAssign(false, ValidBo.RANGE_1_10, 11);
        assertAssign(true , ValidBo.RANGE_1_10, null);
        //
        assertAssign(false, ValidBo.CRN_CODE_3, new Relation(0));
        assertAssign(true , ValidBo.CRN_CODE_3, new Relation(3));
        assertAssign(false, ValidBo.CRN_CODE_3, new Relation(4));
        assertAssign(true , ValidBo.CRN_CODE_3, null);
        //
        assertAssign(false, ValidBo.FUTURE, getNextDate(-1));
        assertAssign(false, ValidBo.FUTURE, getNextDate(0));
        assertAssign(true , ValidBo.FUTURE, getNextDate(10));
        assertAssign(true , ValidBo.FUTURE, null);
        //
        assertAssign(true , ValidBo.PAST, getNextDate(-1));
        assertAssign(true , ValidBo.PAST, getNextDate(0));
        assertAssign(false, ValidBo.PAST, getNextDate(10));
        assertAssign(true , ValidBo.PAST, null);
        //
        assertAssign(true , ValidBo.REG_EXP, "TEST");
        assertAssign(false, ValidBo.REG_EXP, "TES_");
        assertAssign(true , ValidBo.REG_EXP, null);
        //
        assertAssign(true , ValidBo.MAIL, "abc@ujorm.net");
        assertAssign(true , ValidBo.MAIL, "a.b@u.net");
        assertAssign(true , ValidBo.MAIL, "a@u.net");
        assertAssign(true , ValidBo.MAIL, "a@info.ujorm.net");
        assertAssign(false, ValidBo.MAIL, "a@ujorm.n");
        assertAssign(false, ValidBo.MAIL, "a@ujorm");
        assertAssign(false, ValidBo.MAIL, "@ujorm.ne");
        assertAssign(false, ValidBo.MAIL, "a@.ne");
        assertAssign(false, ValidBo.MAIL, "a@ne.");
        assertAssign(false, ValidBo.MAIL, "a@u.");
        assertAssign(false, ValidBo.MAIL, "a b@ujorm.net");
        assertAssign(false, ValidBo.MAIL, "a.b@uj rm.net");
        assertAssign(false, ValidBo.MAIL, "a.b@ujorm.n t");
        assertAssign(false, ValidBo.MAIL, "@abc@ujorm.net");
        assertAssign(false, ValidBo.MAIL, "a@ujorm.net@");
        assertAssign(false, ValidBo.MAIL, "a@@ujorm.net");
        assertAssign(true , ValidBo.MAIL, null);
        //
        assertAssign(false, ValidBo.READ_ONLY, "text");
        assertAssign(false, ValidBo.READ_ONLY, null);  // !!!
        assertAssign(true , ValidBo.ALL_ALLOWED, "text");
        assertAssign(true , ValidBo.ALL_ALLOWED, null);
        //
        assertAssign(false, ValidBo.COMPOSITE_AND, -1);
        assertAssign(true , ValidBo.COMPOSITE_AND,  0);
        assertAssign(true , ValidBo.COMPOSITE_AND,  1);
        assertAssign(true , ValidBo.COMPOSITE_AND,  9);
        assertAssign(true , ValidBo.COMPOSITE_AND, 10);
        assertAssign(false, ValidBo.COMPOSITE_AND, 11);
        assertAssign(true , ValidBo.COMPOSITE_AND, null);
        //
        assertAssign(true , ValidBo.COMPOSITE_OR, -1);
        assertAssign(true , ValidBo.COMPOSITE_OR,  0);
        assertAssign(false, ValidBo.COMPOSITE_OR,  1);
        assertAssign(false, ValidBo.COMPOSITE_OR,  9);
        assertAssign(true , ValidBo.COMPOSITE_OR, 10);
        assertAssign(true , ValidBo.COMPOSITE_OR, 11);
        assertAssign(true , ValidBo.COMPOSITE_OR, null);
    }

    /** Method to test Validators */
    private <T> void assertAssign(boolean expected, Key<ValidBo,T> key, T value) {
        boolean result;
        String resultMsg;
        final ValidBo ujo = new ValidBo();
        try {
            key.setValue(ujo, value);
            result = true;
            resultMsg = "Invalid assignment!";
        } catch (ValidationException e) {
            result = false;
            resultMsg = e.getMessage();
        }
        assertTrue(resultMsg, expected==result);
    }

    /** Returns a next Day */
    private Date getNextDate(int addDay) {
       Calendar calendar = GregorianCalendar.getInstance();
       calendar.add(Calendar.DATE, addDay);
       return calendar.getTime();
    }

    /** Returns a past day: (1971-12-31 22:50:10). */
    private Date getPastDate() {
       Calendar calendar = GregorianCalendar.getInstance();
       calendar.set(Calendar.YEAR, 1971);
       calendar.set(Calendar.MONTH, Calendar.DECEMBER);
       calendar.set(Calendar.DAY_OF_MONTH, 31);
       calendar.set(Calendar.HOUR_OF_DAY, 22);
       calendar.set(Calendar.MINUTE, 50);
       calendar.set(Calendar.SECOND, 10);
       calendar.set(Calendar.MILLISECOND, 0);

       return calendar.getTime();
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
