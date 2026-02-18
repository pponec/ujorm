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

    public static ClassName forHandler(Class<?> domain){
        return new ClassName(
                PACKAGE_PREFIX + domain.getPackageName(),
                domain.getSimpleName() + "_");
    }

    public static ClassName forHandler(DomainModel domainModel) {
        return forHandler(domainModel.domainClass());
    }

    /** Try load a class for this. */
    @Nullable
    public Class<?> classForName() {
        try {
            return Class.forName(toString());
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
