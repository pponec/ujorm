package org.ujorm.core.impl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.CityUjo;
import java.math.BigDecimal;
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
        var inputArray = city.array(); // Create new Array
        inputArray[keyLat.index()] = null;// Ensure the NULL value

        var resultArray = domainHandler.normalizePrimitives(inputArray);
        assertSame(inputArray, resultArray, "Should return the exact same array instance");
        assertEquals(0.0, resultArray[keyLat.index()], "Null should be replaced by primitive default (0)");

        inputArray[keyLat.index()] = 9.0; // Assign 9
        resultArray = domainHandler.normalizePrimitives(inputArray);
        assertSame(inputArray, resultArray, "Should return the exact same array instance");
        assertEquals(9.0, resultArray[keyLat.index()], "Null should be replaced by primitive default (0)");
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
        var inputArray = city.array(); // Create new Array
        inputArray[keyLat.index()] = null; // Ensure the NULL value

        var resultArray = domainHandler.normalizePrimitives(inputArray);
        assertNotSame(inputArray, resultArray, "Should return the exact same array instance");
        assertEquals(0.0, resultArray[keyLat.index()], "Null should be replaced by primitive default (0)");

        inputArray[keyLat.index()] = 9.0; // Assign 9
        resultArray = domainHandler.normalizePrimitives(inputArray);
        assertNotSame(inputArray, resultArray, "Should return the exact same array instance");
        assertEquals(9.0, resultArray[keyLat.index()], "Null should be replaced by primitive default (0)");
    }

   @Test
    void getKey() {
        var domainHandler = createDomainHandler(false);
        var emptyType = (Class<Long>) null;
        var filledType = Long.class;
        var wrongType = BigDecimal.class;
        var cityId = (Key<City, Long>) null;

        // Tests:
        cityId = domainHandler.getKey("id", emptyType);
        assertEquals(filledType, cityId.type());
        cityId = domainHandler.getKey("id", filledType);
        assertEquals(filledType, cityId.type());
        // Wrong type:
        var expectedMessage = "Property City.id has wrong type BigDecimal, expected is Long.";
        var ex = assertThrows(IllegalArgumentException.class, () -> {
            var wrongId = domainHandler.getKey("id", wrongType);
            System.out.println(wrongId.type());
        });
        assertEquals(expectedMessage, ex.getMessage());
    }


    /**
     * Helper method to instantiate the class under test.
     * @param enableMutation Configuration for array handling.
     * @return A configured instance of the normalizer.
     */
    private AbstractDomainHandler<City> createDomainHandler(boolean enableMutation) {
        return new AbstractDomainHandler<>(enableMutation,
                CityUjo.keyId,
                CityUjo.keyName,
                CityUjo.keyCountryCode,
                CityUjo.keyLatitude,
                CityUjo.keyLongitude) {
            @Override
            public @NotNull Class<City> getDomainClass() {
                return City.class;
            }
            @Override
            public City newDomain(Object... values) {
                return null;
            }
        };
    }

}