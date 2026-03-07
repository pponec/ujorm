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
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        getLog().info("Spouštím Ujorm3 MetaGenerator (testScope=" + testScope + ")...");

        var outputDirPath = testScope
                ? project.getBuild().getDirectory() + "/generated-test-sources/ujorm"
                : project.getBuild().getDirectory() + "/generated-sources/ujorm";

        var outputDirectory = new File(outputDirPath);

        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            throw new MojoExecutionException("Nelze vytvořit výstupní složku: " + outputDirectory);
        }

        if (testScope) {
            project.addTestCompileSourceRoot(outputDirectory.getAbsolutePath());
        } else {
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        }

        var classesDirPath = testScope
                ? project.getBuild().getTestOutputDirectory()
                : project.getBuild().getOutputDirectory();

        var classesDir = Paths.get(classesDirPath);

        if (!Files.exists(classesDir)) {
            getLog().info("Složka s classes neexistuje, přeskočeno: " + classesDir);
            return;
        }

        try (var classLoader = ClassLoaderBuilder.build(project, this.getClass().getClassLoader(), testScope)) {
            try (var paths = Files.walk(classesDir)) {

                // Změna: Místo forEach použijeme filter a count pro sečtení úspěšných generování
                long generatedCount = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".class"))
                        .filter(path -> processClassFile(path, classesDir, classLoader, outputDirectory))
                        .count();

                // Závěrečné logování s barvami / formátováním Mavenu
                String scopeName = testScope ? "TEST" : "MAIN";
                getLog().info("---------------------------------------------------------");
                getLog().info(String.format("Ujorm3 [%s]: Vygenerováno %d metamodelů.", scopeName, generatedCount));
                getLog().info("---------------------------------------------------------");

            }
        } catch (Exception e) {
            throw new MojoExecutionException("Chyba při skenování tříd", e);
        }
    }

    /**
     * Zpracuje soubor. Vrací true, pokud byl metamodel úspěšně vygenerován, jinak false.
     */
    private boolean processClassFile(Path classFile, Path classesRoot, ClassLoader classLoader, File outputDirectory) {
        var relativePath = classesRoot.relativize(classFile).toString();
        var className = relativePath.replace(File.separatorChar, '.').replace(".class", "");

        try {
            var clazz = classLoader.loadClass(className);

            var isEntity = Stream.of(clazz.getAnnotations())
                    .anyMatch(a -> a.annotationType().getSimpleName().equals("Table")
                            || a.annotationType().getSimpleName().equals("Entity"));

            if (isEntity) {
                getLog().debug("Generuji metamodel pro entitu: " + clazz.getSimpleName());
                var targetPackage = clazz.getPackageName() + ".meta";
                var sourceCode = SourceGenerator.generate(clazz, prefix, suffix, targetPackage);

                // Pokud metoda saveSourceCode nevyhodí výjimku, vracíme true
                saveSourceCode(clazz.getSimpleName(), targetPackage, sourceCode, outputDirectory);
                return true;
            }
        } catch (Throwable e) {
            getLog().debug("Přeskočena třída " + className + ": " + e.getMessage());
        }
        return false;
    }
    private void saveSourceCode(String originalName, String targetPackage, String sourceCode, File outputDirectory) {
        try {
            var newClassName = prefix + originalName + suffix;
            var packageDir = outputDirectory.toPath().resolve(targetPackage.replace('.', File.separatorChar));
            Files.createDirectories(packageDir);

            var sourceFile = packageDir.resolve(newClassName + ".java");
            Files.writeString(sourceFile, sourceCode);
        } catch (IOException e) {
            getLog().error("Chyba zápisu souboru pro " + originalName, e);
        }
    }

    // --- Vnitřní třídy pro organizaci logiky ---

    /** Generates the source code text. */
    private static class SourceGenerator {
        static String generate(Class<?> clazz, String prefix, String suffix, String targetPackage) {
            var originalName = clazz.getSimpleName();
            var newClassName = prefix + originalName + suffix;

            var sb = new StringBuilder();
            sb.append("package ").append(targetPackage).append(";\n\n");
            sb.append("import ").append(clazz.getName()).append(";\n");
            sb.append("import org.ujorm.Key;\n");
            sb.append("import org.ujorm.mapper.core.DomainHandler;\n");
            sb.append("import org.ujorm.mapper.core.DomainHandlerProvider;\n\n");

            sb.append("/** Auto-generated metamodel pro ").append(originalName).append(" */\n");
            sb.append("public class ").append(newClassName).append(" {\n\n");

            sb.append("    public static final DomainHandler<").append(originalName)
                    .append("> meta = DomainHandlerProvider.getHandler(").append(originalName).append(".class);\n\n");

            for (var field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                var fieldName = field.getName();
                var typeName = field.getType().getSimpleName();

                sb.append("    public static final Key<").append(originalName).append(", ").append(typeName).append("> ")
                        .append(fieldName).append(" = meta.getKey(\"").append(fieldName).append("\", ").append(typeName).append(".class);\n");
            }

            sb.append("}\n");
            return sb.toString();
        }
    }

    /** Builds URLClassLoader safely. */
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