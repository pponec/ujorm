package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Simple enum mapper with thread-safe lazy initialization */
public class EnumMapper {

    /** A mapping a domain class to the domain handler object. */
    private final ConcurrentHashMap<Class<? extends Enum>, Enum<?>[]> byIndex = new ConcurrentHashMap<>();

    /** A mapping a domain class to the name-based map. */
    private final ConcurrentHashMap<Class<? extends Enum>, Map<String, Enum<?>>> byName = new ConcurrentHashMap<>();

    /** Get an Enumeration item by the index */
    @NotNull
    public <E extends Enum<E>> E getByIndex(@NotNull Class<E> clazz, int index) {
        return required(getByIndex(clazz, index, null), clazz, index);
    }

    /** Get an Enumeration item by the index */
    @Nullable
    public <E extends Enum<E>> E getByIndex(@NotNull Class<E> clazz, int index, @Nullable E defaultValue) {
        Objects.requireNonNull(clazz, "Class is required");
        var items = byIndex.computeIfAbsent(clazz, c -> (Enum<?>[]) c.getEnumConstants());
        return (index >= 0 && index < items.length) ? (E) items[index] : defaultValue;
    }

    /** Get an Enumeration item by the name */
    @NotNull
    public <E extends Enum<E>> E getByName(@NotNull Class<E> clazz, @NotNull String name) {
        return required(getByName(clazz, name, null), clazz, name);
    }

    /** Get an Enumeration item by the name */
    @Nullable
    public <E extends Enum<E>> E getByName(@NotNull Class<E> clazz, @NotNull String name, @Nullable E defaultValue) {
        Objects.requireNonNull(clazz, "Class is required");
        Objects.requireNonNull(name, "Name is required");
        var items = byName.computeIfAbsent(clazz, this::buildMap);
        var result = (E) items.get(name);
        return result != null ? result : defaultValue;
    }

    private <E extends Enum<E>> Map<String, Enum<?>> buildMap(Class<? extends Enum> clazz) {
        var constants = clazz.getEnumConstants();
        var result = new HashMap<String, Enum<?>>(constants.length);
        for (var constant : constants) {
            result.put(constant.name(), constant);
        }
        return result;
    }

    /** Not null checker */
    private static <E extends Enum<E>> E required(@Nullable E result, @NotNull Class<E> clazz, Object key) {
        if (result == null) {
            var msg = "No enum item of the %s class for [%s]".formatted(clazz.getSimpleName(), key);
            throw new IllegalArgumentException(msg);
        }
        return result;
    }
}