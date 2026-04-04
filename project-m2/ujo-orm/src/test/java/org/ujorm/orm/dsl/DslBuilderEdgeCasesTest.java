package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.dsl.meta.MetaEmployee;

class DslBuilderEdgeCasesTest {

    @Test
    void testEmptyColumnsList() {
        var builder = createBuilder();
        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals("SELECT ", sql[i++]);
        Assertions.assertEquals("FROM Object o", sql[i++]);
        Assertions.assertEquals(i, sql.length);
    }

    @Test
    void testDomainAliasResolutionInWhereClause() {
        var builder = createBuilder();
        builder.column(MetaEmployee.id);
        builder.column(MetaEmployee.city, MetaCity.name);

        // The criterion is placed on MetaCity, but the user doesn't pass an explicitly aliased key.
        // DslBuilder should dynamically figure out that MetaCity matches the joined table alias.
        var crn = Criterion.where(MetaCity.name, Operator.EQ, "Prague");
        builder.setCriterion(crn);

        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals(", [c.name] AS [city.name]", sql[i++]);
        Assertions.assertEquals("FROM Employee e", sql[i++]);
        Assertions.assertEquals("INNER JOIN City c ON [c.id] = [e.city]", sql[i++]);
        Assertions.assertEquals("WHERE [c.name] EQ 'Prague'", sql[i++]);
        Assertions.assertEquals(i, sql.length);
    }

    @Test
    void testNullValueHandling() {
        var builder = createBuilder();
        builder.column(MetaEmployee.id);

        var crn = Criterion.whereNull(MetaEmployee.name); // Usually maps to Operator.EQ and null value
        builder.setCriterion(crn);

        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM Employee e", sql[i++]);
        Assertions.assertEquals("WHERE [e.name] EQ NULL", sql[i++]);
        Assertions.assertEquals(i, sql.length);
    }

    @Test
    void testConstantCriterionForAll() {
        var builder = createBuilder();
        builder.column(MetaEmployee.id);

        // Simulating 1=1 or always true
        builder.setCriterion(Criterion.forAll());

        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM Employee e", sql[i++]);
        Assertions.assertEquals(i, sql.length, "Globally true criterion should skip generating WHERE clause");
    }

    @Test
    void testConstantCriterionForNone() {
        var builder = createBuilder();
        builder.column(MetaEmployee.id);

        // Simulating 1=0 or always false
        builder.setCriterion(Criterion.forNone());

        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM Employee e", sql[i++]);
        Assertions.assertEquals("WHERE 1=0", sql[i++]);
        Assertions.assertEquals(i, sql.length, "Globally false criterion should generate a safe fail block");
    }


    private static @NotNull DslBuilder createBuilder() {
        return new DslBuilder(QuotePair.ofMsSqlServer());
    }
}