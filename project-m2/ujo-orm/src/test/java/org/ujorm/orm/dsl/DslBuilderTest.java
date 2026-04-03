package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.orm.dsl.meta.MetaEmployee;
import org.ujorm.orm.tutorial.domains.MetaCity;

class DslBuilderTest {

    @Test
    void testBasicSelectWithJoinsAndWhere() {
        var builder = new DslBuilder();

        // 1. Column definition (generates base alias 'e' and joins 'c' for city, 'b' for boss)
        builder.column(MetaEmployee.id);
        builder.column(MetaEmployee.name);
        builder.column(MetaEmployee.city, MetaCity.name);
        builder.column(MetaEmployee.boss, MetaEmployee.name);

        // 2. Criteria creation
        var crn1 = Criterion.where(MetaEmployee.name, Operator.EQ, "Joe");
        var crn2 = Criterion.where(MetaCity.name, Operator.EQ, "Prague");
        var crnAll = crn1.and(crn2);

        builder.setCriterion(crnAll);

        // 3. Execution
        builder.build();
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        Assertions.assertEquals("SELECT e.id, e.name, c.name, b.name", sql[0]);
        Assertions.assertEquals("FROM Employee e", sql[1]);
        Assertions.assertEquals("INNER JOIN City c ON c.id = e.city", sql[2]);
        Assertions.assertEquals("LEFT OUTER JOIN Employee b ON b.id = e.boss", sql[3]);
        Assertions.assertEquals("WHERE (e.name EQ 'Joe' AND c.name EQ 'Prague')", sql[4]);
    }
}