package org.ujorm.mapper.tutorial;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoreDemoTest {

    @Test @Order(0)
    void createTable() {

    }

    @Test @Order(100)
    void insert() {

    }

    @Test @Order(200)
    void select() {

    }

    @Test @Order(300)
    void update() {

    }

    @Test @Order(400)
    void delete() {

    }


}
