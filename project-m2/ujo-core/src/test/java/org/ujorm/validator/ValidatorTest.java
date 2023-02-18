/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.validator;

import org.ujorm.tools.msg.MessageService;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.ujorm.Key;
import org.ujorm.MyTestCase;
import org.ujorm.Validator;
import static org.ujorm.validator.impl.BetweenValidator.*;

/**
 * TextCase
 * @author Pavel Ponec
 */
public class ValidatorTest extends MyTestCase {

    /** Check the input must not be great than 10. */
    @Test
    public void test_MAX_10() {
        System.out.println("test_MAX_10");

        Integer maxValidValue = 10;
        Integer wrongValue = 120;
        Validator<Integer> maxValidator = Validator.Build.max(maxValidValue);
        ValidationError error = maxValidator.validate(wrongValue, null, null);

        // Or check the default message:
        String expectedMesage = "An attribute ''"
                + " must be less than or equals to the 10,"
                + " but the input is: 120";
        String defaultMessage = error.getDefaultMessage();
        assertEquals(expectedMesage, defaultMessage);

        // Check the localized message using a template:
        String template = "My input ${INPUT} must be up to ${LIMIT}.";
        String myMessage = error.getMessage(template);
        assertEquals("My input 120 must be up to 10.", myMessage);
    }

    /** Check the input must be in a range from 1 to 10 */
    @Test
    public void test_RANGE_from_1_to_10() {
        System.out.println("test_RANGE_from_1_to_10");

        Integer minValidValue = 1;
        Integer maxValidValue = 9;
        Integer wrongValue = 130;
        Validator<Integer> rangeValidator = Validator.Build.range(minValidValue, maxValidValue);
        ValidationError error = rangeValidator.validate(wrongValue, ValidBo.CODE, null);
        //
        String expectedDefaultMesage = "An attribute ValidBo.CODE must be between 1 and 9 (including)"
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
    @Test
    public void test_DATE_format() {
        System.out.println("test_DATE");

        Date maxDay = getPastDate();
        Date wrongDay = getNextDate(0);

        Validator<Date> maxValidator = Validator.Build.max(maxDay);
        ValidationError error = maxValidator.validate(wrongDay, null, null);

        // Check the localized message using the template:
        String expectedMessage = "The variable ${LIMIT} = 1971-12-31 22:50:10.";
        String myTemplate = "The variable ${MARK}LIMIT} = ${LIMIT,%tF %tT}.";
        String message = error.getMessage(myTemplate);
        assertEquals(expectedMessage, message);
    }

    /** Test of readValue method, */
    @Test
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
        assertAssign(true , ValidBo.LENGTH_MAX_3, "");
        assertAssign(true , ValidBo.LENGTH_MAX_3, "1");
        assertAssign(true , ValidBo.LENGTH_MAX_3, "12");
        assertAssign(true , ValidBo.LENGTH_MAX_3, "123");
        assertAssign(false, ValidBo.LENGTH_MAX_3, "1234");
        assertAssign(true , ValidBo.LENGTH_MAX_3, null);
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
        //
        assertAssign(true , ValidBo.NUMBER_TYPE, 1);
        assertAssign(true , ValidBo.NUMBER_TYPE, BigDecimal.ONE);
        assertAssign(true , ValidBo.NUMBER_TYPE, 2.99);
        assertAssign(true , ValidBo.NUMBER_TYPE, null);
        //sertAssign(false, ValidBo.NUMBER_TYPE, (Number)(Object)"wrong");
        //sertAssign(false, ValidBo.NUMBER_TYPE, (Number)(Object)new Date());
        //
        assertAssign(true , ValidBo.NUMBER_TYPE_EXPL, 1);
        assertAssign(true , ValidBo.NUMBER_TYPE_EXPL, BigDecimal.ONE);
        assertAssign(true , ValidBo.NUMBER_TYPE_EXPL, 2.99);
        assertAssign(true , ValidBo.NUMBER_TYPE_EXPL, null);
        //sertAssign(false, ValidBo.NUMBER_TYPE_EXPL, (Number)(Object)"wrong");
        //sertAssign(false, ValidBo.NUMBER_TYPE_EXPL, (Number)(Object)new Date());
    }

