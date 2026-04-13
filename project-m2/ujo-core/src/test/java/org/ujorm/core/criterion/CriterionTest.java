package org.ujorm.core.criterion;

import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.domains.Employee;
import org.ujorm.core.criterion.domains.MetaEmployee;
import org.ujorm.tools.common.Array;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for Criterion and BinaryCriterion */
public class CriterionTest {

    /** Test basic factory methods */
    @Test
    public void testFactoryMethods() {
        var crn = Criterion.where(MetaEmployee.name, Operator.EQ, "John");
        assertEquals("Employee(name EQ \"John\")", crn.toString());

        var crnEq = Criterion.whereEq(MetaEmployee.name, "John");
        assertEquals(crn, crnEq);
    }

    /** Test IN operator factory with various array sizes */
    @Test
    public void testWhereIn() {
        // Empty array -> ALWAYS_FALSE (for positive)
        var empty = Criterion.whereIn(true, MetaEmployee.name, Array.of());
        assertEquals("Employee(false)", empty.toString());

        // Single element -> EQ
        var single = Criterion.whereIn(true, MetaEmployee.name, Array.of("A"));
        assertEquals("Employee(name EQ \"A\")", single.toString());

        // Multiple elements -> IN
        var multiple = Criterion.whereIn(true, MetaEmployee.name, Array.of("A", "B"));
        assertEquals("Employee(name IN [A, B])", multiple.toString());
    }

    /** Test SQL factory */
    @Test
    public void testForSql() {
        var crn = Criterion.forSql(MetaEmployee.id, "{0} > 10");
        assertEquals("Employee({0} > 10 [])", crn.toString());
    }

    /** Test logical optimization for ALWAYS_TRUE and ALWAYS_FALSE */
    @Test
    public void testLogicalOptimizations() {
        var base = Criterion.whereEq(MetaEmployee.name, "John");
        var trueCrn = Criterion.forAll(MetaEmployee.id);
        var falseCrn = Criterion.forNone(MetaEmployee.id);

        // AND optimization
        assertSame(base, base.and(trueCrn));
        assertSame(falseCrn, base.and(falseCrn));

        // OR optimization
        assertSame(trueCrn, base.or(trueCrn));
        assertSame(base, base.or(falseCrn));
    }

    /** Test exhaustive combinations of logical optimizations for ALWAYS_TRUE and ALWAYS_FALSE */
    @Test
    public void testLogicalOptimizationsExhaustive() {
        var nameCrn = Criterion.whereEq(MetaEmployee.name, "A");
        var trueCrn = Criterion.forAll(MetaEmployee.id);
        var falseCrn = Criterion.forNone(MetaEmployee.id);
        var trueCrn2 = Criterion.forAll(MetaEmployee.city);

        // --- OR Operator Optimization ---

        // Left constant
        assertSame(trueCrn, trueCrn.or(nameCrn));
        assertSame(nameCrn, falseCrn.or(nameCrn));

        // Right constant
        assertSame(trueCrn, nameCrn.or(trueCrn));
        assertSame(nameCrn, nameCrn.or(falseCrn));

        // Both constants
        assertSame(trueCrn, trueCrn.or(falseCrn));   // TRUE OR FALSE -> TRUE
        assertSame(trueCrn, trueCrn.or(trueCrn));    // TRUE OR TRUE -> TRUE
        assertSame(trueCrn, trueCrn.or(trueCrn2));   // Return the first condition

        assertSame(trueCrn, falseCrn.or(trueCrn));   // FALSE OR TRUE -> TRUE
        assertSame(falseCrn, falseCrn.or(falseCrn)); // FALSE OR FALSE -> FALSE

        // --- AND Operator Optimization ---

        // Left constant
        assertSame(nameCrn, trueCrn.and(nameCrn));
        assertSame(falseCrn, falseCrn.and(nameCrn));

        // Right constant
        assertSame(nameCrn, nameCrn.and(trueCrn));
        assertSame(falseCrn, nameCrn.and(falseCrn));

        // Both constants
        assertSame(falseCrn, trueCrn.and(falseCrn)); // TRUE AND FALSE -> FALSE
        assertSame(falseCrn, falseCrn.and(trueCrn)); // FALSE AND TRUE -> FALSE
        assertSame(trueCrn, trueCrn.and(trueCrn));   // TRUE AND TRUE -> TRUE
        assertSame(falseCrn, falseCrn.and(falseCrn));// FALSE AND FALSE -> FALSE

        // --- Fallback behavior (e.g. NOT operator or when no logic optimization applies) ---

        // Fallback for constants with unsupported optimization operator
        var notJoined = trueCrn.join(BinaryOperator.NOT, trueCrn);
        assertTrue(notJoined.isBinary());

        // Fallback for normal criteria
        var normalJoin = nameCrn.and(Criterion.whereEq(MetaEmployee.name, "B"));
        assertTrue(normalJoin.isBinary());
    }

    /** Test NOT operator */
    @Test
    public void testNotOperator() {
        var base = Criterion.whereEq(MetaEmployee.name, "John");
        var not = base.not();

        assertTrue(not.isBinary());
        assertEquals(BinaryOperator.NOT, not.getOperator());
        assertEquals("Employee(NOT (name EQ \"John\"))", not.toString());
    }

    /** Test BinaryCriterion toString with nested conditions and parentheses */
    @Test
    public void testBinaryToString() {
        var a = Criterion.whereEq(MetaEmployee.name, "A");
        var b = Criterion.whereEq(MetaEmployee.name, "B");
        var c = Criterion.whereEq(MetaEmployee.name, "C");

        // AND: Domain + crn1 + AND + crn2
        var andCrn = a.and(b);
        assertEquals("Employee(name EQ \"A\") AND (name EQ \"B\")", andCrn.toString());

        // OR: Domain + (crn1 OR crn2)
        var orCrn = a.or(b);
        assertEquals("Employee((name EQ \"A\") OR (name EQ \"B\"))", orCrn.toString());

        // Complex: (A OR B) AND C
        var complex = a.or(b).and(c);
        assertEquals("Employee((name EQ \"A\") OR (name EQ \"B\")) AND (name EQ \"C\")", complex.toString());
    }

    /** Test domain merging in BinaryCriterion */
    @Test
    public void testBinaryDomain() {
        var a = Criterion.whereEq(MetaEmployee.name, "A");
        var b = Criterion.whereEq(MetaEmployee.id, 1L);
        var binary = a.and(b);

        assertEquals(Employee.class, binary.getDomain());
    }

    /** Test equals and hashCode */
    @Test
    public void testEqualsAndHashCode() {
        var c1 = Criterion.whereEq(MetaEmployee.name, "A").and(Criterion.whereEq(MetaEmployee.id, 1L));
        var c2 = Criterion.whereEq(MetaEmployee.name, "A").and(Criterion.whereEq(MetaEmployee.id, 1L));
        var c3 = Criterion.whereEq(MetaEmployee.name, "A").or(Criterion.whereEq(MetaEmployee.id, 1L));

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }
}