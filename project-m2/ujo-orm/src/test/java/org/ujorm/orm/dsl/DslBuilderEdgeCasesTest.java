package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.dsl.meta.MetaEmployee;

class DslBuilderEdgeCasesTest {

    @Test
    void testEmptyColumnsList() {
        var builder = new DslBuilder();
        builder.build();
        var sql = builder.toString();

        Assertions.assertEquals("", sql, "Empty builder should produce empty output without failing");
    }

    @Test
    void testDomainAliasResolutionInWhereClause() {
        var builder = new DslBuilder();
        builder.column(MetaEmployee.id);
        builder.column(MetaEmployee.city, MetaCity.name);

        // The criterion is placed on MetaCity, but the user doesn't pass an explicitly aliased key.
        // DslBuilder should dynamically figure out that MetaCity matches the joined table alias.
        var crn = Criterion.where(MetaCity.name, Operator.EQ, "Prague");
        builder.setCriterion(crn);

        builder.build();
        var sql = builder.toString();

        Assertions.assertTrue(sql.contains("WHERE c.name EQ 'Prague'"), "Builder failed to resolve join table alias dynamically");
    }

    @Test
    void testNullValueHandling() {
        var builder = new DslBuilder();
        builder.column(MetaEmployee.id);

        var crn = Criterion.whereNull(MetaEmployee.name); // Usually maps to Operator.EQ and null value
        builder.setCriterion(crn);

        builder.build();
        var sql = builder.toString();

        Assertions.assertTrue(sql.contains("WHERE e.name EQ NULL"), "Null value was not safely formatted");
    }

    @Test
    void testConstantCriterionForAll() {
        var builder = new DslBuilder();
        builder.column(MetaEmployee.id);

        // Simulating 1=1 or always true
        builder.setCriterion(Criterion.forAll());

        builder.build();
        var sql = builder.toString();

        Assertions.assertFalse(sql.contains("WHERE"), "Globally true criterion should skip generating WHERE clause");
    }

    @Test
    void testConstantCriterionForNone() {
        var builder = new DslBuilder();
        builder.column(MetaEmployee.id);

        // Simulating 1=0 or always false
        builder.setCriterion(Criterion.forNone());

        builder.build();
        var sql = builder.toString();

        Assertions.assertTrue(sql.contains("WHERE 1=0"), "Globally false criterion should generate a safe fail block");
    }
}