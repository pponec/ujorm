package org.ujorm.core.impl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.CityUjo;

import static org.junit.jupiter.api.Assertions.*;

class AbstractDomainHandlerTest {

    /**
     * Tests behavior when array mutation is enabled.
     * The original array should be modified and returned.
     */
    @Test
    @DisplayName("Should modify original array when mutation is enabled")
    void shouldModifyOriginalArrayWhenMutationEnabled() {
        var domainHandler = createDomainHandler(true);
        var city = new CityUjo();
        var keyLat = CityUjo.keyLatitude;
        var inputArray = city.array.clone(); // Create new Array
        inputArray[keyLat.getIndex()] = null;// Ensure the NULL value

        var resultArray = domainHandler.normalizePrimitives(inputArray);
        assertSame(inputArray, resultArray, "Should return the exact same array instance");
        assertEquals(0.0, resultArray[keyLat.getIndex()], "Null should be replaced by primitive default (0)");

        inputArray[keyLat.getIndex()] = 9.0; // Assign 9
        resultArray = domainHandler.normalizePrimitives(inputArray);
        assertSame(inputArray, resultArray, "Should return the exact same array instance");
        assertEquals(9.0, resultArray[keyLat.getIndex()], "Null should be replaced by primitive default (0)");

    }

    /**
     * Tests behavior when array mutation is disabled.
     * A clone should be modified and returned, leaving the original array intact.
     */
    @Test
    @DisplayName("Should return a clone when mutation is disabled")
    void shouldReturnCloneWhenMutationDisabled() {
        var domainHandler = createDomainHandler(false);
        var city = new CityUjo();
        var keyLat = CityUjo.keyLatitude;
        var inputArray = city.array.clone(); // Create new Array
        inputArray[keyLat.getIndex()] = null; // Ensure the NULL value

        var resultArray = domainHandler.normalizePrimitives(inputArray);
        assertNotSame(inputArray, resultArray, "Should return the exact same array instance");
        assertEquals(0.0, resultArray[keyLat.getIndex()], "Null should be replaced by primitive default (0)");

        inputArray[keyLat.getIndex()] = 9.0; // Assign 9
        resultArray = domainHandler.normalizePrimitives(inputArray);
        assertNotSame(inputArray, resultArray, "Should return the exact same array instance");
        assertEquals(9.0, resultArray[keyLat.getIndex()], "Null should be replaced by primitive default (0)");
    }

    /**
     * Helper method to instantiate the class under test.
     * @param enableMutation Configuration for array handling.
     * @return A configured instance of the normalizer.
     */
    private AbstractDomainHandler createDomainHandler(boolean enableMutation) {
        return new AbstractDomainHandler(enableMutation,
                CityUjo.keyId,
                CityUjo.keyName,
                CityUjo.keyCountryCode,
                CityUjo.keyLatitude,
                CityUjo.keyLongitude) {
            @Override
            public @NotNull Class getDomainClass() {
                return null;
            }
            @Override
            public Object newDomain(Object[] values) {
                return null;
            }
        };
    }

}