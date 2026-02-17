package org.ujorm.core.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common ancestor for generated MetaModels.
 * @param <D> The Domain class type (e.g. Employee)
 */
@RequiredArgsConstructor
public abstract class AbstractMetaModel<D> {

    private final List<Key<D, ?>> keyList;
    private final Map<String, Key<D, ?>> keyMap;

    protected AbstractMetaModel(@NotNull Key<D, ?>... keyList) {
        this.keyList = List.of(keyList);
        this.keyMap = Stream.of(keyList).collect(Collectors.toUnmodifiableMap(Key::getName, Function.identity()));
    }

    @NotNull
    public abstract Class<D> getDomainType();

    public boolean isRecord() {
        return getDomainType().isRecord();
    }

    @NotNull
    public final List<Key<D, ?>> getKeyList() {
        return keyList;
    }

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
    public final <V> Key<D, V> getKey(@Nullable final String name, final Class<V> type)
            throws NoSuchElementException {
        var result = keyMap.get(name);
        if (result == null) {
            throw new NoSuchElementException("Key not found: %s.%s"
                    .formatted(getDomainType().getSimpleName(), name));
        }
        return (Key<D, V>) result;
    }

    public final int count() {
        return keyList.size();
    }

    /** Create a new domain object */
    public D newDomain() {
        try {
            return getDomainType().getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException  | InvocationTargetException | NoSuchMethodException ex) {
            throw new IllegalStateException("Cant create domain object. " + getDomainType(), ex);
        }
    }
}