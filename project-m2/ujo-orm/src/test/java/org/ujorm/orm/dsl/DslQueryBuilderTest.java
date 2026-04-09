package org.ujorm.orm.dsl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.TemplateValue;
import org.ujorm.core.criterion.ValueCriterion;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.dsl.meta.*;

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
        builder.column(QEmployee.id);
        builder.column(QEmployee.name);
        builder.column(QEmployee.city, QCity.name);
        builder.column(QEmployee.boss, QEmployee.name);

        // 2. Criteria creation
        var crn1 = QEmployee.name.whereEq("Joe");
        var crn2 = QCity.name.whereEq("Prague");
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
        var metaBossName = QEmployee.as("bb", QEmployee.name);

        // 1. Column definition (generates base alias 'e' and joins 'c' for city, 'bb' for boss)
        builder.column(QEmployee.id);
        builder.column(QEmployee.name);
        builder.column(QEmployee.city, QCity.name);
        builder.column(QEmployee.boss, metaBossName);

        // 2. Criteria creation
        var crn1 = QEmployee.id.whereLe(0L);
        var crn2 = QCity.id.whereIn(1L, 2L);
        var crn3 = QCity.name.whereEq("Joe");
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

    /** Test that the WHERE clause is omitted when the root criterion is ALWAYS_TRUE */
    @Test
    void testStandaloneAlwaysTrue() {
        var builder = getBuilder();
        builder.column(QEmployee.id);
        builder.where(QEmployee.id.whereTrue());

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals(2, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e"    , sql[i++]);
    }

    /** Test that the WHERE clause contains only the constant when the root criterion is ALWAYS_FALSE */
    @Test
    void testStandaloneAlwaysFalse() {
        var builder = getBuilder();
        builder.column(QEmployee.id);
        builder.where(QEmployee.id.whereFalse());

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals(3, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e"    , sql[i++]);
        Assertions.assertEquals("WHERE 1=0"            , sql[i++]);
    }

    /** Test that nested constant criteria are not specially handled and fall back to standard column rendering */
    @Test
    void testNestedConstantRestriction() {
        var builder = getBuilder();
        builder.column(QEmployee.id);

        var crn1 = QEmployee.id.whereGt(100L);
        var crn2 = QEmployee.name.whereTrue();
        var crnAll = crn1.and(crn2);

        builder.where(crnAll);

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals(3, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e"    , sql[i++]);
        Assertions.assertEquals("WHERE [e.id] > 100", sql[i++]);
    }

    /** Test that the default Criterion.forAll() behaves the same as ALWAYS_TRUE and omits the WHERE clause */
    @Test
    void testCriterionForAllOmission() {
        var builder = getBuilder();
        builder.column(QEmployee.id);
        builder.where(Criterion.forAll());

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals(2, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e"    , sql[i++]);
    }

    /** Test that an empty columns list results in a SELECT clause without specific columns */
    @Test
    void testEmptyColumnsList() {
        var builder = getBuilder();
        builder.where(Criterion.forAll());

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals(2, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT"           , sql[i++]);
        Assertions.assertEquals("FROM [Object] o"   , sql[i++]);
    }

    private @NotNull DslQueryBuilder getBuilder() {
        return new DslQueryBuilder(new DslQueryWriterTestImpl(writer));
    }

    /** Test that CUSTOM_SQL template correctly replaces the placeholder with the column name */
    @Test
    void testCustomSqlTemplate() {
        var builder = getBuilder();
        builder.column(QEmployee.id);

        // 2. Criteria creation using a template
        var crn = QEmployee.name.whereSql("UPPER({0}) = {1}", "Joe");
        builder.where(crn);

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals(3, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e"    , sql[i++]);
        Assertions.assertEquals("WHERE UPPER([e.name]) = ['Joe']", sql[i]);
    }

    /** Test that CUSTOM_SQL template correctly replaces multiple placeholders in a single string */
    @Test
    void testCustomSqlMultiTemplate() {
        var builder = getBuilder();
        builder.column(QEmployee.id);

        // 2. Criteria creation with multiple placeholders
        var crn = QEmployee.id.whereSql("{0} IS NOT NULL AND {0} IN ({*})", 3L, 5L);
        builder.where(crn);

        // 3. Execution
        var sql = builder.toString().lines().toArray(String[]::new);

        // 4. Output verification
        var i = 0;
        Assertions.assertEquals(3, sql.length, () -> builder.toString());
        Assertions.assertEquals("SELECT [e.id] AS [id]", sql[i++]);
        Assertions.assertEquals("FROM [Employee] e"    , sql[i++]);
        Assertions.assertEquals("WHERE [e.id] IS NOT NULL AND [e.id] IN ((3, 5))", sql[i]);
    }

    // --- CLASS ---

    /** Query writer implementation for testing */
    @RequiredArgsConstructor
    static class DslQueryWriterTestImpl implements DslQueryWriter {

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

        /** Write the condition to SQL */
        @Override
        public void writeCondition(ValueCriterion<?> criterion, @NotNull String alias) {
            var key = (Key<?, ?>) criterion.getLeftNode();
            var operator = criterion.getOperator();
            var value = criterion.getRightNode();

            switch (operator) {
                case ALWAYS_TRUE,
                     ALWAYS_FALSE -> writer.append(operator.term());
                case CUSTOM_SQL -> {
                    if (value instanceof TemplateValue<?> tv) {
                        appendCustomSql(alias, key, tv);
                    }
                }
                case IN, NOT_IN -> {
                    writeColumnName(alias, key);
                    writer.append(' ').append(operator.term()).append(' ');
                    writeValues(value);
                }
                default -> {
                    writeColumnName(alias, key);
                    writer.append(' ').append(operator.term()).append(' ');
                    writeValue(value);
                }
            }
        }

        /** Format custom SQL templates */
        private void appendCustomSql(String alias, Key<?, ?> key, TemplateValue<?> templateValue) {
            var values = templateValue.valuesNonNull();
            var template = templateValue.template();

            var last = 0;
            for (var start = template.indexOf('{'); start != -1; start = template.indexOf('{', last)) {
                var end = template.indexOf('}', start);
                if (end == -1) break;

                writer.append(template.substring(last, start));
                var mark = template.substring(start + 1, end);
                last = end + 1;

                switch (mark) {
                    case "0" -> writeColumnName(alias, key);
                    case "*" -> writeValues(values);
                    default -> writeArrayAsSquareBrackets(values);
                }
            }
            writer.append(template.substring(last));
        }

        /** Format arrays and collections into square brackets */
        private void writeArrayAsSquareBrackets(Object value) {
            if (value instanceof Iterable<?> list) {
                writer.append('[');
                var first = true;
                for (var item : list) {
                    if (!first) writer.append(", ");
                    writeValue(item);
                    first = false;
                }
                writer.append(']');
            } else if (value instanceof Object[] array) {
                writer.append('[');
                for (var i = 0; i < array.length; i++) {
                    if (i > 0) writer.append(", ");
                    writeValue(array[i]);
                }
                writer.append(']');
            } else {
                writeValue(value);
            }
        }

        /** Format scalar value or arrays with parentheses */
        private void writeValues(@Nullable Object value) {
            if (value instanceof Iterable<?> list) {
                writer.append('(');
                var first = true;
                for (var item : list) {
                    if (!first) writer.append(", ");
                    writeValue(item);
                    first = false;
                }
                writer.append(')');
            } else if (value instanceof Object[] array) {
                writer.append('(');
                for (var i = 0; i < array.length; i++) {
                    if (i > 0) writer.append(", ");
                    writeValue(array[i]);
                }
                writer.append(')');
            } else {
                writer.append('(');
                writeValue(value);
                writer.append(')');
            }
        }

        /** Format single scalar value */
        private void writeValue(@Nullable Object value) {
            if (value == null) {
                writer.append("NULL");
            } else if (value instanceof String str) {
                writer.append("'").append(str).append("'");
            } else {
                writer.append(value);
            }
        }

        @Override
        public StringBuilder append(String str) {
            writer.append(str);
            return writer;
        }

        @Override
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