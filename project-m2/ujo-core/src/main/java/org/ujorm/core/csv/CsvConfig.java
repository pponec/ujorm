package org.ujorm.core.csv;

import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.common.Primitive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public record CsvConfig(
        /** Optional CSV header (not implemented yet) */
        @Nullable
        String header,
        Map<Class<?>, Function<String, ?>> converterMap,
        /** Line splitter */
        CsvLineSplitter splitter
) {

    /** Default delimiter is a pipe {@code '|'}*/
    public static CsvConfig ofDefault() {
        return of('|', "");
    }

    /**
     * Create configuration
     * @param delimiter Delimeter character
     * @param header Optional CSV header is not implemented yet.
     * @return
     */
    public static CsvConfig of(char delimiter, @Nullable String header) {
        return new CsvConfig(header, initConverterMap(), CsvLineSplitter.ofQuoted(delimiter));
    }

    public static Map<Class<?>, Function<String, ?>> initConverterMap() {
        var result = new MapBuilder();
        result.putAllPrimitives();
        result.put(BigDecimal.class, BigDecimal::new);
        result.put(BigInteger.class, BigInteger::new);
        result.put(Currency.class, Currency::getInstance);
        result.put(LocalDate.class, LocalDate::parse);
        result.put(LocalDateTime.class, LocalDateTime::parse);
        result.put(LocalTime.class, LocalTime::parse);
        result.put(String.class, Function.identity());
        result.put(URI.class, URI::create);
        result.put(UUID.class, UUID::fromString);
        return result.build();
    }


    /** Build immutable map */
    private static final class MapBuilder {
        private final Map<Class<?>, Function<String, ?>> map = new HashMap<>();

        /** Registers all converters for primitive types and their corresponding wrapper classes. */
        private void putAllPrimitives() {
            var localMap = Primitive.ofAllToMap(
                        Primitive::objectClass,
                        Primitive::textParser
                    );
            map.putAll(localMap);
        }

        public <V> void put(Class<V> key, Function<String, V> value) {
            map.put(key, value);
        }

        /** Build immutable map */
        public Map<Class<?>, Function<String, ?>> build() {
            return Map.copyOf(map);
        }
    }
}
