package org.ujorm.core.generator;

import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class DomainPropertyModelTest {

    /**
     * Test mapping for a standard Java Bean with JPA annotations.
     */
    @Test
    void ofBean() {
        var properties = DomainPropertyModel.of(Employee.class);

        // Check total count of properties
        assertEquals(6, properties.size());

        // Test "id" property (PK, NotNull)
        var idProp = findProperty(properties, "id");
        assertEquals(Long.class, idProp.propertyType());
        assertEquals("getId", idProp.getter());
        assertEquals("setId", idProp.setter());
        assertEquals("id", idProp.dbColumName());
        assertFalse(idProp.isPrimitive());
        assertTrue(idProp.primaryKey());
        assertTrue(idProp.required());
        assertSame(idProp, properties.get(0), "The first field is expected on the first position");

        // Test "name" property (Basic column)
        var nameProp = findProperty(properties, "name");
        assertEquals(String.class, nameProp.propertyType());
        assertEquals("name", nameProp.dbColumName());
        assertFalse(nameProp.primaryKey());
        assertTrue(nameProp.required());

        // Test "superior" property (Object relation, JoinColumn, Nullable)
        var superiorProp = findProperty(properties, "superior");
        assertEquals(Employee.class, superiorProp.propertyType());
        assertEquals("getSuperior", superiorProp.getter());
        assertEquals("setSuperior", superiorProp.setter());
        assertEquals("superior_id", superiorProp.dbColumName()); // Value from @JoinColumn
        assertFalse(superiorProp.primaryKey());
        assertFalse(superiorProp.required()); // Explicit @Nullable annotation overrides


        // Test "contractDay" property (CamelCase field, snake_case DB column via Annotation)
        var dateProp = findProperty(properties, "contractDay");
        assertEquals(LocalDate.class, dateProp.propertyType());
        assertEquals("getContractDay", dateProp.getter());
        assertEquals("setContractDay", dateProp.setter());
        assertEquals("contract_day", dateProp.dbColumName()); // Value from @Column
        assertFalse(dateProp.primaryKey());
        assertTrue(dateProp.required());

        // Test "active" property (CamelCase field, snake_case DB column via Annotation)
        var activeProp = findProperty(properties, "active");
        assertEquals("isActive", activeProp.getter());
        assertEquals("setActive", activeProp.setter());
        assertTrue(activeProp.required());
        assertSame(activeProp, properties.get(properties.size() - 1), "The first field is expected on the first position");
    }

    /**
     * Test mapping for a Java Record with JPA annotations.
     */
    @Test
    void ofRecord() {
        var properties = DomainPropertyModel.of(City.class);

        // Check total count of components
        assertEquals(5, properties.size());

        // Test "id" component (PK)
        var idProp = findProperty(properties, "id");
        assertEquals("id", idProp.propertyName());
        assertEquals("db_id", idProp.dbColumName());
        assertTrue(idProp.primaryKey());
        assertTrue(idProp.required());
        assertSame(idProp, properties.get(0), "The first field is expected on the first position");

        // Test "name" component (Standard string)
        var nameProp = findProperty(properties, "name");
        assertEquals("name", nameProp.propertyName());
        assertEquals("db_name", nameProp.dbColumName());
        assertEquals(String.class, nameProp.propertyType());
        assertEquals("name", nameProp.getter());
        assertNull(nameProp.setter());
        assertTrue(nameProp.required());

        // Test "countryCode" component (Explicit @Column name)
        var codeProp = findProperty(properties, "countryCode");
        assertEquals("countryCode", codeProp.propertyName());
        assertEquals("country_code", codeProp.dbColumName());
        assertEquals("countryCode", codeProp.getter());
        assertTrue(codeProp.required());

        // Test "latitude" component (No annotation -> fallback to field name)
        var latProp = findProperty(properties, "latitude");
        assertEquals("latitude", latProp.propertyName());
        assertEquals("latitude", latProp.dbColumName());
        assertFalse(latProp.primaryKey());
        assertTrue(latProp.required()); // Double wrapper is not nullable
        assertNull(latProp.setter());
        assertTrue(latProp.required());
    }

    /**
     * Helper method to find a property model by its name.
     *
     * @param properties List of property models to search in.
     * @param name       The name of the property to find.
     * @return The found BeanPropertyModel.
     * @throws NoSuchElementException if the property is not found.
     */
    private DomainPropertyModel findProperty(List<DomainPropertyModel> properties, String name) {
        return properties.stream()
                .filter(p -> p.propertyName().equals(name))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Property not found: " + name));
    }
}