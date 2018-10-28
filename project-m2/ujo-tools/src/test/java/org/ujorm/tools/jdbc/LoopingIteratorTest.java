package org.ujorm.tools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.ujorm.tools.jdbc.mock.AbstractPreparedStatement;
import org.ujorm.tools.jdbc.mock.AbstractResultSet;
import org.ujorm.tools.set.LoopingIterator;
import static org.junit.Assert.*;

/**
 *
 * @author Pavel Ponec
 */
public class LoopingIteratorTest {

    /** A date for testing */
    private static final LocalDateTime SOME_DATE = LocalDateTime.of(2018, 10, 28, 8, 4);


    /** Test of iterator method, of class RowIterator. */
    @Test
    public void testMockConnection() throws SQLException, IOException {

        Connection conn = createConnection(createTableValues(3));
        PreparedStatement statement = conn.prepareStatement("");
        ResultSet rs = statement.executeQuery();

        assertTrue(rs.next());
        assertTrue(rs.next());
        assertTrue(rs.next());
        assertTrue(30 == rs.getInt(1));
        assertTrue("C name".equals(rs.getString(2)));
        assertNull(rs.getObject(3, String.class));
        assertFalse(rs.next());

        assertFalse(rs.isClosed());
        rs.close();
        assertTrue(rs.isClosed());

        assertFalse(statement.isClosed());
        statement.close();
        assertTrue(statement.isClosed());
    }

    /** Test of iterator method, of class RowIterator. */
    @Test
    public void testIterator() throws SQLException, IOException {
        System.out.println("iterator");

        List<Object[]> expected = createTableValues(3);
        List<Object[]> result = new ArrayList<>();

        Connection dbConnection = createConnection(expected);
        JdbcBuilder sql = new JdbcBuilder()
            .write("SELECT")
            .column("t.id")
            .column("t.name")
            .column("t.xxx")
            .column("t.date")
            .write("FROM test t");

        ResultSet rs = AbstractResultSet.of();
        LoopingIterator<ResultSet> iterator = sql.executeSelect(dbConnection);
        for (ResultSet resultSet : iterator) {
            Object[] row =
              { resultSet.getObject(1, Integer.class)
              , resultSet.getObject(2, String.class)
              , resultSet.getObject(3, Object.class)
              , resultSet.getObject(4, LocalDateTime.class)
            };
            result.add(row);
            rs = resultSet; // Real result set;
        }

        assertTrue(rs.isClosed());
        assertEquals(expected.size(), result.size());
        assertNotEquals(0, result.size());
        for (int i = 0; i < expected.size(); i++) {
            for (int j = 0; j < expected.get(i).length; j++) {
                assertEquals(expected.get(i)[j], result.get(i)[j]);
            }
        }
        assertFalse(AbstractResultSet.of().isClosed());
    }

   /** Creating a mock connection for many rows in ResultSet */
    private Connection createConnection( List<Object[]> tableValues) throws SQLException, IOException {
        return AbstractPreparedStatement.createConnection(tableValues);
    }

    /** Create 4 column values for DB table */
    private List<Object[]> createTableValues(int count) {
        final List<Object[]> result = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            Object[] rowValues = {(1 + i) * 10, ((char)('A' + i)) + " name", null, SOME_DATE.plusHours(i)};
            result.add(rowValues);
        }

        return result;
    }

}
