package org.ujorm.core.composed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.CityUjo;
import org.ujorm.core.demo.Employee;
import org.ujorm.core.demo.EmployeeUjo;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for {@link ComposedKeyImpl} using real domain objects.
 */
class ComposedKeyIntegrationTest {

    private Employee emp;
    private Employee boss;
    private City brno;

    @BeforeEach
    void setUp() {
        brno = City.of(1L, "Brno");

        boss = new Employee();
        boss.setId(10L);
        boss.setName("Pavel");
        boss.setCity(brno);

        emp = new Employee();
        emp.setId(20L);
        emp.setName("Honza");
        emp.setBoss(boss);
        emp.setCity(brno);
        emp.setContractDay(LocalDate.now());
    }

    /** Create a composed key: Employee.boss.name */
    @Test
    @DisplayName("Test: Employee -> Boss -> Name")
    void testBossName() {
        var bossNameKey = ComposedKeyImpl.of(EmployeeUjo.keyBoss, EmployeeUjo.keyName);

        var result = bossNameKey.getValue(emp);
        assertEquals("Pavel", result);
    }

    /** Create a composed key: Employee.city.name */
    @Test
    @DisplayName("Test: Employee -> City -> Name")
    void testCityName() {
        var cityNameKey = ComposedKeyImpl.of(EmployeeUjo.keyCity, CityUjo.keyName);

        var result = cityNameKey.getValue(emp);
        assertEquals("Brno", result);
    }

    /** Test: setValue on a deep path (Employee -> Boss -> Active) */
    @Test
    @DisplayName("Test: setValue on a deep path (Employee -> Boss -> Active)")
    void testDeepSetValue() {
        var bossActiveKey = ComposedKeyImpl.of(EmployeeUjo.keyBoss, EmployeeUjo.keyActive);

        bossActiveKey.setValue(emp, true);

        assertTrue(boss.isActive(), "Boss should be active now");
    }

    /** Test: getValue via null boss */
    @Test
    @DisplayName("Test: getValue via null boss")
    void testNullIntermediateRead() {
        emp.setBoss(null);
        var bossNameKey = ComposedKeyImpl.of(EmployeeUjo.keyBoss, EmployeeUjo.keyName);

        var result = bossNameKey.getValue(emp);
        assertNull(result, "Result must be null when boss is missing");
    }

    /** Test: setValue via null city (exception) */
    @Test
    @DisplayName("Test: setValue via null city (exception)")
    void testNullIntermediateWrite() {
        emp.setCity(null);
        var cityNameKey = ComposedKeyImpl.of(EmployeeUjo.keyCity, CityUjo.keyName);

        var exception = assertThrows(NullPointerException.class, () ->
                cityNameKey.setValue(emp, "Nové Město"));

        assertTrue(exception.getMessage().contains("Employee.city"));
    }

    /** Test: Triple composition (Employee -> Boss -> City -> Name) */
    @Test
    @DisplayName("Test: Triple composition (Employee -> Boss -> City -> Name)")
    void testTripleComposition() {
        var bossCityNameKey = ComposedKeyImpl.of(
                EmployeeUjo.keyBoss,
                EmployeeUjo.keyCity,
                CityUjo.keyName);

        var result = bossCityNameKey.getValue(emp);
        assertEquals("Brno", result);
        assertEquals("boss.city.name", bossCityNameKey.name());
        assertEquals("Employee.boss.city.name", bossCityNameKey.fullName());
    }
}