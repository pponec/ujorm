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
