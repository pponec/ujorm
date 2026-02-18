package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;

public interface DomainHandler<D> {
    @NotNull Class<D> getDomainClass();

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
    <V> Key<D, V> getKey(@Nullable String name, Class<V> type)
            throws NoSuchElementException;

    int count();
}
