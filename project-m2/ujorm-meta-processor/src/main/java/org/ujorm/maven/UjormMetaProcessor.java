package org.ujorm.maven;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import static org.ujorm.maven.UjormMetaProcessor.Const.*;

/** Generates metamodel from entities during the compilation phase using APT. */
@SupportedAnnotationTypes({
        "javax.persistence.Entity",
        "javax.persistence.Table",
        "jakarta.persistence.Entity",
        "jakarta.persistence.Table"
})
@SupportedOptions({
        PARAM_PREFIX,
        PARAM_SUFFIX,
        PARAM_META_PACKAGE
})
public class UjormMetaProcessor extends AbstractProcessor {
    private String prefix = "Meta";
    private String suffix = "";
    private String metaPackage = "";
    private final Set<String> processedClasses = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        var options = processingEnv.getOptions();

        if (options.containsKey(PARAM_PREFIX)) {
            var p = options.get(PARAM_PREFIX);
            if (p != null) prefix = p;
        }
        if (options.containsKey(PARAM_SUFFIX)) {
            var s = options.get(PARAM_SUFFIX);
            if (s != null) suffix = s;
        }
        if (options.containsKey(PARAM_META_PACKAGE)) {
            var p = options.get(PARAM_META_PACKAGE);
            if (p != null) metaPackage = p;
        }

