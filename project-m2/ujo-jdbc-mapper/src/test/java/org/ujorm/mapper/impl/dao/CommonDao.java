package org.ujorm.mapper.impl.dao;

import lombok.RequiredArgsConstructor;
import org.ujorm.tools.sql.SqlParamBuilder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CommonDao {

    final Connection dbConnection;

    /** Create database tables */
    public void initTables() throws SQLException {
        if (hasTables()) {
            return;
        }
        var sqlStatements = """
                CREATE TABLE country
                   ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                   , name VARCHAR(50) NOT NULL
                   );
                CREATE TABLE city
                   ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                   , name VARCHAR(50) NOT NULL
                   , country_id BIGINT NOT NULL
                   , CONSTRAINT fk_city_country_id__id FOREIGN KEY (country_id) REFERENCES COUNTRY(id) ON DELETE RESTRICT ON UPDATE RESTRICT
                   );
                CREATE TABLE department
                   ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                   , name VARCHAR(50) NOT NULL
                   );
                CREATE TABLE employee
                   ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                   , name VARCHAR(50) NOT NULL
                   , superior_id BIGINT NULL
                   , department_id BIGINT NOT NULL
                   , city_id BIGINT NOT NULL
                   , contract_day DATE NULL
                   );
                ALTER TABLE employee ADD CONSTRAINT fk_employee_superior_id__id
                      FOREIGN KEY (superior_id)
                      REFERENCES employee(id)
                      ON DELETE RESTRICT ON UPDATE RESTRICT;
                ALTER TABLE employee ADD CONSTRAINT fk_employee_department_id__id
                      FOREIGN KEY (department_id)
                      REFERENCES department(id)
                      ON DELETE RESTRICT ON UPDATE RESTRICT;
                ALTER TABLE employee ADD CONSTRAINT fk_employee_city_id__id
                      FOREIGN KEY (city_id)
                      REFERENCES city(id)
                      ON DELETE RESTRICT ON UPDATE RESTRICT;
                """;
        try (var builder = new SqlParamBuilder(dbConnection)) {
            Stream.of(sqlStatements.split(";"))
                    .filter(sql -> !sql.trim().isEmpty())
                    .forEach(sql -> {
                builder.sql(sql).execute();
            });
        }
        dbConnection.commit();
    }

    public boolean hasTables() {
        try {
            String requiredTableName = "employee";
            DatabaseMetaData metaData = dbConnection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, requiredTableName, null)) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new org.ujorm.tools.sql.SQLException(ex);
        }
    }

}
