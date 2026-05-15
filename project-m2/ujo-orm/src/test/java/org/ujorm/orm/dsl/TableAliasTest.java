package org.ujorm.orm.dsl;

import org.junit.jupiter.api.Test;
import org.ujorm.orm.demo.City;
import org.ujorm.orm.demo.Employee;
import org.ujorm.orm.demo.MetaCity;
import org.ujorm.orm.demo.MetaEmployee;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test for TableAlias.aliasedKey using domain metamodels.
 */
class TableAliasTest {

    /** Test for a simple key (path size 1) */
    @Test
    void testAliasedKeySimple() {
        var aliasName = "emp";
        var result = TableAlias.aliasedKey(aliasName, MetaEmployee.name);

        assertNotNull(result);
        assertEquals(1, result.pathSize());
        assertEquals(aliasName, result.tableAlias());
        assertEquals(MetaEmployee.name.name(), result.name());
        assertEquals(Employee.class, result.domainClass());
    }

    /** Test for a composed key (path size 2) */
    @Test
    void testAliasedKeyComposed() {
        var aliasName = "c";
        // Create a composed key: Employee -> City -> Name
        var composedPath = MetaEmployee.city.join(MetaCity.name);

        var result = TableAlias.aliasedKey(aliasName, composedPath);

        assertNotNull(result);
        assertEquals(2, result.pathSize(), "The path size must be preserved.");
        assertEquals(Employee.class, result.domainClass(), "The root domain class must be Employee.");

        // The first item (relation to City) must NOT have an alias
        var firstItem = result.pathItem(0);
        assertEquals(MetaEmployee.city.name(), firstItem.name());
        assertTrue(firstItem.tableAlias().isEmpty(), "The relation key should not have an alias.");

        // The last item (City.name) MUST have the alias for SelectQueryBuilder
        var lastItem = result.pathItem(1);
        assertEquals(MetaCity.name.name(), lastItem.name());
        assertEquals(aliasName, lastItem.tableAlias(), "The last item must carry the alias.");
        assertEquals(City.class, lastItem.domainClass(), "The last item must retain the City domain class.");
    }

    /** Test for a double composed key (path size 2) with different attributes */
    @Test
    void testAliasedKeyCityCoordinates() {
        var aliasName = "loc";
        var latPath = MetaEmployee.city.join(MetaCity.latitude);

        var result = TableAlias.aliasedKey(aliasName, latPath);

        assertEquals(2, result.pathSize());
        assertEquals(aliasName, result.pathItem(1).tableAlias());
        assertEquals("latitude", result.pathItem(1).name());
        assertEquals(City.class, result.pathItem(1).domainClass());
    }
}