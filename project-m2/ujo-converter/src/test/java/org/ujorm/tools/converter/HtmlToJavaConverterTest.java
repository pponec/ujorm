package org.ujorm.tools.converter;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.tools.Lines;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

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

    @Test
    void testConvertHtmlToJavaElements_DefaultTitleAndEscaping_Fluent() throws Exception {
        assertEquals(getEdgeCaseHtml(), false);
    }

    @Test
    void testConvertHtmlToJavaElements_DefaultTitleAndEscaping_Block() throws Exception {
        assertEquals(getEdgeCaseHtml(), true);
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

    private String getEdgeCaseHtml() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><title>Demo</title></head>
                <body>
                    <div id="x" data-val="back\\slash">Line 1
                Line 2</div>
                    <script>
                const q = "\"\"\"";
                const path = "c:\\tmp";
                    </script>
                </body>
                </html>
                """;
    }

    @Test
    void testStyle() throws Exception {
        var converter = new HtmlToJavaConverter();
        var result = converter.convertHtmlToJavaElements(getHtml(), false);
        var lines = Lines.ofQuoted(result);

        Assertions.assertTrue(lines.findLine("html.setAttribute(Html.A_LANG, 'cs');"));
        Assertions.assertTrue(lines.findLine("body.addHeadingX(2, 'c1', 'c2')"));
        Assertions.assertTrue(lines.findLine("try (var div = body.addDiv('container', 'main')) {"));
        Assertions.assertTrue(lines.findLine("div2.addElement('my-custom-tag', 'custom')"));
        Assertions.assertEquals(".setAttribute('my-custom-attribute', 'c');", lines.next().trim());
        Assertions.assertTrue(lines.findLine("console.log('test1' + ' very very very long text;');"));
        Assertions.assertTrue(lines.findLine("try (var form = div.addForm()) {"));
        Assertions.assertTrue(lines.findLine("form.addInput()"));
        Assertions.assertTrue(lines.findLine("label.setFor('in');"));
        Assertions.assertTrue(lines.findLine(".setType('text')"));
        Assertions.assertEquals(".setName('firstname')", lines.next().trim());
        Assertions.assertEquals(".setId('in');", lines.next().trim());
        Assertions.assertTrue(lines.findLine("div.addAnchor('http://ujorm.org', 'niceClass', 'linkClass')"));
        Assertions.assertTrue(lines.findLine("try (var p = div.addParagraph()) {"));
        Assertions.assertTrue(lines.findLine("p.addBreak();"));
        Assertions.assertTrue(lines.findLine("p.addAnchor('#')"));
        Assertions.assertTrue(lines.findLine("div.addImg()"));
        Assertions.assertTrue(lines.findLine("try (var ul = div.addUnorderedlist()) {"));
        Assertions.assertEquals("ul.addListItem()", lines.next().trim());
        Assertions.assertTrue(lines.findLine("try (var ol = div.addOrderedList()) {"));
        Assertions.assertTrue(lines.findLine("div.addPreformatted()"));
        Assertions.assertTrue(lines.findLine("try (var table = div.addTable()) {"));
        Assertions.assertTrue(lines.findLine("try (var thead = table.addTableHead()) {"));
        Assertions.assertTrue(lines.findLine("try (var tbody = table.addTableBody()) {"));
        Assertions.assertEquals("try (var tr = tbody.addTableRow()) {", lines.next().trim());
        Assertions.assertEquals("tr.addTableDetail()", lines.next().trim());
    }

    private void assertEquals(String htmlInput, boolean blockStyle) throws Exception {
        var expectedModel = Jsoup.parse(htmlInput);

        // 3. Generate Java code
        var converter = new HtmlToJavaConverter();
        var methodBody = converter.convertHtmlToJavaElements(htmlInput, blockStyle);

        // 4. Build complete class for compilation (imports + class wrapper)
        var javaClassName = "GeneratedHtmlTest";
        var fullClassCode = wrapMethodInClass(javaClassName, methodBody);

        // Debug: Output to check what we are compiling
        if (printHtmlResult) {
            System.out.printf("--- START Compiling Code (%s) ---%n", blockStyle ? "block style" : "fluent");
            System.out.println(fullClassCode);
            System.out.printf("--- END Compiling Code (%s) -----%n", blockStyle ? "block style" : "fluent");
        }

        // 5. Dynamic compilation and execution
        var htmlResult = compileAndRun(fullClassCode, javaClassName, "webPage");

        // 6. Build result into a model (Jsoup Document)
        var actualModel = Jsoup.parse(htmlResult);
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
        var compiler = ToolProvider.getSystemJavaCompiler();
        var fileManager = compiler.getStandardFileManager(null, null, null);

        // Save bytecode to memory
        var memFileManager = new InMemoryJavaFileManager(fileManager);
        var source = new StringJavaFileObject(className, sourceCode);

        // Get current runtime classpath (so the compiler sees Ujorm and Jsoup libraries)
        // Note: In some complex build systems, this might require explicit configuration
        Iterable<String> options = List.of("-classpath", System.getProperty("java.class.path"));

        var task = compiler.getTask(
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
        var classLoader = memFileManager.getClassLoader(null);
        var clazz = classLoader.loadClass(className);
        var instance = clazz.getDeclaredConstructor().newInstance();
        var method = clazz.getMethod(methodName);

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
                var bytes = classes.get(name).getBytes();
                return defineClass(name, bytes, 0, bytes.length);
            }
            return super.findClass(name);
        }

        public JavaFileObject createFileObject(String className) {
            var file = new ByteArrayClassFile(className);
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