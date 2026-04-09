package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.dsl.meta.*;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** DslQuery test class */
class DslQueryTest {

    /** Default table alias */
    private static final String ALIAS = "t";

    /** Test a SELECT statement generation for Employee and City */
    @Test
    void testSelectEmployeeWithCity() {
        var connection = mock(Connection.class);
        // Passing the connection inside so the mocks can be properly paired
        var entityManager = getEntityManager(connection);

        var query = new DslQuery<>(connection, entityManager);
        query.sql("SELECT")
                .column(QEmployee.id)
                .column(QEmployee.name)
                .column(QEmployee.city, QCity.name)
                .where(QEmployee.id.whereGt(1L))
                .tail("ORDER BY", QEmployee.id);

        var sql = query.toString();

        assertNotNull(sql);
        System.out.println("<<SQL>>\n" + sql); // Print the query to the console for visual inspection
    }

    /**
     * Get a mocked EntityManager with deep stubs
     * @param connection Database connection
     * @return Mocked EntityManager
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityManager<Employee, Long> getEntityManager(Connection connection) {
        // Crucial change: RETURNS_DEEP_STUBS saves us from creating a bunch of intermediate steps
        var em = mock(EntityManager.class, Answers.RETURNS_DEEP_STUBS);

        // 1. Setup the base domain class
        when(em.getDomainClass()).thenReturn((Class) Employee.class);

        // 2. Resolve the path to the TableModel and its properties
        var tableService = em.getTableModelService();
        var tableModel = tableService.getTableModel(Employee.class, connection);

        when(tableModel.tableName()).thenReturn("EMPLOYEE");
        when(tableModel.jdbc().quotes()).thenReturn(QuotePair.ofDefault());

        // 3. Mock ColumnModels for all keys used in the query
        when(tableService.getColumnModel(QEmployee.id, connection).name()).thenReturn("eID");
        when(tableService.getColumnModel(QEmployee.name, connection).name()).thenReturn("eNAME");
        when(tableService.getColumnModel(QEmployee.city, connection).name()).thenReturn("eCITY_ID");
        when(tableService.getColumnModel(QCity.id, connection).name()).thenReturn("cID");
        when(tableService.getColumnModel(QCity.name, connection).name()).thenReturn("cNAME");

        return em;
    }
}