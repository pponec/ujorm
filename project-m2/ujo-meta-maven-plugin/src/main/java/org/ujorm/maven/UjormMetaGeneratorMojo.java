package org.ujorm.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Generates Ujorm metamodel from compiled entities using reflection. */
@Mojo(name = "generate-meta",
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class UjormMetaGeneratorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "Meta")
    private String prefix;

    @Parameter(defaultValue = "")
    private String suffix;

    @Parameter(defaultValue = "false")
    private boolean testScope;

    @Override
    public void execute() throws MojoExecutionException {
        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";
        getLog().info("Ujorm3 MetaGenerator is running (testScope=" + testScope + ")...");

        // Define output path based on the scope
        var outputDirPath = testScope
                ? project.getBuild().getDirectory() + "/generated-test-sources/ujorm"
                : project.getBuild().getDirectory() + "/generated-sources/ujorm";

        var outputDirectory = new File(outputDirPath);

        // Ensure the output directory exists
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new MojoExecutionException("Could not create output directory: " + outputDirectory);
        }

        // Register the generated source directory in the Maven project
        if (testScope) {
            project.addTestCompileSourceRoot(outputDirectory.getAbsolutePath());
        } else {
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        }

        // Define which classes to scan
        var classesDirPath = testScope
                ? project.getBuild().getTestOutputDirectory()
                : project.getBuild().getOutputDirectory();

        var classesDir = Paths.get(classesDirPath);

        if (!Files.exists(classesDir)) {
            getLog().info("Classes directory does not exist, skipping: " + classesDir);
            return;
        }

        try (var classLoader = ClassLoaderBuilder.build(project, this.getClass().getClassLoader(), testScope)) {
            try (var paths = Files.walk(classesDir)) {

                // Filter class files and count successful generations
                long generatedCount = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".class"))
                        .filter(path -> processClassFile(path, classesDir, classLoader, outputDirectory))
                        .count();

                // Final logging using Maven formatting
                String scopeName = testScope ? "TEST" : "MAIN";
                getLog().info("---------------------------------------------------------");
                getLog().info(String.format("Ujorm3 [%s]: Generated %d metamodels.", scopeName, generatedCount));
                getLog().info("---------------------------------------------------------");

            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error during class scanning", e);
        }
    }

    /**
     * Processes a single class file.
     * @return true if the metamodel was successfully generated, otherwise false.
     */
    private boolean processClassFile(Path classFile, Path classesRoot, ClassLoader classLoader, File outputDirectory) {
        var relativePath = classesRoot.relativize(classFile).toString();
        var className = relativePath.replace(File.separatorChar, '.').replace(".class", "");

        try {
            var clazz = classLoader.loadClass(className);

            // Check if the class is marked as an Entity or Table
            var isEntity = Stream.of(clazz.getAnnotations())
                    .anyMatch(a -> a.annotationType().getSimpleName().equals("Table")
                            || a.annotationType().getSimpleName().equals("Entity"));

            if (isEntity) {
                getLog().debug("Generating metamodel for entity: " + clazz.getSimpleName());
                var targetPackage = clazz.getPackageName() + ".meta";
                var sourceCode = SourceGenerator.generate(clazz, prefix, suffix, targetPackage);

                // Save the source code and return success
                saveSourceCode(clazz.getSimpleName(), targetPackage, sourceCode, outputDirectory);
                return true;
            }
        } catch (Throwable e) {
            getLog().debug("Skipped class " + className + ": " + e.getMessage());
        }
        return false;
    }

    /** Saves the generated source code to the file system. */
    private void saveSourceCode(String originalName, String targetPackage, String sourceCode, File outputDirectory) {
        try {
            var newClassName = prefix + originalName + suffix;
            var packageDir = outputDirectory.toPath().resolve(targetPackage.replace('.', File.separatorChar));
            Files.createDirectories(packageDir);

            var sourceFile = packageDir.resolve(newClassName + ".java");
            Files.writeString(sourceFile, sourceCode);
        } catch (IOException e) {
            getLog().error("Error writing source file for " + originalName, e);
        }
    }

    // --- Internal helper classes for logic organization ---

    /** Internal class for generating the source code text */
    private static class SourceGenerator {

        /** Converts primitive types to their object wrappers. */
        private static Class<?> getWrapperType(Class<?> clazz) {
            if (!clazz.isPrimitive()) return clazz;
            if (clazz == int.class) return Integer.class;
            if (clazz == long.class) return Long.class;
            if (clazz == boolean.class) return Boolean.class;
            if (clazz == double.class) return Double.class;
            if (clazz == float.class) return Float.class;
            if (clazz == short.class) return Short.class;
            if (clazz == byte.class) return Byte.class;
            if (clazz == char.class) return Character.class;
            return clazz;
        }

        /** Formats the type name for the generated code. */
        private static String getTypeName(Class<?> clazz) {
            var wrapperClass = getWrapperType(clazz);
            var result = wrapperClass.getCanonicalName() != null ? wrapperClass.getCanonicalName() : wrapperClass.getSimpleName();
            return result.startsWith("java.lang.") ? result.substring(10) : result;
        }

        private static boolean hasTransientAnnotation(Field field) {
            return Stream.of(field.getAnnotations())
                    .anyMatch(a -> a.annotationType().getSimpleName().equals("Transient"));
        }

        private static boolean hasValidGetterAndSetter(Class<?> beanClass, Field field) {
            var suffix = capitalize(field.getName());
            var hasGetter = false;
            var hasSetter = false;

            if (field.getType() == boolean.class) {
                hasGetter = hasMethod(beanClass, "is" + suffix);
            }
            if (!hasGetter) {
                hasGetter = hasMethod(beanClass, "get" + suffix);
            }

            if (hasGetter) {
                hasSetter = hasMethod(beanClass, "set" + suffix, field.getType());
            }

            return hasGetter && hasSetter;
        }

        private static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
            try {
                clazz.getMethod(methodName, parameterTypes);
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }

        private static String capitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }

        public static String generate(Class<?> clazz, String prefix, String suffix, String targetPackage) {
            var entityCanonicalName = clazz.getCanonicalName();
            var originalName = clazz.getSimpleName();
            var newClassName = prefix + originalName + suffix;
            var isRecord = clazz.isRecord();

            var result = new StringBuilder();
            result.append("package ").append(targetPackage).append(";\n\n");

            if (entityCanonicalName != null) {
                result.append("import ").append(entityCanonicalName).append(";\n");
            }

            result.append("import org.ujorm.Key;\n");
            result.append("import org.ujorm.DomainHandler;\n");
            result.append("import org.ujorm.core.DomainHandlerProvider;\n\n");

            result.append("/** Auto-generated metamodel for ").append(originalName).append(" */\n");
            result.append("public class ").append(newClassName).append(" {\n\n");

            result.append("    public static final DomainHandler<").append(originalName)
                    .append("> meta = DomainHandlerProvider.getHandler(").append(originalName).append(".class);\n\n");

            var validFields = new ArrayList<Field>();

            if (isRecord) {
                for (var component : clazz.getRecordComponents()) {
                    try {
                        var field = clazz.getDeclaredField(component.getName());
                        if (!hasTransientAnnotation(field)) {
                            validFields.add(field);
                        }
                    } catch (NoSuchFieldException e) {
                        // Ignore
                    }
                }
            } else {
                var currentClass = clazz;
                while (currentClass != null && currentClass != Object.class) {
                    var classFields = new ArrayList<Field>();
                    for (var field : currentClass.getDeclaredFields()) {
                        var mods = field.getModifiers();
                        if (Modifier.isStatic(mods) || Modifier.isTransient(mods) || hasTransientAnnotation(field) || field.isSynthetic()) {
                            continue;
                        }

                        if (hasValidGetterAndSetter(clazz, field)) {
                            classFields.add(field);
                        }
                    }

                    validFields.addAll(0, classFields);
                    currentClass = currentClass.getSuperclass();
                }
            }

            for (var field : validFields) {
                var fieldName = field.getName();
                var typeName = getTypeName(field.getType());

                if (isRecord) {
                    result.append("    /** The ").append(fieldName).append(" property */\n");
                }

                result.append("    public static final Key<").append(originalName).append(", ").append(typeName).append("> ")
                        .append(fieldName).append(" = meta.getKey(\"").append(fieldName).append("\");\n");
            }

            result.append("}\n");
            return result.toString();
        }
    }

    /** Builds a URLClassLoader to load classes from the project classpath. */
    private static class ClassLoaderBuilder {
        static URLClassLoader build(MavenProject project, ClassLoader parent, boolean testScope) throws DependencyResolutionRequiredException, IOException {
            var classpathElements = testScope
                    ? project.getTestClasspathElements()
                    : project.getCompileClasspathElements();

            var urls = new ArrayList<URL>();
            for (var element : classpathElements) {
                urls.add(new File(element).toURI().toURL());
            }
            return new URLClassLoader(urls.toArray(new URL[0]), parent);
        }
    }
}