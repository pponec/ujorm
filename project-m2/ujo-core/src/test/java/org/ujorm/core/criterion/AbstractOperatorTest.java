package org.ujorm.core.criterion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractOperatorTest {

    @Test
    void isConstant() {
        Assertions.assertTrue(Operator.ALWAYS_TRUE.isConstant());
        Assertions.assertTrue(Operator.ALWAYS_FALSE.isConstant());
        //
        Assertions.assertFalse(Operator.EQ.isConstant());
        Assertions.assertFalse(Operator.GE.isConstant());
        Assertions.assertFalse(BinaryOperator.AND.isConstant());
        Assertions.assertFalse(BinaryOperator.OR.isConstant());
    }
}