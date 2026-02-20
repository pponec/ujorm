package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;

public interface DomainHandler<D> {
    @NotNull Class<D> getDomainClass();
    @NotNull String getDatabaseTable();
    @NotNull List<Key<D, ?>> getKeyList();

    /**
     * Find key in metamodel.
     * @param name Property name.
     * @param type Only for safe result type
     * @return Key object.
     * @param <V> Value
     * @throws NoSuchElementException If not such element was found
     */
    @NotNull
    @SuppressWarnings("unchecked")
    <V> Key<D, V> getKey(@Nullable String name, @Nullable Class<V> type)
            throws NoSuchElementException;

    /**
     * Find key in metamodel.
     * @param name Property name.
     * @return Key object.
     * @throws NoSuchElementException If not such element was found
     */
    @NotNull
    @SuppressWarnings("unchecked")
    default Key<D, Object> getKey(@Nullable String name) {
        return getKey(name, Object.class);
    }

    /** Return total count of the properties. */
    default int count() {
        return getKeyList().size();
    }

    /** Create a new domain object and assign values from the argument array. */
    D newDomain(Object... values);
}
