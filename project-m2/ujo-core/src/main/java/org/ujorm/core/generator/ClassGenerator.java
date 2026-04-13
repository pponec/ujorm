/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.core.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.*;
import org.jetbrains.annotations.NotNull;

public class ClassGenerator {

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
    public Class<?> createClass(String sourceCode, ClassName canonicalClassName) {
        var compiler = ToolProvider.getSystemJavaCompiler();
        Objects.requireNonNull(compiler, "Java Compiler unavailable. Ensure you are running with a JDK.");
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

    /** Creates a file manager wrapper that intercepts compiled bytecode and stores it in a Map. */
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

    /** Executes the compilation task. Throws RuntimeException with detailed diagnostics on failure. */
    private void compile(JavaCompiler compiler,
                         JavaFileManager fileManager,
                         DiagnosticCollector<JavaFileObject> diagnostics,
                         String sourceCode,
                         ClassName canonicalClassName) {

        var uri = URI.create("string:///"
                + canonicalClassName.toString().replace('.', '/')
                + JavaFileObject.Kind.SOURCE.extension);

        var sourceObject = new SimpleJavaFileObject(uri, JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceCode;
            }
        };

        var options = getCompilerOptions();
        var task = compiler.getTask(null, fileManager, diagnostics, options, null, Collections.singletonList(sourceObject));

        if (!Boolean.TRUE.equals(task.call())) {
            var errorMsg = diagnostics.getDiagnostics().stream()
                    .map(d -> String.format(Locale.ENGLISH, "Line %d: %s", d.getLineNumber(), d.getMessage(Locale.ENGLISH)))
                    .collect(Collectors.joining("\n"));
            throw new RuntimeException("Compilation failed for " + canonicalClassName + ":\n" + errorMsg);
        }
    }

    /** Loads the class from the bytecode map using a dedicated ephemeral ClassLoader. */
    private Class<?> loadClass(Map<String, byte[]> classBytes, ClassName canonicalClassName) throws ClassNotFoundException {
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
        return loader.loadClass(canonicalClassName.toString());
    }

    /** Generates compiler options to explicitly define the classpath from the environment. */
    private List<String> getCompilerOptions() {
        var paths = Stream.of(System.getProperty("java.class.path", ""));
        var contextClassLoader = Thread.currentThread().getContextClassLoader();
        var pathSeparator = System.getProperty("path.separator");
        var classLoaders = Stream.iterate(contextClassLoader, c -> c != null, ClassLoader::getParent)
                .filter(URLClassLoader.class::isInstance)
                .map(URLClassLoader.class::cast)
                .flatMap(c -> Arrays.stream(c.getURLs()))
                .map(ClassGenerator::toFileName);
        var classPath = Stream.concat(paths, classLoaders)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.joining(pathSeparator));
        return classPath.isEmpty() ? List.of() : List.of("-classpath", classPath);
    }

    /** Converts URL to an absolute path safely using NIO. */
    private static @NotNull String toFileName(@NotNull URL url) {
        if (!"file".equalsIgnoreCase(url.getProtocol())) {
            return url.getFile();
        }
        try {
            return Paths.get(url.toURI()).toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            return Paths.get(url.getPath()).toAbsolutePath().toString();
        }
    }
}