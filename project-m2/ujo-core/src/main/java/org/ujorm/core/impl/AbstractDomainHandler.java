package org.ujorm.core.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.generator.TableIdentifier;
import org.ujorm.tools.common.Primitive;
import org.ujorm.tools.common.StreamUtils;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Common ancestor for domain handler with generated metamodel.
 * @param <D> The Domain class type (e.g. Employee)
 */
@RequiredArgsConstructor
public abstract class AbstractDomainHandler<D> implements DomainHandler<D> {

    /** List of the keys */
    protected final List<Key<D, ?>> keyList;
    /** Mapping of the keys */
    protected final Map<String, Key<D, ?>> keyMap;
    /** Does the domain have any primitive attribute? */
    private final boolean hasPrimitives;
    /** Enable direct modification of the array elements */
    private final boolean enableArrayMutation;
    /** Database table qualified name */
    private final String tableName;

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
        this.keyMap = StreamUtils.map(Key::getName, keyList);
        this.hasPrimitives = hasPrimitives(keyList);
        this.enableArrayMutation = enableArrayMutation;
        this.tableName = TableIdentifier.of(keyList[0].getDomainClass()).getQualifiedName();
    }

    @Override
    public String getDatabaseTable() {
        return tableName;
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

    public boolean isRecord() {
        return getDomainClass().isRecord();
    }

    @Override
    public final @NotNull List<Key<D, ?>> getKeyList() {
        return keyList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final @NotNull <V> Key<D, V> getKey(@NonNull final String name, @Nullable final Class<V> genericType)
            throws NoSuchElementException {
        var result = keyMap.get(name);
        if (result == null) {
            throw new NoSuchElementException("Key not found: %s.%s"
                    .formatted(getDomainClass().getSimpleName(), name));
        }
        if (genericType != null && genericType != Primitive.wrapPrimitive(result.getType())) {
            var msg = "Property %s.%s has wrong type %s, expected is %s.".formatted(
                    getDomainClass().getSimpleName(),
                    name,
                    genericType.getSimpleName(),
                    result.getType().getSimpleName());
            throw new IllegalArgumentException(msg);
        }
        return (Key<D, V>) result;
    }

    @Override
    public final int count() {
        return keyList.size();
    }
}