package org.ujorm.mapper.impl.dao;

import net.ponec.jdbc.entity.Employee;
import org.simpleflatmapper.jdbc.DynamicJdbcMapper;
import org.simpleflatmapper.jdbc.JdbcMapperFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class EmployeeDao extends GenericCrudManager<Employee, Long> {
    final Connection dbConnection;

    final static DynamicJdbcMapper<Employee> EMPLOYEE_MAPPER = JdbcMapperFactory
            .newInstance().newMapper(Employee.class);

    public EmployeeDao(Connection dbConnection) throws SQLException {
        super(dbConnection, Employee.class, Employee::setId);
        this.dbConnection = dbConnection;
    }

    /** Test the creation and retrieval of a Country entity. */
    public List<Employee> findAllEmployees(Long minId) throws SQLException {
        var sql = """
                SELECT e.id
                     , e.name
                     , c.name AS city_name
                     , e.department_id
                     , d.name AS department_name
                     , e.contract_day
                     , r.name AS city_country_name
                     , e.superior_id
                     , s.name AS superior_name
                FROM employee e
                JOIN department d ON d.id = e.department_id
                JOIN city c ON c.id = e.city_id
                JOIN country r ON r.id = c.country_id
                LEFT JOIN employee s ON s.id = e.superior_id
                WHERE e.id >= :id
                ORDER BY e.id
                """;
        try (var builder = sqlBuilder()) {
            return builder
                    .sql(sql)
                    .bind("id", minId)
                    .streamMap(EMPLOYEE_MAPPER::map)
                    .toList();
        }
    }

}
