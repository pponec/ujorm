package org.ujorm.orm.core;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.dsl.SelectQuery;
import org.ujorm.orm.utils.EntityContext;

/**
 * The writable property contract of the {@link EntityManager} covers the own properties of the
 * entity. An entity behind a relation keeps its own contract, so a read-only property of the
 * target reports itself on the first row mapping instead of at the EntityManager creation.
 * <p>This test pins the trade-off documented in README.md and in the changes.txt file.
 */
@SuppressWarnings("java:S5786") // Public for the nested entities - see EntityManagerContractTest
public class EntityManagerRelationContractTest extends AbstractDaoTest {

    private static final EntityContext CTX = EntityContext.ofDefault();

    @Override
    protected void initTables() {
        super.initTables();
        try (var query = new SqlQuery(dbConnection)) {
            query.sql("""
                    CREATE TABLE IF NOT EXISTS late_read_only
                       ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                       , web_release BOOLEAN NOT NULL DEFAULT FALSE
                       )
                    """).execute();
            query.sql("""
                    CREATE TABLE IF NOT EXISTS late_owner
                       ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                       , read_only_id BIGINT
                       )
                    """).execute();
            query.sql("INSERT INTO late_read_only (web_release) VALUES (TRUE)").execute();
            query.sql("INSERT INTO late_owner (read_only_id) VALUES (1)").execute();
        }
    }

    /** The owner has writable properties only, so its EntityManager is created. */
    @Test
    void ownerOfAReadOnlyTargetIsAccepted() {
        Assertions.assertNotNull(CTX.entityManager(LateOwner.class, Long.class));
    }

    /** The target reports its read-only property when the mapper assigns the joined value. */
    @Test
    void readOnlyTargetFailsOnTheRowMapping() {
        var em = CTX.<LateOwner, Long>entityManager(LateOwner.class, Long.class);

        var result = Assertions.assertThrows(UnsupportedOperationException.class,
                () -> SelectQuery.run(dbConnection, em, query -> query
                        .columns(true)
                        .column(key(LateOwner.class, "readOnly"), key(LateReadOnly.class, "webRelease"))
                        .toList()));

        Assertions.assertTrue(result.getMessage().contains("LateReadOnly.webRelease"),
                "The property is named in full: " + result.getMessage());
    }

    /** A key of a test entity, which has no generated metamodel class. */
    @SuppressWarnings("unchecked")
    private static <D, V> Key<D, V> key(Class<D> domainClass, String name) {
        return (Key<D, V>) (Key<?, ?>) DomainHandlerProvider.getHandler(domainClass).getKey(name);
    }

    @Table(name = "late_read_only")
    public static class LateReadOnly {
        @Id
        private Long id;
        /** A property with a getter only */
        @Column(name = "web_release")
        private boolean webRelease;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public boolean isWebRelease() {
            return webRelease;
        }
    }

    @Table(name = "late_owner")
    public static class LateOwner {
        @Id
        private Long id;
        @JoinColumn(name = "read_only_id")
        private LateReadOnly readOnly;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public LateReadOnly getReadOnly() {
            return readOnly;
        }

        public void setReadOnly(LateReadOnly readOnly) {
            this.readOnly = readOnly;
        }
    }
}
