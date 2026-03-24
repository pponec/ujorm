package org.ujorm.orm.tutorial;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ujorm.orm.SqlQuery;

/** MS SQL Server integration test implementation */
@Testcontainers
class MssqlTutorialIT extends AbstractTutorialIT {

    @Container
    @ServiceConnection
    static MSSQLServerContainer<?> mssql = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest")
            .acceptLicense();

    @Override
    void init() {
        try (var query = new SqlQuery(connection())) {
            query.sql("""
                    CREATE TABLE city
                    ( id INT IDENTITY(1,1) PRIMARY KEY
                    , name NVARCHAR(50) NOT NULL
                    , country_code NVARCHAR(2) NOT NULL
                    )
                    """).execute();

            query.sql("""
                    CREATE TABLE employee
                    ( id INT IDENTITY(1,1) PRIMARY KEY
                    , name NVARCHAR(50) NOT NULL
                    , boss_id INT NULL
                    , city_id INT NOT NULL
                    , CONSTRAINT fk_emp_city FOREIGN KEY (city_id) REFERENCES city(id) ON DELETE CASCADE
                    , CONSTRAINT fk_emp_boss FOREIGN KEY (boss_id) REFERENCES employee(id)
                    )
                    """).execute();
        }
    }
}