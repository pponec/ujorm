package org.ujorm.mapper.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

class ClassGeneratorTest {

    private final int loopCount = 10;

    // @Test
    void testDynamicIncrementerMany() throws Exception {
        for (int i = 0; i < loopCount; i++) {
            testDynamicIncrementer();
        }
    }

    @Test
    void testDynamicIncrementer() throws Exception {
        // 1. Define class metadata to avoid duplication in the template
        var packageName = "com.example.dynamic";
        var simpleName = "Incrementer";
        var canonicalName = new ClassName(packageName, simpleName);

        // 2. Prepare source code using text block formatting
        var sourceCode = """
            package %s;
            import java.util.function.Function;

            public class %s implements Function<Integer, Integer> {
                @Override
                public Integer apply(Integer i) {
                    if (i < 0) throw new IllegalArgumentException("Input cannot be negative");
                    return i + 1;
                }
            }
            """.formatted(packageName, simpleName);

        // 3. Compile and load the class in-memory
        var provider = new ClassGenerator();
        var dynamicClass = provider.createClass(sourceCode, canonicalName);

        // 4. Instantiate and cast to the known interface
        // We use getDeclaredConstructor().newInstance() as Class.newInstance() is deprecated.
        @SuppressWarnings("unchecked")
        var incrementer = (Function<Integer, Integer>) dynamicClass.getDeclaredConstructor().newInstance();

        // 5. Verify successful execution
        assertEquals(11, incrementer.apply(10));

        // 6. Verify exception handling
        assertThrows(IllegalArgumentException.class, () -> incrementer.apply(-1));
    }

}

