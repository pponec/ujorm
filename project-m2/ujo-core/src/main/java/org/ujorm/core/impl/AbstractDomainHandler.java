package org.ujorm.core.impl;


import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.DomainHandler;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.generator.TableIdentifier;
import org.ujorm.tools.common.Primitive;
import org.ujorm.tools.common.StreamUtils;
import java.util.List;
import java.util.Locale;
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
    /** A mapping of the keys by name. */
    protected final Map<String, Key<D, ?>> keyMap;
    /** Maps uppercase database column names to their corresponding keys. */
    protected final Map<String, Key<D, ?>> columnMap;
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
        this.keyMap = StreamUtils.map(Key::name, keyList);
        this.columnMap = StreamUtils.map(key -> key.columnLabel().toUpperCase(Locale.ENGLISH).intern(), keyList);
        this.hasPrimitives = hasPrimitives(keyList);
        this.enableArrayMutation = enableArrayMutation;
        this.tableName = TableIdentifier.of(keyList[0].domainClass()).getQualifiedName();
    }

    @Override
    public String getDatabaseTable() {
        return tableName;
    }

    /** Does the domain have a primitive attribute? */
    static boolean hasPrimitives(final Key<?, ?>[] keyList) {
        for (var key : keyList) {
            if (key.type().isPrimitive()) return true;
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
                if (key.type().isPrimitive()) {
                    final var idx = key.index();
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
    public final @NotNull <V> Key<D, V> getKey(@NotNull final String name, @Nullable final Class<V> genericType)
            throws NoSuchElementException {
        var result = keyMap.get(name);
        if (result == null) {
            throw new NoSuchElementException("Property not found: %s.%s"
                    .formatted(getDomainClass().getSimpleName(), name));
        }
        if (genericType != null && genericType != Primitive.wrapPrimitive(result.type())) {
            var msg = "Property %s.%s has wrong type %s, expected is %s.".formatted(
                    getDomainClass().getSimpleName(),
                    name,
                    genericType.getSimpleName(),
                    result.type().getSimpleName());
                    throw new IllegalArgumentException(msg);
        }
        return (Key<D, V>) result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final @NotNull <V> Key<D, V> getKeyByColumn(@NotNull final String columnName, boolean required, @Nullable final Class<V> genericType)
            throws NoSuchElementException {
        var column = columnName.toUpperCase(Locale.ENGLISH);
        var result = columnMap.get(column);
        if (required && result == null) {
            throw new NoSuchElementException("Column not found: %s.%s"
                    .formatted(getDomainClass().getSimpleName(), column));
        }
        if (genericType != null && genericType != Primitive.wrapPrimitive(result.type())) {
            var msg = "Column %s.%s has wrong type %s, expected is %s.".formatted(
                    getDomainClass().getSimpleName(),
                    column,
                    genericType.getSimpleName(),
                    result.type().getSimpleName());
            throw new IllegalArgumentException(msg);
        }
        return (Key<D, V>) result;
    }

    @Override
    public final int count() {
        return keyList.size();
    }

    /** Create a new domain object and assign values from the argument array. */
    @Override
    public Ujo<D> newUjoDomain() {
        return AbstractUjo.of(this);
    }
}