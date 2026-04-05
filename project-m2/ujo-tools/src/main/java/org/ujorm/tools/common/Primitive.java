package org.ujorm.tools.common;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Descriptor for primitive types, their object wrappers, and string converters.
 */
public record Primitive<T>(
        /** Primitive class */
        Class<T> primitiveClass,
        /** Object class */
        Class<T> objectClass,
        /** Default primitive value in the object format */
        T defaultValue,
        /** A parser of the text format. */
        Function<String, T> textParser
) {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_MAP = ofAllToMap(
            Primitive::primitiveClass,
            Primitive::objectClass
    );

    /**
     * Converts primitive types to their object wrapper classes.
     *
     * @param type the class type to check
     * @return the wrapper class if primitive, otherwise the original type
     */
    public static <T> Class<T> wrapPrimitive(final Class<T> type) {
        return type.isPrimitive() ? (Class<T>) PRIMITIVE_MAP.getOrDefault(type, type) : type;
    }

    /** Compare two classes with ignoring primitive forms */
    public static boolean equalsIgnorePrimitive(@NotNull final Class<?> c1, @NotNull final Class<?> c2) {
        return wrapPrimitive(c1) == wrapPrimitive(c2);
    }

    /**
     * Builds a new instance.
     */
    @NotNull
    public static <T> Primitive<T> of(
            @NotNull final Class<T> primitiveClass,
            @NotNull final Class<T> objectClass,
            @NotNull final T defaultValue,
            @NotNull final Function<String, T> textParser) {
        return new Primitive<>(primitiveClass, objectClass, defaultValue, textParser);
    }

    /**
     * Creates a list of all primitive types with non-null basic converters.
     * @return A list of primitive type descriptors
     * @see StreamUtils#map(Function, Collection)
     */
    public static List<Primitive<?>> ofAll() {
        return List.of(
                of(boolean.class, Boolean.class, false, Boolean::valueOf),
                of(byte.class, Byte.class, (byte) 0, Byte::valueOf),
                of(char.class, Character.class, '\0', s -> s.isEmpty() ? null : s.charAt(0)),
                of(short.class, Short.class, (short) 0, Short::valueOf),
                of(int.class, Integer.class, 0, Integer::valueOf),
                of(long.class, Long.class, 0L, Long::valueOf),
                of(float.class, Float.class, 0.0f, Float::valueOf),
                of(double.class, Double.class, 0.0d, Double::valueOf)
        );
    }

    /**
     * Collects all primitive types using the provided collector.
     * @param collector The collector used to accumulate the elements
     * @param <A> The intermediate accumulation type of the Collector
     * @param <R> The type of the final collected result
     * @return The collected result produced by the collector
     */
    public static <A, R> R ofAllToCollect(@NotNull final Collector<? super Primitive<?>, A, R> collector) {
        return ofAll().stream().collect(collector);
    }

    /**
     * Collects all primitive types into a Map using the provided key and value mapping functions.
     *
     * @param keyMapper a mapping function to produce map keys
     * @param valueMapper a mapping function to produce map values
     * @param <K> the output type of the map keys
     * @param <V> the output type of the map values
     * @return A map populated with the extracted keys and values
     * @see Function#identity
     */
    public static <K, V> Map<K, V> ofAllToMap(
            @NotNull final Function<? super Primitive<?>, ? extends K> keyMapper,
            @NotNull final Function<? super Primitive<?>, ? extends V> valueMapper) {
        return ofAllToCollect(Collectors.toMap(keyMapper, valueMapper));
    }
}