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