package org.ujorm.orm.core;

import lombok.RequiredArgsConstructor;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;
import org.ujorm.orm.SqlQuery;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CommonDao {

    final Connection dbConnection;

    /** Create database tables */
    public void initTables() {
        if (hasTables()) {
            return;
        }
        var sqlStatements = """
                CREATE TABLE city
                   ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                   , name VARCHAR(50) NOT NULL
                   , country_code VARCHAR(2) NOT NULL
                   , latitude DECIMAL(10, 8) NOT NULL
                   , longitude DECIMAL(11, 8) NOT NULL
                   );
                CREATE TABLE employee
                   ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                   , name VARCHAR(50) NOT NULL
                   , superior_id BIGINT NULL
                   , city_id BIGINT NOT NULL
                   , contract_day DATE NULL
                   , is_active BOOLEAN DEFAULT true
                   );
                ALTER TABLE employee ADD CONSTRAINT fk_employee_superior_id__id
                      FOREIGN KEY (superior_id)
                      REFERENCES employee(id)
                      ON DELETE RESTRICT ON UPDATE RESTRICT;
                ALTER TABLE employee ADD CONSTRAINT fk_employee_city_id__id
                      FOREIGN KEY (city_id)
                      REFERENCES city(id)
                      ON DELETE RESTRICT ON UPDATE RESTRICT;
                """;
        try (var query = new SqlQuery(dbConnection)) {
            Stream.of(sqlStatements.split(";"))
                    .filter(sql -> !sql.trim().isEmpty())
                    .forEach(sql -> {
                        query.sql(sql).execute();
                    });
            dbConnection.commit();
        } catch (SQLException ex1) {
            try {
                dbConnection.rollback();
            } catch (SQLException ex2) {
                throw SQLExceptionBuilder.build(ex2);
            }
            throw SQLExceptionBuilder.build(ex1);
        }
    }

    public boolean hasTables() {
        try {
            String requiredTableName = "employee";
            DatabaseMetaData metaData = dbConnection.getMetaData();
            try (ResultSet resultSet = metaData.getTables(null, null, requiredTableName, null)) {
                return resultSet.next();
            }
        } catch (SQLException ex) {
            throw new org.ujorm.tools.jdbc.SQLException(ex);
        }
    }

}
