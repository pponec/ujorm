package org.ujorm.core.criterion;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.core.criterion.domains.MetaEmployee;
import org.ujorm.tools.common.Array;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for default methods of the CriterionProvider interface.
 */
public class CriterionProviderTest {

    private CriterionProvider<Object, String> nameProvider;

    @BeforeEach
    void setUp() {
        // Create a provider instance for the 'name' key
        nameProvider = new CriterionProvider<>() {
            @NotNull
            @Override
            public Key<Object, String> self() {
                return (Key) MetaEmployee.name;
            }
        };
    }

    /** Test basic comparison methods */
    @Test
    public void testBasicComparisons() {
        assertEquals("Employee(name EQ \"John\")", nameProvider.whereEq("John").toString());
        assertEquals("Employee(name NOT_EQ \"John\")", nameProvider.whereNeq("John").toString());
        assertEquals("Employee(name GT \"A\")", nameProvider.whereGt("A").toString());
        assertEquals("Employee(name GE \"A\")", nameProvider.whereGe("A").toString());
        assertEquals("Employee(name LT \"Z\")", nameProvider.whereLt("Z").toString());
        assertEquals("Employee(name LE \"Z\")", nameProvider.whereLe("Z").toString());
    }

    /** Test nullability checks */
    @Test
    public void testNullability() {
        assertEquals("Employee(name EQ null)", nameProvider.whereNull().toString());
        assertEquals("Employee(name NOT_EQ null)", nameProvider.whereNotNull().toString());
    }

    /** Test constant criterions */
    @Test
    public void testConstants() {
        assertEquals("Employee(true)", nameProvider.whereTrue().toString());
        assertEquals("Employee(false)", nameProvider.whereFalse().toString());
    }

    /** Test IN and NOT_IN operators with varargs */
    @Test
    public void testInOperators() {
        assertEquals("Employee(name IN [A, B])", nameProvider.whereIn("A", "B").toString());
        assertEquals("Employee(name NOT_IN [A, B])", nameProvider.whereNotIn("A", "B").toString());

        // Test with Array object
        var array = Array.of("X", "Y");
        assertEquals("Employee(name IN [X, Y])", nameProvider.whereIn(array).toString());
        assertEquals("Employee(name NOT_IN [X, Y])", nameProvider.whereNotIn(array).toString());

        // Test with Collection object
        var collection = List.of("C", "D");
        assertEquals("Employee(name IN [C, D])", nameProvider.whereIn(collection).toString());
        assertEquals("Employee(name NOT_IN [C, D])", nameProvider.whereNotIn(collection).toString());
    }

    /** Test custom SQL templates */
    @Test
    public void testSqlMethods() {
        assertEquals("Employee(UPPER({0}) = {1} [JOHN])",
                nameProvider.whereSql("UPPER({0}) = {1}", "JOHN").toString());
    }

    /** Test ProxyValue methods */
    @Test
    public void testProxyValue() {
        ProxyValue<String> proxy = () -> "proxyResult";
        // Note: The toString result depends on how FunctionCriterion handles the proxy.
        // Assuming it formats similarly to a ValueCriterion for this test context.
        var result = nameProvider.whereEq(proxy).toString();
        assertEquals("Employee(name EQ \"proxyResult\")", result);
    }

    /** Test relationship with another Key */
    @Test
    public void testWhereKeyRelation() {
        var result = nameProvider.whereEq((Key) MetaEmployee.id).toString();
        // Assuming the printer writes the Key name directly
        assertEquals("Employee(name EQ id)", result);
    }
}