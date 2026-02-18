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
 * Common ancestor for domain handler with generated metamodel.
 * @param <D> The Domain class type (e.g. Employee)
 */
@RequiredArgsConstructor
public abstract class AbstractDomainHandler<D> {

    /** List of the keys */
    protected final List<Key<D, ?>> keyList;
    /** Mapping of the keys */
    protected final Map<String, Key<D, ?>> keyMap;
    /** Does the domain have any primitive attribute? */
    private final boolean hasPrimitives;
    /** Enable direct modification of the array elements */
    private final boolean enableArrayMutation;

    /**
     * Constructs a new instance with {@code enableArrayMutation} set to {@code true} by default.
     * @param keyList List of the all Ujorm Keys per domain object.
     */
    protected AbstractDomainHandler(@NotNull Key<D, ?>... keyList) {
        this(true, keyList);
    }

    /**
     * Constructs a new instance.
     * @param enableArrayMutation {@code true} to permit direct modification of the array elements;
     *  * {@code false} to enforce immutability by defensive copying or preventing setter access.
     * @param keyList List of the all Ujorm Keys per domain object.
     */
    protected AbstractDomainHandler(boolean enableArrayMutation, @NotNull Key<D, ?>... keyList) {
        this.keyList = List.of(keyList);
        this.keyMap = Stream.of(keyList).collect(Collectors.toUnmodifiableMap(Key::getName, Function.identity()));
        this.hasPrimitives = hasPrimitives(keyList);
        this.enableArrayMutation = enableArrayMutation;
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
            if (!enableArrayMutation) {
                values = values.clone();
            }
            for (var key : keyList) {
                if (key.getType().isPrimitive()) {
                    final var idx = key.getIndex();
                    final var value = values[idx];
                    if (value == null) {
                        values[idx] = key.getDefaultValue();
                    }
                }
            }
        }
        return values;
    }

    @NotNull
    public abstract Class<D> getDomainClass();

    public boolean isRecord() {
        return getDomainClass().isRecord();
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
                    .formatted(getDomainClass().getSimpleName(), name));
        }
        return (Key<D, V>) result;
    }

    public final int count() {
        return keyList.size();
    }

    /** Create a new domain object and assign values */
    public abstract D newDomain(Object[] values);
}