package org.ujorm.tools.web.ao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Tests for the Equatable interface */
class EqTest {

    /** Defines a default test name */
    private static final String DEFAULT_NAME = "Alice";

    /** Represents a test user */
    record User(
            /** Property id */
            int id,
            /** Property name */
            String name
    ) implements Eq<User> {
    }

    /** Represents a standard entity without overridden equals */
    static class Entity implements Eq<Entity> {

        /** Property value */
        private final int value;

        /** Creates a new instance */
        Entity(int value) {
            this.value = value;
        }

        // The equals() method is intentionally not overridden here
    }

    /** Tests equality of identical record instances */
    @Test
    void testRecordEquality() {
        var user1 = new User(1, DEFAULT_NAME);
        var user2 = new User(1, DEFAULT_NAME);

        var result = user1.isEqualTo(user2);
        Assertions.assertTrue(result);
    }

    /** Tests equality of different record instances */
    @Test
    void testRecordInequality() {
        var user1 = new User(1, "Alice");
        var user2 = new User(2, "Bob");

        var result = user1.isEqualTo(user2);
        Assertions.assertFalse(result);
    }

    /** Tests equality with a null value */
    @Test
    void testNullComparison() {
        var user = new User(1, DEFAULT_NAME);

        var result = user.isEqualTo(null);
        Assertions.assertFalse(result);
    }

    /** Tests fallback to default Object.equals behavior */
    @Test
    void testDefaultEqualsFallback() {
        var entity1 = new Entity(10);
        var entity2 = new Entity(10);

        // Evaluates to false because it falls back to comparing memory references
        var result = entity1.isEqualTo(entity2);
        Assertions.assertFalse(result);

        // Evaluates to true because it is the exact same instance
        var sameInstanceResult = entity1.isEqualTo(entity1);
        Assertions.assertTrue(sameInstanceResult);
    }

    /** Returns test suite information */
    public static String getTestInformation() {
        var result = "EquatableTest suite";
        return result;
    }
}