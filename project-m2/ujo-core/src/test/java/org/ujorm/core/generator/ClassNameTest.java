package org.ujorm.core.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ClassName record.
 */
class ClassNameTest {

    @Test
    @DisplayName("Test ofGenerated method with a standard class")
    void testOfGenerated() {
        var domainClass = String.class;
        var result = ClassName.ofGenerated(domainClass);

        assertEquals("org.ujorm.gen_.java.lang", result.packageName(),
                "Package should have the required prefix");
        assertEquals("String_", result.className(),
                "Class name should have a trailing underscore");
        assertEquals("org.ujorm.gen_.java.lang.String_", result.toString(),
                "Full string representation must be correct");
    }

    @Test
    @DisplayName("Test ofGenerated method with a custom class")
    void testOfGeneratedWithCustomClass() {
        var domainClass = ClassName.class;
        var result = ClassName.ofGenerated(domainClass);

        assertEquals("org.ujorm.gen_.org.ujorm.core.generator", result.packageName());
        assertEquals("ClassName_", result.className());
    }

    @Test
    @DisplayName("Test ofGenerated with an inner record")
    void testOfGeneratedWithInnerClass() {
        var domainClass = CityInner.class;
        var result = ClassName.ofGenerated(domainClass);
        var msg = "Verify inner class generation";

        assertEquals("org.ujorm.gen_.org.ujorm.core.generator.ClassNameTest", result.packageName(), msg);
        assertEquals("CityInner_", result.className(), msg);
        assertEquals("org.ujorm.gen_.org.ujorm.core.generator.ClassNameTest.CityInner_", result.toString(), msg);
    }

    @Test
    @DisplayName("Test classForName method with CityInner")
    void testClassForNameFailure() {
        var domainClass = CityInner.class;
        var className = ClassName.ofGenerated(domainClass);
        var result = className.classForName();

        assertNull(result, "Should return null because the generated class does not exist yet");
    }

    @Test
    @DisplayName("Test ofGenerated throws exception for class without canonical name")
    void testOfGeneratedThrowsException() {
        var anonymousClass = new Runnable() {
            @Override
            public void run() {} // default implementation ignored
        }.getClass();

        var exception = assertThrows(IllegalArgumentException.class,
                () -> ClassName.ofGenerated(anonymousClass));

        var expectedMsg = "Missing canonical name for class: " + anonymousClass.getName();
        assertEquals(expectedMsg, exception.getMessage(), "Exception message should match exactly");
    }

    public record CityInner(String name) {}
}