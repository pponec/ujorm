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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A build-time tool that generates the domain handlers ahead of time, so the runtime needs
 * no Java compiler at all.
 * <p>
 * The tool runs after the {@code compile} phase, hence it inspects the <b>compiled</b> domain
 * classes by common reflection and reuses the very same code path as the runtime does:
 * {@link DomainModel#of(Class)} &rarr; {@link JavaSourceGenerator} &rarr; {@link ClassGenerator}.
 * There is no second model of an entity to keep in sync, so the result is identical to the class
 * the runtime would have built on the first use of the entity.
 * <p>
 * The list of the entities comes from the {@value #ENTITY_INDEX} resource written by the Ujorm
 * annotation processor, or from an explicit argument.
 * <p>
 * Beside the class files the tool writes the GraalVM reflection metadata, because
 * {@link ClassName#classForName(ClassLoader)} looks the handler up by a calculated name,
 * which the native image analysis can not see.
 *
 * <h2>Command line</h2>
 * <pre>{@code
 * java -cp <projectClasspath> org.ujorm.core.generator.HandlerPrecompiler \
 *      target/classes target/generated-sources/ujorm-handlers [entityClass ...]
 * }</pre>
 */
public class HandlerPrecompiler {

    /** Resource with the entity class names, one per line, written by the Ujorm annotation processor. */
    public static final String ENTITY_INDEX = "META-INF/ujorm/entities.lst";

    /** Resource with the GraalVM reflection metadata of the generated handlers. */
    public static final String REFLECT_CONFIG = "META-INF/native-image/org.ujorm/ujo-core/reflect-config.json";

    /** The minimal Java release supported by Ujorm. */
    static final int MIN_JAVA_RELEASE = 17;

    /** Bytes of the class file header up to the major version: magic, minor and major version. */
    private static final int CLASS_HEADER_SIZE = 8;

    /** Difference between the class file major version and the Java release, e.g. 61 - 44 = 17. */
    private static final int CLASS_MAJOR_OFFSET = 44;

    /** A class loader that sees the compiled domain classes and their dependencies. */
    @NotNull
    private final ClassLoader classLoader;

    public HandlerPrecompiler(@NotNull ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Generates a handler of each entity.
     * <p>The context class loader is switched for the compilation, because {@link ClassGenerator}
     * derives the compiler class path from it.
     *
     * @param entityNames Canonical names of the domain classes.
     * @param javaOutputDir A directory for the generated source code, {@code null} to skip it.
     *        The directory must <b>not</b> be registered as a source root, else the handlers
     *        would be compiled twice.
     * @param classOutputDir The root directory of the class files, typically {@code target/classes}.
     * @return Canonical names of the generated handler classes.
     */
    @NotNull
    public List<String> generate(
            @NotNull List<String> entityNames,
            @Nullable Path javaOutputDir,
            @NotNull Path classOutputDir) {

        var generator = new JavaSourceGenerator();
        var classGenerator = new ClassGenerator();
        var handlerNames = new ArrayList<String>(entityNames.size());
        var currentThread = Thread.currentThread();
        var originalLoader = currentThread.getContextClassLoader();

        currentThread.setContextClassLoader(classLoader);
        try {
            for (var entityName : entityNames) {
                var domainClass = loadClass(entityName);
                var handlerName = ClassName.ofGenerated(domainClass);
                var sourceCode = generator.getSourceCode(DomainModel.of(domainClass), handlerName);

                writeSource(sourceCode, handlerName, javaOutputDir);
                classGenerator.compileToDirectory(sourceCode, handlerName, classOutputDir, javaRelease(domainClass));
                handlerNames.add(handlerName.toString());
            }
        } finally {
            currentThread.setContextClassLoader(originalLoader);
        }
        writeReflectConfig(handlerNames, classOutputDir);
        return handlerNames;
    }

    /**
     * Reads the target Java release from the class file of the domain class, so the handler
     * gets the very same bytecode version as the entity it serves.
     * <p>
     * A build JDK is often newer than the runtime one, and the default bytecode version of the
     * compiler would then be unreadable on the target JVM. The runtime compilation has no such
     * problem, because it runs on the target JVM itself.
     *
     * @return The release between the minimal one supported by Ujorm and the running JDK.
     */
    private int javaRelease(@NotNull Class<?> domainClass) {
        var resource = domainClass.getName().replace('.', '/') + ".class";
        try (var input = classLoader.getResourceAsStream(resource)) {
            if (input != null) {
                var header = input.readNBytes(CLASS_HEADER_SIZE);
                if (header.length == CLASS_HEADER_SIZE) {
                    var major = ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
                    return clampRelease(major - CLASS_MAJOR_OFFSET);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the class file: " + resource, e);
        }
        return clampRelease(MIN_JAVA_RELEASE);
    }

    /** Loads a domain class by the dedicated class loader. */
    @NotNull
    private Class<?> loadClass(@NotNull String entityName) {
        try {
            return Class.forName(entityName, false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Entity class is not available: " + entityName, e);
        }
    }

    /** Writes the generated source code for a later inspection. */
    private void writeSource(@NotNull String sourceCode, @NotNull ClassName handlerName, @Nullable Path javaOutputDir) {
        if (javaOutputDir == null) {
            return;
        }
        var target = javaOutputDir
                .resolve(handlerName.packageName().replace('.', '/'))
                .resolve(handlerName.className() + ".java");
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, sourceCode, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write the source code: " + target, e);
        }
    }

    /**
     * Writes the GraalVM reflection metadata of the generated handlers.
     * <p>The runtime resolves a handler by {@code Class.forName()} and instantiates it by
     * its public constructor, so both must be registered for a native image.
     */
    private void writeReflectConfig(@NotNull List<String> handlerNames, @NotNull Path classOutputDir) {
        var target = classOutputDir.resolve(REFLECT_CONFIG);
        var content = handlerNames.stream()
                .map(name -> ("""
                          {
                            "name": "%s",
                            "methods": [{ "name": "<init>", "parameterTypes": [] }]
                          }""").formatted(name))
                .collect(Collectors.joining(",\n", "[\n", "\n]\n"));
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write the reflection metadata: " + target, e);
        }
    }

    /**
     * Reads the entity index written by the Ujorm annotation processor.
     * Blank lines and lines commented by the {@code #} character are skipped.
     *
     * @param classOutputDir The root directory of the class files.
     * @return Canonical names of the domain classes, an empty list for a missing index.
     */
    @NotNull
    public static List<String> readEntityIndex(@NotNull Path classOutputDir) {
        var source = classOutputDir.resolve(ENTITY_INDEX);
        if (!Files.isRegularFile(source)) {
            return List.of();
        }
        try {
            return Files.readAllLines(source, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read the entity index: " + source, e);
        }
    }

    /** Limits the release to the range supported by both Ujorm and the running JDK. */
    static int clampRelease(int release) {
        return Math.max(MIN_JAVA_RELEASE, Math.min(release, Runtime.version().feature()));
    }

    /** Creates a class loader that sees the compiled domain classes. */
    @NotNull
    public static ClassLoader classLoaderOf(@NotNull Path classOutputDir) {
        try {
            var url = classOutputDir.toUri().toURL();
            return new URLClassLoader(new URL[] {url}, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid directory: " + classOutputDir, e);
        }
    }

    /**
     * Command line entry point.
     *
     * @param args {@code <classOutputDir> <javaOutputDir|-> [entityClass ...]}, where the entity
     *        classes default to the content of the {@value #ENTITY_INDEX} resource.
     */
    public static void main(@NotNull String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Usage: " + HandlerPrecompiler.class.getName()
                    + " <classOutputDir> <javaOutputDir|-> [entityClass ...]");
        }
        var classOutputDir = Path.of(args[0]);
        var javaOutputDir = "-".equals(args[1]) ? null : Path.of(args[1]);
        var entityNames = args.length > 2
                ? List.of(args).subList(2, args.length)
                : readEntityIndex(classOutputDir);

        if (entityNames.isEmpty()) {
            System.out.println("Ujorm: no entity found, nothing to pre-compile.");
            return;
        }
        var handlers = new HandlerPrecompiler(classLoaderOf(classOutputDir))
                .generate(entityNames, javaOutputDir, classOutputDir);
        System.out.println("Ujorm: pre-compiled %s domain handler(s).".formatted(handlers.size()));
    }
}
