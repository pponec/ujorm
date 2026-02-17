package org.ujorm.mapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.tools.*;

public class ClassGenerator {

    /**
     * Compiles the given source code in-memory and loads the resulting class.
     * <p>
     * The method uses the current thread's context class loader as the parent,
     * ensuring the dynamic class can see interfaces and classes from the main application.
     *
     * @param sourceCode         The Java source code.
     * @return The compiled and loaded Class object.
     * @throws RuntimeException if compilation fails (includes compiler error messages).
     */
    public Class<?> createClass(String sourceCode) {
        var canonicalClassName = getCanonicalClassName(sourceCode);
        return createClass(sourceCode, canonicalClassName);
    }

    /**
     * Extracts the canonical class name using a more concise approach.
     */
    protected String getCanonicalClassName(String sourceCode) {
        var pkgMatcher = Pattern.compile("package\\s+([\\w.]+)\\s*;").matcher(sourceCode);
        if (!pkgMatcher.find()) {
            throw new IllegalStateException("Source code does not contain a package declaration.");
        }
        var classMatcher = Pattern.compile("public\\s+.*?class\\s+([a-zA-Z_$][a-zA-Z\\d_$]*)").matcher(sourceCode);
        if (!classMatcher.find()) {
            throw new IllegalStateException("Source code does not contain a public class declaration.");
        }
        return pkgMatcher.group(1) + "." + classMatcher.group(1);
    }

    /**
     * Compiles the given source code in-memory and loads the resulting class.
     * <p>
     * The method uses the current thread's context class loader as the parent,
     * ensuring the dynamic class can see interfaces and classes from the main application.
     *
     * @param sourceCode         The Java source code.
     * @param canonicalClassName The fully qualified name of the class (e.g., "com.example.MyClass").
     * @return The compiled and loaded Class object.
     * @throws RuntimeException if compilation fails (includes compiler error messages).
     */
    public Class<?> createClass(String sourceCode, String canonicalClassName) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Java Compiler unavailable. Ensure you are running with a JDK.");
        }

        var classBytes = new HashMap<String, byte[]>();
        var diagnostics = new DiagnosticCollector<JavaFileObject>();

        try (var standardManager = compiler.getStandardFileManager(diagnostics, null, null);
             var fileManager = createCapturingFileManager(standardManager, classBytes)) {

            compile(compiler, fileManager, diagnostics, sourceCode, canonicalClassName);
            return loadClass(classBytes, canonicalClassName);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to create class: " + canonicalClassName, e);
        }
    }

    /**
     * Creates a file manager wrapper that intercepts compiled bytecode and stores it in a Map.
     */
    private JavaFileManager createCapturingFileManager(StandardJavaFileManager delegate, Map<String, byte[]> classBytes) {
        return new ForwardingJavaFileManager<>(delegate) {
            @Override
            public JavaFileObject getJavaFileForOutput(Location loc, String className, JavaFileObject.Kind kind, FileObject sibling) {
                return new SimpleJavaFileObject(URI.create("mem:///" + className), kind) {
                    @Override
                    public OutputStream openOutputStream() {
                        return new ByteArrayOutputStream() {
                            @Override
                            public void close() throws IOException {
                                super.close();
                                classBytes.put(className, toByteArray());
                            }
                        };
                    }
                };
            }
        };
    }

    /**
     * Executes the compilation task. Throws RuntimeException with detailed diagnostics on failure.
     */
    private void compile(JavaCompiler compiler, JavaFileManager fileManager,
                         DiagnosticCollector<JavaFileObject> diagnostics,
                         String sourceCode, String canonicalClassName) {

        var sourceObject = new SimpleJavaFileObject(URI.create("string:///" + canonicalClassName.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceCode;
            }
        };

        var task = compiler.getTask(null, fileManager, diagnostics, null, null, Collections.singletonList(sourceObject));

        if (!Boolean.TRUE.equals(task.call())) {
            var errorMsg = diagnostics.getDiagnostics().stream()
                    .map(d -> String.format(Locale.ENGLISH, "Line %d: %s", d.getLineNumber(), d.getMessage(Locale.ENGLISH)))
                    .collect(Collectors.joining("\n"));
            throw new RuntimeException("Compilation failed for " + canonicalClassName + ":\n" + errorMsg);
        }
    }

    /**
     * Loads the class from the bytecode map using a dedicated ephemeral ClassLoader.
     */
    private Class<?> loadClass(Map<String, byte[]> classBytes, String canonicalClassName) throws ClassNotFoundException {
        var loader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                var bytes = classBytes.get(name);
                if (bytes == null) {
                    return super.findClass(name);
                }
                return defineClass(name, bytes, 0, bytes.length);
            }
        };
        return loader.loadClass(canonicalClassName);
    }
}


