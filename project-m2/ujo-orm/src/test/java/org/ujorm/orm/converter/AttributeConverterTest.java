package org.ujorm.orm.converter;

import org.junit.jupiter.api.Test;
import org.ujorm.orm.AbstractDatabaseTest;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.dsl.SelectQuery;
import org.ujorm.orm.utils.EntityContext;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/** Tests of the explicit JPA converters declared by the {@code @Convert} annotation */
class AttributeConverterTest extends AbstractDatabaseTest {

    private final EntityContext ctx = EntityContext.ofDefault();
    private final EntityManager<Customer, Long> customerEm = ctx.entityManager(Customer.class);

    @Test
    void insertAndRead() {
        var crud = customerEm.crud(connection());
        var id = crud.insert(Customer.of("Joe", "Joe@Example.com", true, CustomerState.INACTIVE)).getId();

        var result = crud.findById(id).orElseThrow();
        assertEquals(new Email("joe@example.com"), result.getEmail());
        assertTrue(result.isActive());
        assertEquals(Boolean.TRUE, result.getNewsletter());
        assertEquals(CustomerState.INACTIVE, result.getState());

        // Raw database values:
        var row = SqlQuery.run(connection(), query -> query
                .sql("SELECT email, active, state FROM customer WHERE id = :id")
                .bind("id", id)
                .toStream(rs -> rs.getString(1) + '|' + rs.getString(2) + '|' + rs.getString(3))
                .findFirst()
                .orElseThrow());
        assertEquals("joe@example.com|Y|I", row);
    }

    @Test
    void nullIsNeverConverted() {
        var crud = customerEm.crud(connection());
        var id = crud.insert(Customer.of("Ann", null, false, CustomerState.ACTIVE)).getId();

        var result = crud.findById(id).orElseThrow();
        assertNull(result.getEmail());
        assertFalse(result.isActive());

        var count = SqlQuery.run(connection(), query -> query
                .sql("SELECT COUNT(*) FROM customer WHERE email IS NULL")
                .toStream(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());
        assertEquals(1, count);
    }

    @Test
    void update() {
        var crud = customerEm.crud(connection());
        var customer = crud.insert(Customer.of("Tom", "tom@example.com", true, CustomerState.ACTIVE));
        customer.setEmail(new Email("tom@example.org"));
        customer.setActive(false);
        customer.setState(CustomerState.INACTIVE);
        crud.update(customer);

        var result = crud.findById(customer.getId()).orElseThrow();
        assertEquals(new Email("tom@example.org"), result.getEmail());
        assertFalse(result.isActive());
        assertEquals(CustomerState.INACTIVE, result.getState());
    }

    @Test
    void criteria() {
        var crud = customerEm.crud(connection());
        crud.insert(Customer.of("Joe", "joe@example.com", true, CustomerState.ACTIVE));
        crud.insert(Customer.of("Ann", "ann@example.com", false, CustomerState.INACTIVE));
        crud.insert(Customer.of("Tom", "tom@example.com", true, CustomerState.INACTIVE));

        assertEquals("Joe", findNames(MetaCustomer.email.whereEq(new Email("JOE@example.com"))));
        assertEquals("Joe,Tom", findNames(MetaCustomer.active.whereEq(true)));
        assertEquals("Ann,Tom", findNames(MetaCustomer.state.whereIn(CustomerState.INACTIVE)));
        assertEquals("Ann", findNames(MetaCustomer.state.whereEq(CustomerState.INACTIVE)
                .and(MetaCustomer.active.whereEq(false))));
        assertEquals("Ann,Tom", findNames(MetaCustomer.state.whereSql("{0} = {1}", CustomerState.INACTIVE)));
    }

    @Test
    void sqlQueryBind() {
        var crud = customerEm.crud(connection());
        crud.insert(Customer.of("Joe", "joe@example.com", true, CustomerState.ACTIVE));
        crud.insert(Customer.of("Ann", "ann@example.com", false, CustomerState.INACTIVE));

        var names = SqlQuery.run(connection(), query -> query
                .sql("SELECT name FROM customer WHERE state = :state")
                .bind("state", MetaCustomer.state, CustomerState.INACTIVE)
                .toStream(rs -> rs.getString(1))
                .toList());
        assertEquals("[Ann]", names.toString());
    }

    private String findNames(org.ujorm.core.criterion.Criterion criterion) {
        return SelectQuery.run(connection(), customerEm, query -> query
                .columns(false)
                .where(criterion)
                .tail("ORDER BY", MetaCustomer.id)
                .toStream()
                .map(Customer::getName)
                .reduce((a, b) -> a + ',' + b)
                .orElse(""));
    }

    // -------------

    /** Initialize database schema */
    @Override
    protected void initSchema(Connection connection) {
        try (var query = new SqlQuery(connection)) {
            query.sql("""
                    CREATE TABLE customer
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , email VARCHAR(100) NULL
                    , active CHAR(1) NOT NULL
                    , newsletter BOOLEAN NULL
                    , state CHAR(1) NOT NULL
                    )
                    """).execute();
        }
    }
}
