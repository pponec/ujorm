package org.ujorm.orm.metaModel;

import java.sql.SQLException;

import junit.framework.TestCase;
import org.junit.Assert;
import org.ujorm.Key;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.Session;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Db;
import org.ujorm.orm.annot.Table;
import org.ujorm.orm.dialect.H2Dialect;

/**
 * @author Franta Mejta
 * @date 2021-03-09T14:19:05+01:00
 */
public class IndexTest extends TestCase {

    public void testCreateIndicesOnDatabaseCreation() {
        MetaParams params = new MetaParams();
        params.set(MetaParams.AUTO_CLOSING_DEFAULT_SESSION, false);

        OrmHandler handler = new OrmHandler();
        handler.config(params);
        handler.loadDatabase(IndexDatabase.class);

        String uniqueValue = "uniqueValue";
        IndexTable r1 = newRow(uniqueValue);
        IndexTable r2 = newRow(uniqueValue);

        Session s = handler.getDefaultSession();
        s.insert(r1);

        try {
            s.insert(r2);
            Assert.fail("should have failed on unique index violation");
        } catch (IllegalUjormException ex) {
            Throwable cause = ex.getCause();
            Assert.assertNotNull(cause);
            Assert.assertTrue(cause instanceof SQLException);

            SQLException sql = (SQLException) cause;
            Assert.assertEquals("23505", sql.getSQLState());
        }
    }

    private IndexTable newRow(String value) {
        IndexTable t = new IndexTable();
        t.setIndexColumn(value);

        return t;
    }

    @Db(schema = "index_test", dialect = H2Dialect.class, user = "sa", password = "", jdbcUrl = "jdbc:h2:mem:index_test;DB_CLOSE_DELAY=-1")
    public static class IndexDatabase extends OrmTable<IndexDatabase> {

        @Table(name = "index_table") @SuppressWarnings("unused")
        public static final RelationToMany<IndexDatabase, IndexTable> INDEX_TABLE = newRelation();

    }

    public static class IndexTable extends OrmTable<IndexTable> {

        @Column(name = "id_index_column", pk = true)
        public static final Key<IndexTable, Integer> ID = newKey();
        @Column(name = "index_column", uniqueIndex = "idx")
        public static final Key<IndexTable, String> INDEX_COLUMN = newKey();

        void setIndexColumn(String value) {
            INDEX_COLUMN.setValue(this, value);
        }

    }

}
