package org.ujorm.mapper.tutorial;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.ujorm.tools.jdbc.SqlParamBuilder;

public class BasicDemoTest extends AbstractDemo {

    @Test @Order(100)
    void createTable() {
        try (var builder = new SqlParamBuilder(connection())) {
            builder.sql("""
                    CREATE TABLE city
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , country_code VARCHAR(2) NOT NULL
                    )
                    """).execute();
            builder.sql("""
                    CREATE TABLE employee
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , superior_id BIGINT NULL
                    , city_id BIGINT NOT NULL
                    )
                    """).execute();
            builder.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_superior_id__id
                    FOREIGN KEY (superior_id)
                    REFERENCES employee(id)
                    ON DELETE RESTRICT ON UPDATE RESTRICT;
                    """).execute();
            builder.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_city_id__id
                    FOREIGN KEY (city_id)
                    REFERENCES city(id)
                    ON DELETE CASCADE ON UPDATE RESTRICT;
                    """).execute();
        }
    }

    @Test
    @Order(100)
    void insert() {

    }

    @Test
    @Order(100)
    void select() {

    }

    @Test
    @Order(100)
    void update() {

    }

    @Test
    @Order(100)
    void delete() {

    }


}
