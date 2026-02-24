package org.ujorm.core.csv;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.tools.common.StreamUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class CsvManager<D> {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = Map.of(
            int.class, Integer.class,
            long.class, Long.class,
            boolean.class, Boolean.class,
            double.class, Double.class,
            float.class, Float.class,
            short.class, Short.class,
            byte.class, Byte.class,
            char.class, Character.class
    );

    private final DomainHandler<D> domainHandler;
    private final CsvConfig config;
    private final Map<String, Key<D,?>> keyMap;
    private final int maxFields;
    private final char delimiter;
    private final KeyFun<D, ?>[] keyFuns;

    public CsvManager(DomainHandler<D> domainHandler, CsvConfig csvConfig) {
        this.domainHandler = domainHandler;
        this.config = csvConfig;
        this.keyMap = StreamUtils.map(Key::getName, domainHandler.getKeyList());
        this.maxFields = domainHandler.count();
        this.delimiter = csvConfig.delimiter();
        this.keyFuns = new KeyFun[domainHandler.count()];
        init();
    }

    private void init() {
        for (var key : domainHandler.getKeyList()) {
            var type = wrapPrimitive(key.getType());
            var fun = this.config.converterMap().get(type);

            if (fun == null) {
                var msg = "Property %s type of %s has no converter".formatted(key.getFullName(), key.getType().getSimpleName());
                throw new IllegalArgumentException(msg);
            }
            keyFuns[key.getIndex()] = new KeyFun(key, fun);
        }
    }

    public Stream<D> convertByOrder(Stream<String> lines) {
        return lines.map( line -> {
            var result = AbstractUjo.of(domainHandler);
            var texts = splitToArray(line);
            var max = Math.min(texts.length, keyFuns.length);
            for (int i = 0; i < max; i++) {
                var text = texts[i];
                var keyFun = keyFuns[i];
                var key = (Key<D, Object>) keyFun.key;
                var value = key.getType().equals(String.class)
                        ? text
                        : text.isEmpty()
                        ? null
                        : keyFun.fun.apply(text);
                result.setValue(key, value);
            }
            return result.toDomainObject();
        });
    }

    @Deprecated
    public Stream<D> convertByName(Stream<String> line) {
        throw new IllegalArgumentException("TODO");
    }

    /**
     * Splits a string by a specific character delimiter into an array without using regex.
     * Highly optimized by pre-allocating the array with a known maximum size.
     * If the string contains more values than expected (maxFields), the extra
     * values are ignored.
     *
     * @param text  the input string to split
     * @return an array of substrings separated by the delimiter
     */
    public String[] splitToArray(String text) {
        if (text == null || text.isEmpty()) return new String[0];

        var result = new String[maxFields];
        var count = 0;
        var start = 0;

        while (count < maxFields) {
            var end = text.indexOf(delimiter, start);
            if (end == -1) {
                result[count++] = text.substring(start);
                break;
            }
            result[count++] = text.substring(start, end);
            start = end + 1;
        }
        return count == maxFields ? result : Arrays.copyOf(result, count);
    }

    record KeyFun<D, V> (Key<D,V> key, Function<String, V> fun) {}

    /**
     * Converts primitive types to their wrapper classes for map lookup.
     *
     * @param type the class type to check
     * @return the wrapper class if primitive, otherwise the original type
     */
    private Class<?> wrapPrimitive(Class<?> type) {
        return type.isPrimitive() ? PRIMITIVE_WRAPPERS.getOrDefault(type, type) : type;
    }

    public static <D> CsvManager<D> of(Class<D> domainClass) {
        return of(domainClass, CsvConfig.ofDefault());
    }

    public static <D> CsvManager<D> of(Class<D> domainClass, CsvConfig csvConfig) {
        return new CsvManager<>(DomainHandlerProvider.getHandler(domainClass),csvConfig);
    }
}
