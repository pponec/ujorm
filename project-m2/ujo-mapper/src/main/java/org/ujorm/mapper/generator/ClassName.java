package org.ujorm.mapper.generator;

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
     */
    public static ClassName ofGenerated(Class<?> domain) {
        return new ClassName(
                PACKAGE_PREFIX + domain.getPackageName(),
                domain.getSimpleName() + "_");
    }

    /** Factory method for a generated class from DomainModel.
     * * @param domainModel The source domain model
     * @return New instance of ClassName
     */
    public static ClassName ofGenerated(DomainModel domainModel) {
        return ofGenerated(domainModel.domainClass());
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