package org.ujorm.core.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
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

    /** List of the keys */
    protected final List<Key<D, ?>> keyList;
    /** Mapping of the keys */
    protected final Map<String, Key<D, ?>> keyMap;
    /** Does the domain have any primitive attribute? */
    private final boolean hasPrimitives;


    protected AbstractMetaModel(@NotNull Key<D, ?>... keyList) {
        this.keyList = List.of(keyList);
        this.keyMap = Stream.of(keyList).collect(Collectors.toUnmodifiableMap(Key::getName, Function.identity()));
        this.hasPrimitives = hasPrimitives(keyList);
    }

    /** Does the domain have a primitive attribute? */
    static boolean hasPrimitives(final Key<?, ?>[] keyList) {
        for (var key : keyList) {
            if (key.getType().isPrimitive()) return true;
        }
        return false;
    }

    /** For Record: replace all null values for primitive types with default values. */
    protected @NotNull Object[] normalizePrimitives(@NotNull Object[] values) {
        if (keyList.size() != values.length) {
            var msg = "Constructor requires %s arguments, but %s were provided."
                    .formatted(keyList.size(), values.length);
            throw new IllegalArgumentException(msg);
        }
        if (hasPrimitives) {
            values = values.clone();
            for (var key : keyList) {
                if (key.getType().isPrimitive()) {
                    var idx = key.getIndex();
                    var value = values[idx];
                    if (value == null) {
                        values[idx] = key.getDefaultValue();
                    }
                }
            }
        }
        return values;
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

    /** Create a new domain object and assign values */
    public abstract D newDomain(Object[] values);
}