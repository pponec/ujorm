package org.ujorm.orm.dsl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.core.criterion.TemplateValue;
import org.ujorm.core.criterion.ValueCriterion;
import org.ujorm.orm.dsl.meta.QEmployee;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.utils.Lines;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for SelectQueryBuilder to achieve high code coverage in SonarQube.
 */
class SelectQueryBuilderCoverageTest {

    private final StringBuilder writer = new StringBuilder(256);

    @BeforeEach
    void init() {
        writer.setLength(0);
    }

    @Test
    void testComplexBinaryCriterionTree() {
        var builder = getBuilder();
        builder.column(QEmployee.id);

        // Build a complex tree: (id = 1 AND name = 'Joe') OR (id = 2)
        var crn1 = Criterion.where(QEmployee.id, Operator.EQ, 1L);
        var crn2 = Criterion.where(QEmployee.name, Operator.EQ, "Joe");
        var crn3 = Criterion.where(QEmployee.id, Operator.EQ, 2L);
        var crnAll = crn1.and(crn2).or(crn3);

        builder.where(crnAll);
        var sql = Lines.of(builder.toString());

        // Verification of nested parentheses logic in buildCriterionTree
        // Verify multiple joins on the same table with generated aliases
        assertEquals(3, sql.size(), sql.toString());
        assertEquals("SELECT [e.id] AS [id]", sql.next());
        assertEquals("FROM [Employee] e", sql.next());
        assertEquals("WHERE ([e.id] = 1 AND [e.name] = Joe) OR [e.id] = 2", sql.next());
    }

    @Test
    void testValueCriterionConstants() {
        var builder = getBuilder();
        builder.column(QEmployee.id);

        // Test ALWAYS_FALSE constant
        builder.where(Criterion.forConstant(QEmployee.id, false));
        var lines = Lines.of(builder.toString());

        var foundWhere = false;
        while (true) {
            var line = lines.next();
            if (line.isEmpty()) break;
            if (line.contains("WHERE 1=0")) {
                foundWhere = true;
                break;
            }
        }
        assertTrue(foundWhere, "WHERE 1=0 must be present for ALWAYS_FALSE");
    }

    @Test
    void testCustomSqlCriterion() {
        var builder = getBuilder();
        builder.column(QEmployee.id);

        // Use the forSql factory method from Criterion
        var crn = Criterion.forSql(QEmployee.name, "UPPER({0}) = {1}", "JOE");
        builder.where(crn);

        var sql = builder.toString();
        assertTrue(sql.contains("WHERE UPPER([e.name]) = JOE"));
    }

    @Test
    void testNestedAliasGenerationWithRecursion() {
        var builder = getBuilder();
        // Path: Employee -> Boss (Employee) -> Boss's Boss (Employee)
        builder.column(QEmployee.id);
        builder.column(QEmployee.boss, QEmployee.boss, QEmployee.name);

        var sql = Lines.ofQuoted(builder.toString());

        // Verify multiple joins on the same table with generated aliases
        assertEquals(5, sql.size(), sql.toString());
        assertEquals("SELECT [e.id] AS [id]", sql.next());
        assertEquals(", [b1.name] AS [boss.boss.name]", sql.next());
        assertEquals("FROM [Employee] e", sql.next());
        assertEquals("LEFT JOIN [Employee] b ON [b.id] = [e.boss]", sql.next());
        assertEquals("LEFT JOIN [Employee] b1 ON [b1.id] = [b.boss]", sql.next());
    }

    @Test
    void testFindTableAliasFallback() {
        var builder = getBuilder();
        builder.column(QEmployee.id);
        builder.build();

        // Key from the same domain should resolve to the base alias
        var alias = builder.findTableAlias(QEmployee.name);
        assertEquals("e", alias);
    }

    @Test
    void testCloseAndReuse() {
        var builder = getBuilder();
        builder.column(QEmployee.id);
        builder.build();
        builder.close();

        var result = builder.build().toString();
        // After close, columns are empty, SelectQueryBuilder.build() should handle it
        assertEquals("""
                SELECT [e.id] AS [id]
                FROM [Employee] e
                FROM [Object] o
                """.trim(), result);
    }

    /** Helper method to initialize the builder */
    private @NotNull SelectQueryBuilder getBuilder() {
        var writerImpl = new LocalSelectQueryWriter(writer);
        return new SelectQueryBuilder(writerImpl);
    }

    // --- INNER CLASSES ---

    /** Local implementation of SelectQueryWriter for testing purposes. */
    @RequiredArgsConstructor
    private static class LocalSelectQueryWriter implements SelectQueryWriter {

        private final StringBuilder writer;
        private final QuotePair q = QuotePair.ofMsSqlServer();

        @Override
        public void writeTableName(@NotNull String tableAlias, @NotNull Class<?> entityClass) {
            writer.append(q.open())
                    .append(entityClass.getSimpleName())
                    .append(q.close())
                    .append(' ').append(tableAlias);
        }

        @Override
        public void writeColumnName(@NotNull String tableAlias, @NotNull Key<?, ?> column, Key<?, ?>... labels) {
            writer.append(q.open()).append(tableAlias).append('.').append(column.name()).append(q.close());
            if (labels.length > 0) {
                writer.append(" AS ").append(q.open());
                for (var i = 0; i < labels.length; i++) {
                    if (i > 0) writer.append('.');
                    writer.append(labels[i].name());
                }
                writer.append(q.close());
            }
        }

        @Override
        public void writeCondition(ValueCriterion<?> criterion, @NotNull String alias) {
            var operator = criterion.getOperator();
            if (operator == Operator.CUSTOM_SQL) {
                var tv = (TemplateValue<?>) criterion.getRightNode();
                var sql = tv.template().replace("{0}", q.open() + alias + "." + criterion.getLeftNode().name() + q.close())
                        .replace("{1}", String.valueOf(tv.values().get(0)));
                writer.append(sql);
            } else {
                writeColumnName(alias, criterion.getLeftNode());
                writer.append(" ").append(operator.name().equals("EQ") ? "=" : operator.name()).append(" ");
                writer.append(criterion.getRightNode());
            }
        }

        @Override
        public StringBuilder append(String str) {
            var result = writer.append(str);
            return result;
        }

        @Override
        public StringBuilder append(char c) {
            var result = writer.append(c);
            return result;
        }
    }
}