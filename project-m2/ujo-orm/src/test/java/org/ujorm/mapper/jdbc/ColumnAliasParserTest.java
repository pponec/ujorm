package org.ujorm.mapper.jdbc;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ColumnAliasParserTest {

    private final ColumnAliasParser parser = new ColumnAliasParser();

    @Test
    void testStandardAliases() {
        var sql = """
            SELECT e.id AS 'id'
                 , p.id AS 'parent.id'
                 , s.id AS 'parent.parent.id'
            FROM employee e
            """;
        var expected = List.of("id", "parent.id", "parent.parent.id");
        assertEquals(expected, parser.parseSql(sql));
    }

    @Test
    void testDifferentQuoteTypes() {
        var sql = """
            SELECT a.name AS "double_quotes",
                   b.name AS `backticks`,
                   c.name AS [brackets],
                   d.name AS simple_alias
            FROM table_a a
            """;
        var expected = List.of("double_quotes", "backticks", "brackets", "simple_alias");
        assertEquals(expected, parser.parseSql(sql));
    }

    @Test
    void testMixedCaseAndSpacing() {
        var sql = "SELECT col1 as 'alias1', col2  AS   \"alias2\" FROM table";
        var expected = List.of("alias1", "alias2");
        assertEquals(expected, parser.parseSql(sql));
    }

    @Test
    void testIgnoreTableAliases() {
        // Aliasy tabulek (e, p, s) nesmí být v seznamu, protože jsou za FROM/JOIN
        var sql = """
            SELECT e.name AS 'emp_name'
            FROM employee AS e
            JOIN department AS d ON e.dept_id = d.id
            """;
        var expected = List.of("emp_name");
        assertEquals(expected, parser.parseSql(sql));
    }

    @Test
    void testEmptyResult() {
        var sql = "SELECT * FROM employee";
        assertTrue(parser.parseSql(sql).isEmpty());
    }

    @Test
    void testNoAsKeyword() {
        var sql = "SELECT name 'alias' FROM employee";
        assertTrue(parser.parseSql(sql).isEmpty(), "Should only find aliases with AS keyword");
    }
}