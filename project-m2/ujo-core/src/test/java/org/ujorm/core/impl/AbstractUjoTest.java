package org.ujorm.core.impl;
import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AbstractUjo class testing the of() static methods.
 */
class AbstractUjoTest {

    private DomainHandlerService handlerProvider = DomainHandlerProvider.provider();

    /**
     * Tests the static of() methods using a standard Java Bean.
     */
    @Test
    void testOfWithJavaBean() {
        var handler = handlerProvider.getHandler(Employee.class);

        var emptyUjo = AbstractUjo.of(handler);
        var emptyResult = emptyUjo.toDomainObject();
        assertNotNull(emptyResult);
        assertNull(emptyResult.getId());
        assertNull(emptyResult.getName());
        assertEquals(false, emptyResult.isActive());

        // Test the of(domainObject, handler) method
        var employee = new Employee();
        employee.setName("Alice");
        var filledUjo = AbstractUjo.of(employee, handler);
        var result = filledUjo.toDomainObject();
        assertNotNull(result);
        assertEquals(employee, result);
        assertEquals("Alice", result.getName());
    }

    /**
     * Tests the static of() methods using a Java Record.
     */
    @Test
    void testOfWithJavaRecord() {
        var handler = handlerProvider.getHandler(City.class);

        // Test the empty of(handler) method
        var emptyUjo = AbstractUjo.of(handler);
        var emptyResult = emptyUjo.toDomainObject();
        assertNotNull(emptyResult);
        assertNull(emptyResult.id());
        assertNull(emptyResult.name());
        assertEquals(0.0, emptyResult.latitude(), 0.00001);

        // Test the of(domainObject, handler) method
        var city = City.of(1L, "Prague");
        var filledUjo = AbstractUjo.of(city, handler);
        var result = filledUjo.toDomainObject();

        assertNotNull(result);
        assertEquals(city, result);
        assertEquals(1L, result.id());
        assertEquals("Prague", result.name());
        assertEquals(0.0, emptyResult.latitude(), 0.00001);
    }

}