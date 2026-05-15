package org.ujorm.generator.test;

import org.junit.jupiter.api.Test;
import org.ujorm.generator.test.entity.StandardEntity;

import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/** Integration tests for Ujorm APT Meta Generator */
class UjormMetaProcessorTest {

    private final String packageName = StandardEntity.class.getPackageName() + ".";

    /** parameter 'false' prevents static initialization and Ujorm runtime crash on invalid entitie */
     @Test
    public void testStandardOuterEntity() throws ClassNotFoundException {
        var metaClass = Class.forName(packageName + "MetaStandardEntity", false, getClass().getClassLoader());
        assertMetaFields(metaClass);
    }

    @Test
    public void testStandardInnerEntity() throws ClassNotFoundException {
        var metaClass = Class.forName(packageName + "MetaInnerEntity", false, getClass().getClassLoader());
        assertMetaFields(metaClass);
    }

    @Test
    public void testLombokOuterEntity() throws ClassNotFoundException {
        var metaClass = Class.forName(packageName + "MetaLombokEntity", false, getClass().getClassLoader());
        assertMetaFields(metaClass);
    }

    @Test
    public void testLombokInnerEntity() throws ClassNotFoundException {
        var metaClass = Class.forName(packageName + "MetaInnerLombok", false, getClass().getClassLoader());
        assertMetaFields(metaClass);
    }

    @Test
    public void testRecordEntity() throws ClassNotFoundException {
        var metaClass = Class.forName(packageName + "MetaRecordEntity", false, getClass().getClassLoader());

        // Record fields should be present
        assertNotNull(getField(metaClass, "id"), "Field 'id' should be generated");
        assertNotNull(getField(metaClass, "active"), "Field 'active' should be generated");
        assertNotNull(getField(metaClass, "name"), "Field 'name' should be generated");

        // Transient annotation should be ignored
        assertNull(getField(metaClass, "ignoredTransient"), "Transient field should be ignored");
    }

    /** Helper method to verify the presence and absence of generated fields */
    private void assertMetaFields(Class<?> metaClass) {
        // Valid properties that must be generated
        assertNotNull(getField(metaClass, "id"), "Field 'id' should be generated");
        assertNotNull(getField(metaClass, "active"), "Field 'active' should be generated");
        assertNotNull(getField(metaClass, "name"), "Field 'name' should be generated");

        // Invalid properties that must be ignored
        assertNull(getField(metaClass, "ignoredTransient"), "Transient field should be ignored");
        assertNull(getField(metaClass, "ignoredNoSetter"), "Field without setter should be ignored");
        assertNull(getField(metaClass, "ignoredNoGetter"), "Field without getter should be ignored");
    }

    /** Helper method to safely retrieve a field by name */
    private Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}