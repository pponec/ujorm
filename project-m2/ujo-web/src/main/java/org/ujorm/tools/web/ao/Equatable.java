package org.ujorm.tools.web.ao;

import org.jetbrains.annotations.Nullable;

/** Interface defining type-safe equality comparison */
public interface Equatable<T> {

    /**
     * Compares this object to the specified object of the same type.
     *
     * @param other the object to be compared for equality with this object
     * @return true if the specified object is equal to this object
     */
    default boolean isEqualTo(@Nullable T other) {
        return equals(other);
    }

}