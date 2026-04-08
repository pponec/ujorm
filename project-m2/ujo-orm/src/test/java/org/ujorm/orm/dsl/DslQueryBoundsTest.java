package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.dsl.meta.MetaEmployee;

class DslQueryBoundsTest {

    private final StringBuilder writer = new StringBuilder(256);

    @BeforeEach
    public void init() {
        writer.setLength(0);
    }

    @Test
    void testEmptyColumnsList() {
        var builder = createBuilder("SELECT *");
        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals(2, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT *", sql[i++]);
        Assertions.assertEquals("FROM [Object] o", sql[i++]);
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
        builder.where(crn);

        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals(5, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals(", [c.name] AS [city.name]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e", sql[i++]);
        Assertions.assertEquals("INNER JOIN [City] c ON [c.id] = [e.city]", sql[i++]);
        Assertions.assertEquals("WHERE [c.name] = 'Prague'", sql[i++]);
        Assertions.assertEquals(i, sql.length);
    }

    @Test
    void testNullValueHandling() {
        var builder = createBuilder();
        builder.column(MetaEmployee.id);

        var crn = Criterion.whereNull(MetaEmployee.name); // Usually maps to Operator.EQ and null value
        builder.where(crn);

        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals(3, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e", sql[i++]);
        Assertions.assertEquals("WHERE [e.name] = NULL", sql[i++]);
        Assertions.assertEquals(i, sql.length);
    }

    @Test
    void testConstantCriterionForAll() {
        var builder = createBuilder();
        builder.column(MetaEmployee.id);

        // Simulating 1=1 or always true
        builder.where(Criterion.forAll());

        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals(2, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e", sql[i++]);
        Assertions.assertEquals(i, sql.length, "Globally true criterion should skip generating WHERE clause");
    }

    @Test
    void testConstantCriterionForNone() {
        var builder = createBuilder();
        builder.column(MetaEmployee.id);

        // Simulating 1=0 or always false
        builder.where(Criterion.forNone());

        var sql = builder.toString().lines().toArray(String[]::new);

        var i = 0;
        Assertions.assertEquals(3, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e", sql[i++]);
        Assertions.assertEquals("WHERE 1=0", sql[i++]);
        Assertions.assertEquals(i, sql.length, "Globally false criterion should generate a safe fail block");
    }

    private @NotNull DslQueryBuilder createBuilder(String ...sql) {
        var dslWriter = new DslQueryBuilderTest.DslQueryWriterTestImpl(writer);
        dslWriter.append(String.join(" ", sql));
        return new DslQueryBuilder(dslWriter);
    }
}