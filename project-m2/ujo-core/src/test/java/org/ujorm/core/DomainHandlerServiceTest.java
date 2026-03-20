package org.ujorm.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.*;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DomainHandlerServiceTest {

    @Test @Order(100)
    void getHandler() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(Employee.class);
        var keyId = handler.getKey("id", Long.class);
        var keyCity = handler.getKey("city", City.class);
        var keySuperior = handler.getKey("boss", Employee.class);

        Assertions.assertNotNull(keyId);
        Assertions.assertEquals(Long.class, keyId.type());
        Assertions.assertEquals(City.class, keyCity.type());
        Assertions.assertEquals(Employee.class, keySuperior.type());
    }

    @Test @Order(200)
    void getHandlerOfInnerClass() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(CityInner.class);
        var key = handler.getKey("name", String.class);
        Assertions.assertNotNull(key);
    }

    /** Test the Parent Bean. */
    @Test @Order(300)
    void getHandler_parent() {
        var domainClass = Parent.class;
        var service = new DomainHandlerService();
        var handler = service.getHandler(domainClass);
        var keyId = handler.getKey("id", Long.class);
        Assertions.assertNotNull(keyId);
    }

    /** Test the Child Bean. */
    @Test  @Order(400)
    void getHandler_child() {
        var domainClass = Child.class;
        var service = new DomainHandlerService();
        var handler = service.getHandler(domainClass);
        var keyId = handler.getKey("id", Long.class);
        Assertions.assertNotNull(keyId);
    }

    /** Test case for verifying exception handling during handler creation. */
    @Order(500)
    @Test
    void getHandler_fail() {
        var service = new DomainHandlerService();

        // Capture the top-level exception
        var exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            service.getHandler(NoBean.class);
        });

        // 1. Check the top-level message
        var expectedTopMessage = "Cant create org.ujorm.gen_.org.ujorm.core.DomainHandlerServiceTest.NoBean_ "
                + "class for the domain: class org.ujorm.core.DomainHandlerServiceTest$NoBean";
        Assertions.assertEquals(expectedTopMessage, exception.getMessage());

        // 2. Check the nested cause (IllegalArgumentException)
        var cause = exception.getCause();
        Assertions.assertNotNull(cause, "Exception should have a cause");
        Assertions.assertInstanceOf(IllegalArgumentException.class, cause);

        var expectedCauseMessage = "Only Bean and Record domain objects are supported";
        Assertions.assertEquals(expectedCauseMessage, cause.getMessage());
    }

    /** Simple Java Record representing a City. */
    public record CityInner(String name) {}

    @RequiredArgsConstructor @Getter
    public static class NoBean {
        private final Integer id;
    }

    @Getter @Setter
    public static class Parent {
        private long id;
    }

    @Getter @Setter
    public static class Child extends Parent {
        private String name;
    }
}