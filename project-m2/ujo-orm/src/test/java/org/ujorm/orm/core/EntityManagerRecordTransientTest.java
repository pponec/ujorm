package org.ujorm.orm.core;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.utils.EntityContext;

/**
 * A record component excluded by the @Transient annotation keeps its position in the canonical
 * constructor, so the ORM maps the remaining components to the database columns.
 */
@SuppressWarnings("java:S5786") // Public for the nested TransientCity - see EntityManagerContractTest
public class EntityManagerRecordTransientTest extends AbstractDaoTest {

    private static final EntityContext CTX = EntityContext.ofDefault();

    @Override
    protected void initTables() {
        super.initTables();
        try (var query = new SqlQuery(dbConnection)) {
            query.sql("""
                    CREATE TABLE IF NOT EXISTS transient_city
                       ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                       , name VARCHAR(50) NOT NULL
                       )
                    """).execute();
        }
    }

    /** The transient component is no database column, and it gets the default value on reading. */
    @Test
    void transientComponentIsNotMapped() {
        var crud = CTX.entityManager(TransientCity.class, Long.class).crud(dbConnection);
        var inserted = crud.insert(new TransientCity(null, "A temporary note", "Prague"));

        Assertions.assertNotNull(inserted.id(), "The primary key is assigned");

        var reloaded = crud.findById(inserted.id()).orElseThrow();
        Assertions.assertEquals(inserted.id(), reloaded.id());
        Assertions.assertEquals("Prague", reloaded.name(), "The component behind the transient one is mapped");
        Assertions.assertNull(reloaded.note(), "The transient component gets the default value");
    }

    /**
     * The entity returned by an INSERT is rebuilt to carry the generated primary key, so the
     * excluded component comes back reset while the passed object stays untouched. Keep the value
     * in the original object, or model it as a regular property.
     */
    @Test
    void transientComponentIsResetByInsert() {
        var crud = CTX.entityManager(TransientCity.class, Long.class).crud(dbConnection);
        var input = new TransientCity(null, "A temporary note", "Prague");
        var inserted = crud.insert(input);

        Assertions.assertEquals("A temporary note", input.note(), "The passed object stays untouched");
        Assertions.assertNull(inserted.note(), "The returned entity has the component reset");
        Assertions.assertEquals("Prague", inserted.name(), "A mapped component survives the rebuild");
        Assertions.assertNotNull(inserted.id(), "The primary key is assigned");
    }

    /** The transient component is missing in the SQL statement of the entity. */
    @Test
    void transientComponentIsOutOfTheSql() {
        var crud = CTX.entityManager(TransientCity.class, Long.class).crud(dbConnection);
        var sql = crud.selectWhere("id > 0", b -> b.toString());

        Assertions.assertTrue(sql.contains("\"NAME\""), sql);
        Assertions.assertFalse(sql.toLowerCase().contains("note"), sql);
    }

    /** The excluded component is placed between two mapped ones. */
    @Table(name = "transient_city")
    public record TransientCity(
            @Id Long id,
            @Transient String note,
            String name) {}
}
