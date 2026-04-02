package org.ujorm.core.criterion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the abstract Criterion class */
class CriterionTest {

    private static final Object DUMMY_LEFT_NODE = "leftNode";

    /** Test the logical AND operator joining */
    @Test
    void testAndOperator() {
        var left = new DummyCriterion(1);
        var right = new DummyCriterion(2);

        var result = left.and(right);

        assertNotNull(result);
        assertTrue(result instanceof BinaryCriterion);
    }

    /** Test the logical OR operator joining */
    @Test
    void testOrOperator() {
        var left = new DummyCriterion(1);
        var right = new DummyCriterion(2);

        var result = left.or(right);

        assertNotNull(result);
        assertTrue(result instanceof BinaryCriterion);
    }

    /** Test equals and hashCode contract */
    @Test
    void testEqualsAndHashCode() {
        var crit1 = new DummyCriterion(1);
        var crit2 = new DummyCriterion(1);
        var crit3 = new DummyCriterion(2);

        assertEquals(crit1, crit2);
        assertEquals(crit1.hashCode(), crit2.hashCode());
        assertNotEquals(crit1, crit3);
    }

    /** A simple dummy implementation for testing the abstract class */
    static class DummyCriterion extends Criterion {

        private final int id;

        /** Constructor */
        DummyCriterion(int id) {
            this.id = id;
        }

        @NotNull
        @Override
        public Object getLeftNode() {
            return DUMMY_LEFT_NODE;
        }

        @Nullable
        @Override
        public Object getRightNode() {
            return id;
        }

        @NotNull
        @Override
        public AbstractOperator getOperator() {
            return Operator.EQ;
        }

        @NotNull
        @Override
        public Class<?> getDomain() {
            return String.class;
        }
    }
}