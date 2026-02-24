package org.ujorm.mapper.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ujorm.mapper.MapperContext;
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
//import org.ujorm.mapper.MapperContext;

/** TODO:pop: create interface from this class */
@Setter @Getter @ToString
public class Config implements MapperContext {

    public static final Map<Class<?>, Function<String,?>> DEFAULT_CONVERTER_MAP = initConverterMap();

    /** The first key in the sequence represents the primary key. */
    private boolean firstPropertyIsIdentifier = true;

    private Map<Class<?>, Function<String,?>> converterMap = DEFAULT_CONVERTER_MAP;



    static final Map<Class<?>, Function<String,?>> initConverterMap() {
        var result = new MapBuilder();
        result.put(Boolean.class, Boolean::valueOf);
        result.put(Byte.class, Byte::valueOf);
        result.put(Short.class, Short::valueOf);
        result.put(Character.class, s -> s.isEmpty() ? null : s.charAt(0));
        result.put(Integer.class, Integer::valueOf);
        result.put(Float.class, Float::valueOf);
        result.put(Long.class, Long::valueOf);
        result.put(Double.class, Double::valueOf);
        result.put(UUID.class, UUID::fromString);
        result.put(LocalTime.class, LocalTime::parse);
        result.put(LocalDate.class, LocalDate::parse);
        result.put(LocalDateTime.class, LocalDateTime::parse);
        result.put(BigInteger.class, BigInteger::new);
        result.put(BigDecimal.class, BigDecimal::new);
        result.put(Currency.class, Currency::getInstance);
        result.put(URI.class, URI::create);
        result.put(String.class, Function.identity());
        return result.build();
    }


    /** Build immutable map */
    private static final class MapBuilder {
        private final Map<Class<?>, Function<String,?>> map = new HashMap<>();

        public <V> void put(Class<V> key, Function<String,V> value, Map<Class<?>, Function<String,V>>) {
             map.put(key, value);
        }

        /** Build immutable map */
        public Map<Class<?>, Function<String,?>> build() {
            return Map.copyOf(map);
        }
    }

}
