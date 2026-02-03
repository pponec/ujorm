package org.ujorm.tools.converter;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlToJavaConverterTest {
    private final boolean printHtmlResult = false;
    private final String metaCharset = "  <meta charset=\"UTF-8\">\n";

    @Test
    void testConvertHtmlToJavaElements_FluentStyle() throws Exception {
        assertEquals(getHtml(), false);
    }

    @Test
    void testConvertHtmlToJavaElements_BlockStyle() throws Exception {
        assertEquals(getHtml(), true);
    }

    private String getHtml() {
        return """
                <!DOCTYPE html>
                <html lang="cs">
                <head>
                    <title>Test</title>
                </head>
                <body>
                    <h2 class="c1 c2">Title</h2>
                    <div class="container main" id="outer">
                        <div id="inner">
                            <my-custom-tag class="custom" my-custom-attribute="c"></my-custom-tag>
                            <script>
                               console.log('test1' + ' very very very long text;');
                               console.log('test2' + ' very very very long text;');
                            </script>
                        </div>
                        <form>
                            <label for="in">First name</label>
                            <input type="text" name="firstname" id="in">
                        </form>
                        <form2>
                            <label for="in">First name:<input type="text" name="firstname" id="in">
                            </label>
                        </form2>
                        <a class="niceClass linkClass" href="http://ujorm.org">The Ujorm</a>
                        <a href="http://test.org">The Test</a>
                        <p>Welcome user.<br>Please visit <a href="#">our website</a>.</p>
                        <img src="image.png" alt="Sample">
                        <ul><li>Unordered item</li></ul>
                        <ol><li>Ordered item 1</li><li>Ordered item 2</li></ol>
                        <pre>print("Hello World");</pre>
                        <table>
                          <thead><tr><td>Header Cell</td></tr></thead>
                          <tbody><tr><td>Data Cell</td></tr></tbody>
                        </table>
                    </div>
                </body>
                </html>
                """;
    }


    @Test
    void testStyle() throws Exception {
        HtmlToJavaConverter converter = new HtmlToJavaConverter();
        String result = converter.convertHtmlToJavaElements(getHtml(), false);

        assertTrue(result.contains("html.setAttribute(Html.A_LANG, \"cs\");"));
        assertTrue(result.contains("body.addHeadingX(2, \"c1\", \"c2\")"));
        assertTrue(result.contains("try (var div = body.addDiv(\"container\", \"main\"))"));
        assertTrue(result.contains("div2.addElement(\"my-custom-tag\", \"custom\")"));
        assertTrue(result.contains(".setAttribute(\"my-custom-attribute\", \"c\")"));
        assertTrue(result.contains("console.log('test1' + ' very very very long text;');"));

        assertTrue(result.contains("var form = div.addForm()"));
        assertTrue(result.contains("form.addInput()"));
        assertTrue(result.contains(".setFor(\"in\")"));
        assertTrue(result.contains(".setFor(\"in\")"));
        assertTrue(result.contains("form.addInput()"));
        assertTrue(result.contains(".setType(\"text\")"));
        assertTrue(result.contains(".setName(\"firstname\")"));
        assertTrue(result.contains(".setId(\"in\");"));
        assertTrue(result.contains("div.addAnchor(\"http://ujorm.org\", \"niceClass\", \"linkClass\""));

        assertTrue(result.contains("var p = div.addParagraph()"));
        assertTrue(result.contains("p.addBreak();"));
        assertTrue(result.contains("p.addAnchor(\"#\")"));
        assertTrue(result.contains("div.addImg()"));
        assertTrue(result.contains("var ul = div.addUnorderedlist()"));
        assertTrue(result.contains("ul.addListItem()"));
        assertTrue(result.contains("var ol = div.addOrderedList()"));
        assertTrue(result.contains("div.addPreformatted()"));
        assertTrue(result.contains("var table = div.addTable()"));
        assertTrue(result.contains("var thead = table.addTableHead()"));
        assertTrue(result.contains("var tbody = table.addTableBody()"));
        assertTrue(result.contains("var tr = thead.addTableRow()"));
        assertTrue(result.contains("tr.addTableDetail()"));
    }

    private void assertEquals(String htmlInput, boolean blockStyle) throws Exception {
        Document expectedModel = Jsoup.parse(htmlInput);

        // 3. Generate Java code
        HtmlToJavaConverter converter = new HtmlToJavaConverter();
        String methodBody = converter.convertHtmlToJavaElements(htmlInput, blockStyle);

        // 4. Build complete class for compilation (imports + class wrapper)
        String javaClassName = "GeneratedHtmlTest";
        String fullClassCode = wrapMethodInClass(javaClassName, methodBody);

        // Debug: Output to check what we are compiling
        System.out.println("--- START Compiling Code (%s) ---".formatted(blockStyle ? "block style" : "fluent"));
        System.out.println(fullClassCode);
        System.out.println("--- END Compiling Code (%s) -----".formatted(blockStyle ? "block style" : "fluent"));

        // 5. Dynamic compilation and execution
        String htmlResult = compileAndRun(fullClassCode, javaClassName, "htmlGenerator");

        // 6. Build result into a model (Jsoup Document)
        Document actualModel = Jsoup.parse(htmlResult);
        expectedModel.outputSettings().prettyPrint(true);
        actualModel.outputSettings().prettyPrint(true);

        // 7. Print HTML
        if (printHtmlResult) {
            System.out.println("--- Result HTML ---");
            System.out.println(actualModel.outerHtml());
        }

        // 8. Comparison
        Assertions.assertEquals(
                expectedModel.outerHtml(),
                actualModel.outerHtml().replace(metaCharset, ""),
                "Generated HTML model does not match input model");

        // 9. Check empty lines:
        long emptyLineCount = methodBody.lines()
                .filter(line -> line.trim().isEmpty())
                .count();
        Assertions.assertEquals(0, emptyLineCount, "Expected no zero lines");
    }

    /**
     * Wraps the generated method into a Java class structure with necessary imports.
     */
    private String wrapMethodInClass(String className, String methodBody) {
        return """
            import org.ujorm.tools.web.*;
            import org.ujorm.tools.web.ao.*;
            import org.ujorm.tools.web.request.*;
            
            public class %s {
            %s
            }
            """.formatted(className, methodBody);
    }

    /**
     * Helper method for in-memory compilation and execution.
     */
    private String compileAndRun(String sourceCode, String className, String methodName) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // Save bytecode to memory
        InMemoryJavaFileManager memFileManager = new InMemoryJavaFileManager(fileManager);
        JavaFileObject source = new StringJavaFileObject(className, sourceCode);

        // Get current runtime classpath (so the compiler sees Ujorm and Jsoup libraries)
        // Note: In some complex build systems, this might require explicit configuration
        Iterable<String> options = List.of("-classpath", System.getProperty("java.class.path"));

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                memFileManager,
                null,
                options,
                null,
                Collections.singletonList(source)
        );

        if (!task.call()) {
            throw new RuntimeException("Compilation failed.");
        }

        // Load compiled class
        ClassLoader classLoader = memFileManager.getClassLoader(null);
        Class<?> clazz = classLoader.loadClass(className);
        Object instance = clazz.getDeclaredConstructor().newInstance();
        Method method = clazz.getMethod(methodName);

        // Execution
        return (String) method.invoke(instance);
    }

    // --- Helper classes for In-Memory compilation (Boilerplate) ---

    private static class StringJavaFileObject extends SimpleJavaFileObject {
        private final String code;

        protected StringJavaFileObject(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    private static class InMemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final ClassLoaderOutput classLoaderOutput;

        protected InMemoryJavaFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
            this.classLoaderOutput = new ClassLoaderOutput();
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
            return classLoaderOutput.createFileObject(className);
        }

        @Override
        public ClassLoader getClassLoader(Location location) {
            return classLoaderOutput;
        }
    }

    private static class ClassLoaderOutput extends ClassLoader {
        private final java.util.Map<String, ByteArrayClassFile> classes = new java.util.HashMap<>();

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (classes.containsKey(name)) {
                byte[] bytes = classes.get(name).getBytes();
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }

        public JavaFileObject createFileObject(String className) {
            ByteArrayClassFile file = new ByteArrayClassFile(className);
            classes.put(className, file);
            return file;
        }
    }

    private static class ByteArrayClassFile extends SimpleJavaFileObject {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        protected ByteArrayClassFile(String name) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.CLASS.extension), Kind.CLASS);
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return out;
        }

        public byte[] getBytes() {
            return out.toByteArray();
        }
    }
}