package org.ujorm.core.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Handler class name */
public record ClassName(String packageName, String className) {
    /** Prefix for generated classes */
    public static final String PACKAGE_PREFIX = "org.ujorm.gen_.";

    @Override
    public @NotNull String toString() {
        return packageName + "." + className;
    }

    /** Factory method for a generated class.
     * Creates a new ClassName instance derived from the domain class
     * with a modified package and name suffix.
     *
     * @param domain The source domain class
     * @return New instance of ClassName
     * @throws IllegalArgumentException if the domain class does not have a canonical name
     */
    public static ClassName ofGenerated(Class<?> domain) {
        var canonicalName = domain.getCanonicalName();
        if (canonicalName == null) {
            var msg = "Missing canonical name for class: " + domain.getName();
            throw new IllegalArgumentException(msg);
        }
        var separatorIndex = canonicalName.lastIndexOf('.');
        var packageName = separatorIndex > 0
                ? canonicalName.substring(0, separatorIndex)
                : "";
        return new ClassName(
                PACKAGE_PREFIX + packageName,
                domain.getSimpleName() + "_"
        );
    }

    /** Factory method for a generated class from DomainModel.
     *
     * @param meta The source domain model
     * @return New instance of ClassName
     */
    public static ClassName ofGenerated(DomainModel meta) {
        return ofGenerated(meta.domainClass());
    }

    /** Try to load a class for this name. */
    @Nullable
    public Class<?> classForName() {
        try {
            return Class.forName(toString());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}