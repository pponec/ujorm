package org.ujorm.core.criterion;

import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.domains.Employee;
import org.ujorm.core.criterion.domains.MetaEmployee;
import org.ujorm.tools.common.Array;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for ValueCriterion */
public class ValueCriterionTest {

    /** Test name constant */
    public static final String TEST_NAME = "John";

    /** Test initialization with a default operator */
    @Test
    public void testDefaultOperator() {
        var criterion = new ValueCriterion<>(MetaEmployee.name, null, TEST_NAME);
        assertEquals(Operator.EQ, criterion.getOperator());
    }

    /** Test CharSequence validation with valid data */
    @Test
    public void testCharSequenceValidationValid() {
        var criterion = new ValueCriterion<>(MetaEmployee.name, Operator.STARTS, TEST_NAME);
        assertEquals(Operator.STARTS, criterion.getOperator());
    }

    /** Test CharSequence validation with invalid data */
    @Test
    public void testCharSequenceValidationInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ValueCriterion<>(MetaEmployee.name, Operator.STARTS, 123);
        });
    }

    /** Test validation for non-string key with string operator */
    @Test
    public void testInvalidKeyTypeForStringOperator() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Assuming MetaEmployee.id is not a String key
            new ValueCriterion<>(MetaEmployee.id, Operator.STARTS, "1");
        });
    }

    /** Test Array validation with valid data */
    @Test
    public void testArrayValidationValid() {
        var values = createDummyArray();
        var criterion = new ValueCriterion<>(MetaEmployee.name, Operator.IN, values);
        assertEquals(Operator.IN, criterion.getOperator());
    }

    /** Test Array validation with invalid data */
    @Test
    public void testArrayValidationInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ValueCriterion<>(MetaEmployee.name, Operator.IN, TEST_NAME);
        });
    }

    /** Test logical OR operation with ALWAYS_TRUE */
    @Test
    public void testJoinAlwaysTrueOr() {
        var criterion1 = ValueCriterion.TRUE;
        var criterion2 = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        var result = criterion1.join(BinaryOperator.OR, criterion2);

        assertSame(criterion1, result);
    }

    /** Test logical AND operation with ALWAYS_TRUE */
    @Test
    public void testJoinAlwaysTrueAnd() {
        var criterion1 = ValueCriterion.TRUE;
        var criterion2 = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        var result = criterion1.join(BinaryOperator.AND, criterion2);

        assertSame(criterion2, result);
    }

    /** Test logical OR operation with ALWAYS_FALSE */
    @Test
    public void testJoinAlwaysFalseOr() {
        var criterion1 = ValueCriterion.FALSE;
        var criterion2 = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        var result = criterion1.join(BinaryOperator.OR, criterion2);

        assertSame(criterion2, result);
    }

    /** Test logical AND operation with ALWAYS_FALSE */
    @Test
    public void testJoinAlwaysFalseAnd() {
        var criterion1 = ValueCriterion.FALSE;
        var criterion2 = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        var result = criterion1.join(BinaryOperator.AND, criterion2);

        assertSame(criterion1, result);
    }

    /** Test constant property evaluation */
    @Test
    public void testIsForConstant() {
        var criterion = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        assertFalse(criterion.isConstant());

        var trueCriterion = (ValueCriterion<?>) ValueCriterion.TRUE;
        assertTrue(trueCriterion.isConstant());
    }

    /** Test case insensitivity property evaluation */
    @Test
    public void testIsInsensitive() {
        var criterion1 = new ValueCriterion<>(MetaEmployee.name, Operator.EQUALS_CASE_INSENSITIVE, TEST_NAME);
        assertTrue(criterion1.isInsensitive());

        var criterion2 = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        assertFalse(criterion2.isInsensitive());
    }

    /** Test domain extraction from key */
    @Test
    public void testGetDomain() {
        var criterion = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        var result = criterion.getDomain();

        assertEquals(Employee.class, result);
    }

    /** Test toString representation for standard criterion */
    @Test
    public void testToString() {
        var criterion = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        assertEquals("Employee(name EQ \"John\")", criterion.toString());
    }

    /** Test toString representation for constants */
    @Test
    public void testToStringConstants() {
        assertEquals("Objects(true)", ValueCriterion.TRUE.toString());
        assertEquals("Objects(false)", ValueCriterion.FALSE.toString());
    }

    /** Test toString representation for Custom SQL */
    @Test
    public void testToStringCustomSql() {
        var template = new TemplateValue<>("column = {1}", Array.of(10));
        var criterion = new ValueCriterion<>(null, Operator.CUSTOM_SQL, template);
        assertEquals("Objects(column = {1} [10])", criterion.toString());
    }

    /** Test the freeze method */
    @Test
    public void testFreeze() {
        var criterion = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        assertSame(criterion, criterion.freeze());
    }

    /** Test cloning via protected constructor */
    @Test
    public void testCloning() {
        var criterion = new ValueCriterion<>(MetaEmployee.name, Operator.EQ, TEST_NAME);
        var clone = new ValueCriterion<>(criterion);

        assertEquals(criterion.getLeftNode(), clone.getLeftNode());
        assertEquals(criterion.getOperator(), clone.getOperator());
        assertEquals(criterion.getRightNode(), clone.getRightNode());
    }

    /** Test protected compare method */
    @Test
    public void testCompare() {
        var criterion = new ValueCriterion<>(true);
        assertEquals(0, criterion.compare(10, 10));
        assertEquals(1, criterion.compare(null, 10));
        assertEquals(-1, criterion.compare(10, null));
        assertTrue(criterion.compare(5, 10) < 0);
    }

    /** Helper method to create array of objects */
    public static Array<String> createDummyArray() {
        return Array.of("A", "B");
    }
}