package org.ujorm.core.criterion;

import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.domains.MetaEmployee;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for FunctionCriterion
 * @author Pavel Ponec
 */
public class FunctionCriterionTest {

    @Test
    void testBasicProperties() {
        Supplier<String> supplier = () -> "John";
        var crn = new FunctionCriterion<>(MetaEmployee.name, Operator.EQ, supplier);

        assertEquals(MetaEmployee.name, crn.getLeftNode());
        assertEquals(Operator.EQ, crn.getOperator());
        assertEquals("John", crn.getRightNode());
    }

    @Test
    void testUnsupportedOperators() {
        Supplier<String> supplier = () -> "John";

        assertThrows(IllegalArgumentException.class, () -> new FunctionCriterion<>(MetaEmployee.name, Operator.ALWAYS_TRUE, supplier));
        assertThrows(IllegalArgumentException.class, () -> new FunctionCriterion<>(MetaEmployee.name, Operator.ALWAYS_FALSE, supplier));
    }

    @Test
    void testCharSequenceValidationWithSupplier() {
        Supplier<String> supplier = () -> "Jo";

        // The Operator.STARTS triggers makeCharSequenceTest in the ValueCriterion super constructor.
        // FunctionCriterion overrides this to allow Supplier instances.
        var crn = new FunctionCriterion<>(MetaEmployee.name, Operator.STARTS, supplier);

        assertEquals(Operator.STARTS, crn.getOperator());
        assertEquals("Jo", crn.getRightNode());
    }

    @Test
    void testNullSupplierValidation() {
        assertThrows(RuntimeException.class, () -> new FunctionCriterion<>(MetaEmployee.name, Operator.EQ, null));
    }

    @Test
    void testFreeze() {
        Supplier<String> supplier = () -> "John";
        var crn = new FunctionCriterion<>(MetaEmployee.name, Operator.EQ, supplier);

        var frozen = crn.freeze();

        assertNotNull(frozen);
        assertNotSame(crn, frozen); // Should be a new instance
        assertEquals(crn.getLeftNode(), frozen.getLeftNode());
        assertEquals(crn.getOperator(), frozen.getOperator());
        assertEquals("John", frozen.getRightNode()); // The value is evaluated during cloning
    }
}