        // Safety fallback against null values injected by Maven
        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";
        if (metaPackage == null) metaPackage = "";

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Ujorm3 APT Processor initialized.");
    }

    /** Dynamically supports whatever Java version the compiler is running. */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * Explicitly declares supported options to prevent javac warnings.
     * This is more reliable than the @SupportedOptions annotation.
     */
    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(PARAM_PREFIX, PARAM_SUFFIX, PARAM_META_PACKAGE);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (var element : roundEnv.getRootElements()) {
            scanElementRecursive(element);
        }
        return false;
    }

    /** Recursively scans elements to find nested classes annotated with @Entity or @Table. */
    private void scanElementRecursive(Element element) {
        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.RECORD) {
            if (hasEntityOrTableAnnotation(element)) {
                processClassElement((TypeElement) element);
            }
            for (var enclosed : element.getEnclosedElements()) {
                scanElementRecursive(enclosed);
            }
        }
    }

    /** Manually checks if the element has @Entity or @Table. */
    private boolean hasEntityOrTableAnnotation(Element element) {
        for (var mirror : element.getAnnotationMirrors()) {
            var annoName = mirror.getAnnotationType().asElement().getSimpleName().toString();
            if (annoName.equals("Entity") || annoName.equals("Table")) {
                return true;
            }
        }
        return false;
    }

    /** Processes a single annotated class or record and generates the metamodel source file. */
    private void processClassElement(TypeElement classElement) {
        var originalPackage = processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();

        // Relative package is supported only
        var subPackage = metaPackage.trim().replaceFirst("^\\.", "");
        var targetPackage = subPackage.isEmpty()
                ? originalPackage
                : originalPackage + "." + subPackage;

        var originalName = classElement.getSimpleName().toString();
        var newClassName = prefix + originalName + suffix;
        var fullClassName = targetPackage + "." + newClassName;

        // Prevent generating the same file multiple times across different processing rounds
        if (!processedClasses.add(fullClassName)) {
            return;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Ujorm3: Generating metamodel -> " + fullClassName);

        // Instantiate the generator as a regular object
        var generator = new SourceGenerator(processingEnv, prefix, suffix);
        var sourceCode = generator.generate(classElement, targetPackage);

        try {
            var sourceFile = processingEnv.getFiler().createSourceFile(fullClassName, classElement);
            try (var writer = sourceFile.openWriter()) {
                writer.write(sourceCode);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate metamodel: " + e.getMessage(), classElement);
        }
    }

    /** Inner class for generating the source code text using an object-oriented approach. */
    private record SourceGenerator(
            ProcessingEnvironment env,
            String prefix,
            String suffix) {

        /** Checks if the element has a @Transient annotation. */
        private boolean hasTransientAnnotation(Element element) {
            for (var mirror : element.getAnnotationMirrors()) {
                if (mirror.getAnnotationType().asElement().getSimpleName().toString().equals("Transient")) {
                    return true;
                }
            }
            return false;
        }

        /** Checks for valid getter and setter methods, including Lombok annotations support. */
        private boolean hasValidGetterAndSetter(TypeElement classElement, VariableElement field) {
            var fieldSuffix = capitalize(field.getSimpleName().toString());
            var hasExplicitGetter = false;
            var hasExplicitSetter = false;

            // 1. Check explicitly written methods
            var isBoolean = field.asType().getKind() == TypeKind.BOOLEAN;
            var getterName1 = "get" + fieldSuffix;
            var getterName2 = isBoolean ? "is" + fieldSuffix : getterName1;
            var setterName = "set" + fieldSuffix;

            var methods = ElementFilter.methodsIn(classElement.getEnclosedElements());
            for (var method : methods) {
                var name = method.getSimpleName().toString();
                if ((name.equals(getterName1) || name.equals(getterName2)) && method.getParameters().isEmpty()) {
                    hasExplicitGetter = true;
                }
                if (name.equals(setterName) && method.getParameters().size() == 1) {
                    hasExplicitSetter = true;
                }
            }

            // 2. Check Lombok annotations on the CLASS level (@Getter, @Setter, @Data)
            var classHasGetter = false;
            var classHasSetter = false;
            for (var am : classElement.getAnnotationMirrors()) {
                var annoName = am.getAnnotationType().asElement().getSimpleName().toString();
                if (annoName.equals("Getter") || annoName.equals("Data")) classHasGetter = true;
                if (annoName.equals("Setter") || annoName.equals("Data")) classHasSetter = true;
            }

            // 3. Check Lombok annotations on the FIELD level (overrides class level)
            var hasLombokGetter = classHasGetter;
            var hasLombokSetter = classHasSetter;
            for (var am : field.getAnnotationMirrors()) {
                var annoName = am.getAnnotationType().asElement().getSimpleName().toString();

                // Safe and exact way to read annotation values in APT
                var isNone = false;
                for (var entry : am.getElementValues().entrySet()) {
                    if (entry.getValue().toString().contains("NONE")) {
                        isNone = true;
                        break;
                    }
                }

                if (annoName.equals("Getter")) hasLombokGetter = !isNone;
                if (annoName.equals("Setter")) hasLombokSetter = !isNone;
            }

            return (hasExplicitGetter || hasLombokGetter) && (hasExplicitSetter || hasLombokSetter);
        }

        /** Formats the type name and handles primitive boxing for generics. */
        private String getTypeName(TypeMirror type) {
            var typeUtils = env.getTypeUtils();
            var objectType = type;

            if (type.getKind().isPrimitive()) {
                objectType = typeUtils.boxedClass((PrimitiveType) type).asType();
            }

            var typeElement = (TypeElement) typeUtils.asElement(objectType);
            if (typeElement == null) {
                return objectType.toString();
            }

            var result = getCanonicalName(typeElement);
            if (result.startsWith("java.lang.")) {
                return result.substring(10);
            }
            return result;
        }

        /** Constructs the canonical name manually to properly support nested classes. */
        private String getCanonicalName(TypeElement element) {
            var enclosing = element.getEnclosingElement();
            if (enclosing != null && enclosing.getKind() == ElementKind.CLASS) {
                return getCanonicalName((TypeElement) enclosing) + "." + element.getSimpleName();
            }
            return element.getQualifiedName().toString();
        }

        /** Capitalizes the first letter of the string. */
        private String capitalize(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }

        /** Generates the final Java source code string for the metamodel. */
        public String generate(TypeElement classElement, String targetPackage) {
            var canonicalName = getCanonicalName(classElement);
            var originalName = classElement.getSimpleName().toString();
            var newClassName = prefix + originalName + suffix;
            var isRecord = classElement.getKind() == ElementKind.RECORD;

            var result = new StringBuilder();
            result.append("package ").append(targetPackage).append(";\n\n");

            if (!canonicalName.isEmpty()) {
                result.append("import ").append(canonicalName).append(";\n");
            }

            result.append("import javax.annotation.processing.Generated;\n");
            result.append("import org.ujorm.core.Key;\n");
            result.append("import org.ujorm.core.DomainHandler;\n");
            result.append("import org.ujorm.core.DomainHandlerProvider;\n");
            result.append("import org.ujorm.orm.dsl.TableAlias;\n\n");

            result.append("/** Auto-generated metamodel for the {@code ").append(originalName).append("} domain class. */\n");
            result.append("@Generated(\"").append(getClass().getCanonicalName()).append("\")\n");
            result.append("public abstract class ").append(newClassName).append(" {\n\n");

            result.append("    private static final DomainHandler<").append(originalName)
                    .append("> meta = DomainHandlerProvider.getHandler(").append(originalName).append(".class);\n\n");

            var validFields = new ArrayList<VariableElement>();
            var currentClass = classElement;

            while (currentClass != null && !currentClass.getQualifiedName().toString().equals("java.lang.Object")) {
                var classFields = new ArrayList<VariableElement>();
                var enclosedFields = ElementFilter.fieldsIn(currentClass.getEnclosedElements());

                for (var field : enclosedFields) {
                    var mods = field.getModifiers();
                    if (mods.contains(Modifier.STATIC) || mods.contains(Modifier.TRANSIENT) || hasTransientAnnotation(field)) {
                        continue;
                    }

                    if (isRecord || hasValidGetterAndSetter(currentClass, field)) {
                        classFields.add(field);
                    }
                }

                validFields.addAll(0, classFields);

                if (isRecord) {
                    break;
                }

                var superclassMirror = currentClass.getSuperclass();
                if (superclassMirror.getKind() == TypeKind.DECLARED) {
                    currentClass = (TypeElement) ((DeclaredType) superclassMirror).asElement();
                } else {
                    currentClass = null;
                }
            }

            for (var field : validFields) {
                var fieldName = field.getSimpleName().toString();
                var typeName = getTypeName(field.asType());

                if (isRecord) {
                    result.append("    /** The {@code ").append(fieldName).append("} property descriptor */\n");
                }

                result.append("    public static final Key<").append(originalName).append(", ").append(typeName).append("> ")
                        .append(fieldName).append(" = meta.getKey(\"").append(fieldName).append("\");\n");
            }

            result.append("\n    /** Creates a table alias for the {@code ").append(originalName).append("} domain class. */\n");
            result.append("    public static TableAlias<").append(originalName).append("> as(String alias) {\n");
            result.append("        return TableAlias.of(alias);\n");
            result.append("    }\n");
            result.append("}\n");
            return result.toString();
        }
    }

    /** Parameter constants */
    static final class Const {
        static final String PARAM_PREFIX = "ujorm.prefix";
        static final String PARAM_SUFFIX = "ujorm.suffix";
        static final String PARAM_META_PACKAGE = "ujorm.metaPackage";
    }
}