    /** Test of readValue method, */
    @Test
    public void testMayAssignsLegal() {
        assertAssignLeg(true , ValidMandatory.NOTNULL, "");
        assertAssignLeg(true , ValidMandatory.NOTNULL, "TEXT");
        assertAssignLeg(false, ValidMandatory.NOTNULL, null);
        //
        assertAssignLeg(true , ValidMandatory.NOT_EMPTY, " ");
        assertAssignLeg(true , ValidMandatory.NOT_EMPTY, "TEXT");
        assertAssignLeg(false, ValidMandatory.NOT_EMPTY, "");
        assertAssignLeg(false, ValidMandatory.NOT_EMPTY, null);
        //
        assertAssignLeg(false, ValidMandatory.NOT_BLANK, " ");
        assertAssignLeg(false, ValidMandatory.NOT_BLANK, "  ");
        assertAssignLeg(false, ValidMandatory.NOT_BLANK, "\t");
        assertAssignLeg(true , ValidMandatory.NOT_BLANK, "TEXT");
        assertAssignLeg(false, ValidMandatory.NOT_BLANK, "");
        assertAssignLeg(false, ValidMandatory.NOT_BLANK, null);
        //
        assertAssignLeg(true , ValidMandatory.MAX_10, -1.0);
        assertAssignLeg(true , ValidMandatory.MAX_10, 0.0);
        assertAssignLeg(true , ValidMandatory.MAX_10, 9.99);
        assertAssignLeg(true , ValidMandatory.MAX_10, 10.0);
        assertAssignLeg(false, ValidMandatory.MAX_10, 10.0001);
        assertAssignLeg(false, ValidMandatory.MAX_10, 20.0);
        assertAssignLeg(false, ValidMandatory.MAX_10, 30.0001);
        assertAssignLeg(false, ValidMandatory.MAX_10, null);
        //
        assertAssignLeg(true , ValidMandatory.FORBIDDEN_1_3, 0);
        assertAssignLeg(false, ValidMandatory.FORBIDDEN_1_3, 1);
        assertAssignLeg(true , ValidMandatory.FORBIDDEN_1_3, 2);
        assertAssignLeg(false, ValidMandatory.FORBIDDEN_1_3, 3);
        assertAssignLeg(true , ValidMandatory.FORBIDDEN_1_3, 4);
        assertAssignLeg(false, ValidMandatory.FORBIDDEN_1_3, null);
        //
        assertAssignLeg(false, ValidMandatory.REQUIRED_1_3, 0);
        assertAssignLeg(true , ValidMandatory.REQUIRED_1_3, 1);
        assertAssignLeg(false, ValidMandatory.REQUIRED_1_3, 2);
        assertAssignLeg(true , ValidMandatory.REQUIRED_1_3, 3);
        assertAssignLeg(false, ValidMandatory.REQUIRED_1_3, 4);
        assertAssignLeg(false, ValidMandatory.REQUIRED_1_3, null);
        //
        assertAssignLeg(true , ValidMandatory.MIN_10, 20.0);
        assertAssignLeg(false, ValidMandatory.MIN_10, 9.9);
        assertAssignLeg(false, ValidMandatory.MIN_10, 0.1);
        assertAssignLeg(false, ValidMandatory.MIN_10, null);
        //
        assertAssignLeg(true , ValidMandatory.LENGTH_MAX_3, "");
        assertAssignLeg(true , ValidMandatory.LENGTH_MAX_3, "1");
        assertAssignLeg(true , ValidMandatory.LENGTH_MAX_3, "12");
        assertAssignLeg(true , ValidMandatory.LENGTH_MAX_3, "123");
        assertAssignLeg(false, ValidMandatory.LENGTH_MAX_3, "1234");
        assertAssignLeg(false, ValidMandatory.LENGTH_MAX_3, null);
        //
        assertAssignLeg(false, ValidMandatory.LENGTH_2_4, "");
        assertAssignLeg(false, ValidMandatory.LENGTH_2_4, "1");
        assertAssignLeg(true , ValidMandatory.LENGTH_2_4, "12");
        assertAssignLeg(true , ValidMandatory.LENGTH_2_4, "123");
        assertAssignLeg(true , ValidMandatory.LENGTH_2_4, "1234");
        assertAssignLeg(false, ValidMandatory.LENGTH_2_4, "12345");
        assertAssignLeg(false, ValidMandatory.LENGTH_2_4, null);
        //
        assertAssignLeg(false, ValidMandatory.BETWEEN_1_10, 0);
        assertAssignLeg(true , ValidMandatory.BETWEEN_1_10, 9);
        assertAssignLeg(false, ValidMandatory.BETWEEN_1_10, 10); // !!!
        assertAssignLeg(false, ValidMandatory.BETWEEN_1_10, 11);
        assertAssignLeg(false, ValidMandatory.BETWEEN_1_10, null);
        //
        assertAssignLeg(false, ValidMandatory.RANGE_1_10, 0);
        assertAssignLeg(true , ValidMandatory.RANGE_1_10, 9);
        assertAssignLeg(true , ValidMandatory.RANGE_1_10, 10); // !!!
        assertAssignLeg(false, ValidMandatory.RANGE_1_10, 11);
        assertAssignLeg(false, ValidMandatory.RANGE_1_10, null);
        //
        assertAssignLeg(false, ValidMandatory.CRN_CODE_3, new Relation(0));
        assertAssignLeg(true , ValidMandatory.CRN_CODE_3, new Relation(3));
        assertAssignLeg(false, ValidMandatory.CRN_CODE_3, new Relation(4));
        assertAssignLeg(false, ValidMandatory.CRN_CODE_3, null);
        //
        assertAssignLeg(false, ValidMandatory.FUTURE, getNextDate(-1));
        assertAssignLeg(false, ValidMandatory.FUTURE, getNextDate(0));
        assertAssignLeg(true , ValidMandatory.FUTURE, getNextDate(10));
        assertAssignLeg(false, ValidMandatory.FUTURE, null);
        //
        assertAssignLeg(true , ValidMandatory.PAST, getNextDate(-1));
        assertAssignLeg(true , ValidMandatory.PAST, getNextDate(0));
        assertAssignLeg(false, ValidMandatory.PAST, getNextDate(10));
        assertAssignLeg(false, ValidMandatory.PAST, null);
        //
        assertAssignLeg(true , ValidMandatory.REG_EXP, "TEST");
        assertAssignLeg(false, ValidMandatory.REG_EXP, "TES_");
        assertAssignLeg(false, ValidMandatory.REG_EXP, null);
        //
        assertAssignLeg(true , ValidMandatory.MAIL, "abc@ujorm.net");
        assertAssignLeg(true , ValidMandatory.MAIL, "a.b@u.net");
        assertAssignLeg(true , ValidMandatory.MAIL, "a@u.net");
        assertAssignLeg(true , ValidMandatory.MAIL, "a@info.ujorm.net");
        assertAssignLeg(false, ValidMandatory.MAIL, "a@ujorm.n");
        assertAssignLeg(false, ValidMandatory.MAIL, "a@ujorm");
        assertAssignLeg(false, ValidMandatory.MAIL, "@ujorm.ne");
        assertAssignLeg(false, ValidMandatory.MAIL, "a@.ne");
        assertAssignLeg(false, ValidMandatory.MAIL, "a@ne.");
        assertAssignLeg(false, ValidMandatory.MAIL, "a@u.");
        assertAssignLeg(false, ValidMandatory.MAIL, "a b@ujorm.net");
        assertAssignLeg(false, ValidMandatory.MAIL, "a.b@uj rm.net");
        assertAssignLeg(false, ValidMandatory.MAIL, "a.b@ujorm.n t");
        assertAssignLeg(false, ValidMandatory.MAIL, "@abc@ujorm.net");
        assertAssignLeg(false, ValidMandatory.MAIL, "a@ujorm.net@");
        assertAssignLeg(false, ValidMandatory.MAIL, "a@@ujorm.net");
        assertAssignLeg(false, ValidMandatory.MAIL, null);
        //
        assertAssignLeg(false, ValidMandatory.READ_ONLY, "text");
        assertAssignLeg(false, ValidMandatory.READ_ONLY, null);  // !!!
        assertAssignLeg(true , ValidMandatory.ALL_ALLOWED, "text");
        assertAssignLeg(true , ValidMandatory.ALL_ALLOWED, null);
        //
        assertAssignLeg(false, ValidMandatory.COMPOSITE_AND, -1);
        assertAssignLeg(true , ValidMandatory.COMPOSITE_AND,  0);
        assertAssignLeg(true , ValidMandatory.COMPOSITE_AND,  1);
        assertAssignLeg(true , ValidMandatory.COMPOSITE_AND,  9);
        assertAssignLeg(true , ValidMandatory.COMPOSITE_AND, 10);
        assertAssignLeg(false, ValidMandatory.COMPOSITE_AND, 11);
        assertAssignLeg(false, ValidMandatory.COMPOSITE_AND, null);
        //
        assertAssignLeg(true , ValidMandatory.COMPOSITE_OR, -1);
        assertAssignLeg(true , ValidMandatory.COMPOSITE_OR,  0);
        assertAssignLeg(false, ValidMandatory.COMPOSITE_OR,  1);
        assertAssignLeg(false, ValidMandatory.COMPOSITE_OR,  9);
        assertAssignLeg(true , ValidMandatory.COMPOSITE_OR, 10);
        assertAssignLeg(true , ValidMandatory.COMPOSITE_OR, 11);
        assertAssignLeg(false, ValidMandatory.COMPOSITE_OR, null);
        //
        assertAssignLeg(true , ValidMandatory.NUMBER_TYPE, 1);
        assertAssignLeg(true , ValidMandatory.NUMBER_TYPE, BigDecimal.ONE);
        assertAssignLeg(true , ValidMandatory.NUMBER_TYPE, 2.99);
        assertAssignLeg(false, ValidMandatory.NUMBER_TYPE, null);
        //sertAssignLeg(false, ValidMandatory.NUMBER_TYPE, (Number)(Object)"wrong");
        //sertAssignLeg(false, ValidMandatory.NUMBER_TYPE, (Number)(Object)new Date());
        //
        assertAssignLeg(true , ValidMandatory.NUMBER_TYPE_EXPL, 1);
        assertAssignLeg(true , ValidMandatory.NUMBER_TYPE_EXPL, BigDecimal.ONE);
        assertAssignLeg(true , ValidMandatory.NUMBER_TYPE_EXPL, 2.99);
        assertAssignLeg(false, ValidMandatory.NUMBER_TYPE_EXPL, null);
        //sertAssignLeg(false, ValidMandatory.NUMBER_TYPE_EXPL, (Number)(Object)"wrong");
        //sertAssignLeg(false, ValidMandatory.NUMBER_TYPE_EXPL, (Number)(Object)new Date());
    }


