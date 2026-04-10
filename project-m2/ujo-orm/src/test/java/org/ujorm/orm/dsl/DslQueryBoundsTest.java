package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.orm.dsl.meta.QCity;
import org.ujorm.orm.dsl.meta.QEmployee;
import org.ujorm.orm.tutorial.domains.*;
import org.ujorm.orm.utils.Lines;
import static org.junit.jupiter.api.Assertions.*;

class DslQueryBoundsTest {

    private final StringBuilder writer = new StringBuilder(256);

    @BeforeEach
    public void init() {
        writer.setLength(0);
    }

    @Test
    void testEmptyColumnsList() {
        var builder = createBuilder("SELECT *");
        var sql = Lines.of(builder.toString());

        assertEquals(2, sql.size(), () -> builder.toString());
        assertEquals("SELECT *", sql.next());
        assertEquals("FROM [Object] o", sql.next());
    }

    @Test
    void testDomainAliasResolutionInWhereClause() {
        var builder = createBuilder();
        builder.column(QEmployee.id);
        builder.column(QEmployee.city, QCity.name);

        // The criterion is placed on QCity, but the user doesn't pass an explicitly aliased key.
        // DslBuilder should dynamically figure out that QCity matches the joined table alias.
        var crn = Criterion.where(QCity.name, Operator.EQ, "Prague");
        builder.where(crn);

        var sql = Lines.of(builder.toString());

        assertEquals(5, sql.size(), () -> builder.toString());
        assertEquals("SELECT [e.id] AS [id]", sql.next());
        assertEquals(", [c.name] AS [city.name]", sql.next());
        assertEquals("FROM [Employee] e", sql.next());
        assertEquals("JOIN [City] c ON [c.id] = [e.city]", sql.next());
        assertEquals("WHERE [c.name] = 'Prague'", sql.next());
    }

    @Test
    void testNullValueHandling() {
        var builder = createBuilder();
        builder.column(QEmployee.id);

        var crn = Criterion.whereEq(QEmployee.name, null);
        builder.where(crn);

        var sql = Lines.of(builder.toString());

        assertEquals(3, sql.size(), () -> builder.toString());
        assertEquals("SELECT [e.id] AS [id]", sql.next());
        assertEquals("FROM [Employee] e", sql.next());
        assertEquals("WHERE [e.name] = NULL", sql.next()); // Value will be relaced by the placeholder.
    }

    @Test
    void testConstantCriterionForAll() {
        var builder = createBuilder();
        builder.column(QEmployee.id);

        // Simulating 1=1 or always true
        builder.where(Criterion.forAll());

        var sql = Lines.of(builder.toString());

        assertEquals(2, sql.size(), () -> builder.toString());
        assertEquals("SELECT [e.id] AS [id]", sql.next());
        assertEquals("FROM [Employee] e", sql.next());
    }

    @Test
    void testConstantCriterionForNone() {
        var builder = createBuilder();
        builder.column(QEmployee.id);

        // Simulating 1=0 or always false
        builder.where(Criterion.forNone());

        var sql = Lines.of(builder.toString());

        assertEquals(3, sql.size(), () -> builder.toString());
        assertEquals("SELECT [e.id] AS [id]", sql.next());
        assertEquals("FROM [Employee] e", sql.next());
        assertEquals("WHERE 1=0", sql.next());
    }

    private @NotNull DslQueryBuilder createBuilder(String ...sql) {
        var dslWriter = new DslQueryBuilderTest.DslQueryWriterTestImpl(writer);
        dslWriter.append(String.join(" ", sql));
        return new DslQueryBuilder(dslWriter);
    }
}