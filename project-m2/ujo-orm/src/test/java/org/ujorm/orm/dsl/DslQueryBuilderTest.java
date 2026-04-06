package org.ujorm.orm.dsl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.orm.dsl.meta.MetaEmployee;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.tutorial.domains.MetaCity;

class DslQueryBuilderTest {

    private final StringBuilder writer = new StringBuilder(256);

    @BeforeEach
    public void init() {
        writer.setLength(0);
    }

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
        Assertions.assertEquals(8, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]"     , sql[i++]);
        Assertions.assertEquals(", [e.name] AS [name]"      , sql[i++]);
        Assertions.assertEquals(", [c.name] AS [city.name]" , sql[i++]);
        Assertions.assertEquals(", [b.name] AS [boss.name]" , sql[i++]);
        Assertions.assertEquals("FROM [Employee] e"         , sql[i++]);
        Assertions.assertEquals("INNER JOIN [City] c ON [c.id] = [e.city]", sql[i++]);
        Assertions.assertEquals("OUTER JOIN [Employee] b ON [b.id] = [e.boss]", sql[i++]);
        Assertions.assertEquals("WHERE [e.name] = 'Joe' AND [c.name] = 'Prague'", sql[i]);
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
        Assertions.assertEquals(8, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]"     , sql[i++]);
        Assertions.assertEquals(", [e.name] AS [name]"      , sql[i++]);
        Assertions.assertEquals(", [c.name] AS [city.name]" , sql[i++]);
        Assertions.assertEquals(", [bb.name] AS [boss.name]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e"         , sql[i++]);
        Assertions.assertEquals("INNER JOIN [City] c ON [c.id] = [e.city]", sql[i++]);
        Assertions.assertEquals("OUTER JOIN [Employee] bb ON [bb.id] = [e.boss]", sql[i++]);
        Assertions.assertEquals("WHERE ([e.id] <= 0 AND [c.id] IN (1, 2)) OR ([c.name] = 'Joe' AND [bb.name] = 'Black')", sql[i]);
    }


    private @NotNull DslQueryBuilder getBuilder() {
        return new DslQueryBuilder(new DslQueryWriterImpl(writer));
    }

    /** */
    @RequiredArgsConstructor
    public static class DslQueryWriterImpl implements DslQueryWriter {

        final StringBuilder writer;
        final QuotePair q = QuotePair.ofMsSqlServer();


        /** Write database table name. */
        @Override
        public void writeTableName(@NotNull String tableAlias, @NotNull Class<?> entityClass) {
            writer.append(q.open())
                    .append(entityClass.getSimpleName())
                    .append(q.close())
                    .append(' ').append(tableAlias);
        }

        /** Write database column name. */
        @Override
        public void writeColumnName(@NotNull String tableAlias, @NotNull Key<?,?> column, Key<?,?>... labels) {
            writer.append(q.open()).append(tableAlias).append('.').append(column.name()).append(q.close());

            var printLabel = labels.length > 0;
            if (printLabel) {
                writer.append(" AS ");
                writer.append(q.open());
                for (var i = 0; i < labels.length; i++) {
                    if (i > 0) writer.append('.');
                    writer.append(labels[i].name());
                }
                writer.append(q.close());
            }
        }

        public StringBuilder append(String str) {
            writer.append(str);
            return writer;
        }

        public StringBuilder append(char str) {
            writer.append(str);
            return writer;
        }

        @Override
        public String toString() {
            return writer.toString();
        }
    }
}