    /** Test of readValue method, */
    @Test
    public void testMandatory() {
        assertTrue(ValidatorUtils.isMandatoryValidator(ValidBo.NOT_NULL.getValidator()));
        assertTrue(ValidatorUtils.isMandatoryValidator(ValidBo.NOT_EMPTY.getValidator()));
        assertTrue(ValidatorUtils.isMandatoryValidator(ValidBo.NOT_BLANK.getValidator()));
        //
        assertFalse(ValidatorUtils.isMandatoryValidator(ValidBo.BETWEEN_1_10.getValidator()));
        assertFalse(ValidatorUtils.isMandatoryValidator(ValidBo.MAIL.getValidator()));
        assertFalse(ValidatorUtils.isMandatoryValidator(ValidBo.FORBIDDEN_1_3.getValidator()));
        //
        assertTrue(ValidatorUtils.isMandatoryValidator(ValidMandatory.BETWEEN_1_10.getValidator()));
        assertTrue(ValidatorUtils.isMandatoryValidator(ValidMandatory.MAIL.getValidator()));
        assertTrue(ValidatorUtils.isMandatoryValidator(ValidMandatory.FORBIDDEN_1_3.getValidator()));
    }

    /** Test of readValue method, */
    @Test
    public void testMaxLength() {
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidBo.NOT_NULL.getValidator()));
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidBo.NOT_EMPTY.getValidator()));
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidBo.NOT_BLANK.getValidator()));
        //
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidBo.BETWEEN_1_10.getValidator()));
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidBo.MAIL.getValidator()));
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidBo.FORBIDDEN_1_3.getValidator()));
        //
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidMandatory.BETWEEN_1_10.getValidator()));
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidMandatory.MAIL.getValidator()));
        assertEquals(-1, ValidatorUtils.getMaxLength(ValidMandatory.FORBIDDEN_1_3.getValidator()));
        //
        assertEquals(4, ValidatorUtils.getMaxLength(ValidBo.LENGTH_2_4.getValidator()));
        assertEquals(3, ValidatorUtils.getMaxLength(ValidBo.LENGTH_MAX_3.getValidator()));
        assertEquals(4, ValidatorUtils.getMaxLength(ValidMandatory.LENGTH_2_4.getValidator()));
        assertEquals(3, ValidatorUtils.getMaxLength(ValidMandatory.LENGTH_MAX_3.getValidator()));
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
        assertEquals(expected, result, resultMsg);
    }

    /** Method to test Validators */
    private <T> void assertAssignLeg(boolean expected, Key<ValidMandatory,T> key, T value) {
        boolean result;
        String resultMsg;
        final ValidMandatory ujo = new ValidMandatory();
        try {
            key.setValue(ujo, value);
            result = true;
            resultMsg = "Invalid assignment!";
        } catch (ValidationException e) {
            result = false;
            resultMsg = e.getMessage();
        }
        assertEquals(expected, result, resultMsg);
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

}
