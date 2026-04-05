package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.dsl.meta.MetaEmployee;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.tutorial.domains.MetaCity;

class DslQueryBuilderTest {

    @Test
    void testBasicSelectWithJoinsAndWhere() {
        var builder = getBuilder();

        // 1. Column definition (generates base alias 'e' and joins 'c' for city, 'b' for boss)
        builder.column(MetaEmployee.id);
        builder.column(MetaEmployee.name);
        builder.column(MetaEmployee.city, MetaCity.name);
        builder.column(MetaEmployee.boss, MetaEmployee.name);

        // 2. Criteria creation
        var crn1 = MetaEmployee.name.whereEq("Joe");
        var crn2 = MetaCity.name.whereEq("Prague");
        var crnAll = crn1.and(crn2);

        builder.where(crnAll);

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals("SELECT [e.id] AS [id]"   , sql[i++]);
        Assertions.assertEquals(", [e.name] AS [name]"    , sql[i++]);
        Assertions.assertEquals(", [c.name] AS [city.name]" , sql[i++]);
        Assertions.assertEquals(", [b.name] AS [boss.name]" , sql[i++]);
        Assertions.assertEquals("FROM Employee e"         , sql[i++]);
        Assertions.assertEquals("INNER JOIN City c ON [c.id] = [e.city]", sql[i++]);
        Assertions.assertEquals("LEFT OUTER JOIN Employee b ON [b.id] = [e.boss]", sql[i++]);
        Assertions.assertEquals("WHERE [e.name] EQ 'Joe' AND [c.name] EQ 'Prague'", sql[i++]);
    }

    @Test
    void testBasicSelectWithJoinsAndWhereExt() {
        var builder = getBuilder();
        var metaBossName = MetaEmployee.as("bb", MetaEmployee.name);

        // 1. Column definition (generates base alias 'e' and joins 'c' for city, 'bb' for boss)
        builder.column(MetaEmployee.id);
        builder.column(MetaEmployee.name);
        builder.column(MetaEmployee.city, MetaCity.name);
        builder.column(MetaEmployee.boss, metaBossName);

        // 2. Criteria creation
        var crn1 = MetaEmployee.id.whereLe(0L);
        var crn2 = MetaCity.id.whereIn(1L, 2L);
        var crn3 = MetaCity.name.whereEq("Joe");
        var crn4 = metaBossName.whereEq("Black");
        var crnAll = crn1.and(crn2).or(crn3.and(crn4));

        builder.where(crnAll);

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals("SELECT [e.id] AS [id]"   , sql[i++]);
        Assertions.assertEquals(", [e.name] AS [name]"    , sql[i++]);
        Assertions.assertEquals(", [c.name] AS [city.name]" , sql[i++]);
        Assertions.assertEquals(", [bb.name] AS [boss.name]", sql[i++]);
        Assertions.assertEquals("FROM Employee e"         , sql[i++]);
        Assertions.assertEquals("INNER JOIN City c ON [c.id] = [e.city]", sql[i++]);
        Assertions.assertEquals("LEFT OUTER JOIN Employee bb ON [bb.id] = [e.boss]", sql[i++]);
        Assertions.assertEquals("WHERE ([e.id] LE 0 AND [c.id] IN (1, 2)) OR ([c.name] EQ 'Joe' AND [bb.name] EQ 'Black')", sql[i++]);
    }


    private static @NotNull DslQueryBuilder getBuilder() {
        return new DslQueryBuilder(QuotePair.ofMsSqlServer());
    }
}