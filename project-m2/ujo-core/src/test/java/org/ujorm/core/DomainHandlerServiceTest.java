package org.ujorm.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;

public class DomainHandlerServiceTest {

    @Test
    void getHandler() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(Employee.class);
        var keyId = handler.getKey("id", Long.class);
        var keyCity = handler.getKey("city", City.class);
        var keySuperior = handler.getKey("superior", Employee.class);

        Assertions.assertNotNull(keyId);
        Assertions.assertEquals(Long.class, keyId.getType());
        Assertions.assertEquals(City.class, keyCity.getType());
        Assertions.assertEquals(Employee.class, keySuperior.getType());
    }

    @Test
    void getHandlerOfInnerClass() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(CityInner.class);
        var key = handler.getKey("name", String.class);
        Assertions.assertNotNull(key);
    }

    /** Test case for verifying exception handling during handler creation. */
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
        Assertions.assertTrue(cause instanceof IllegalArgumentException);

        var expectedCauseMessage = "Only Bean and Record domain objects are supported";
        Assertions.assertEquals(expectedCauseMessage, cause.getMessage());
    }

    /** Simple Java Record representing a City. */
    public record CityInner(String name) {}

    @RequiredArgsConstructor @Getter
    public class NoBean {
        private final Integer id;
    }
}