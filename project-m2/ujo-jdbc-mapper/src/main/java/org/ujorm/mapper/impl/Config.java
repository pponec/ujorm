package org.ujorm.mapper.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.ujorm.mapper.MapperContext;
import org.ujorm.tools.common.Primitive;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
//import org.ujorm.mapper.MapperContext;

/** TODO:pop: create interface from this class */
@Setter @Getter @ToString
public class Config implements MapperContext {

    public static final Map<Class<?>, Function<String,?>> DEFAULT_CONVERTER_MAP = initConverterMap();

    /** The first key in the sequence represents the primary key. */
    private boolean firstPropertyIsIdentifier = true;

    private Map<Class<?>, Function<String,?>> converterMap = DEFAULT_CONVERTER_MAP;

    /** Maximum size of the cache in the ResultSet Mapper */
    private int maxCacheSize = 512;

    /** Batch size for the INSERT */
    private int insertBatchSize = 512;

    /** Printa all SQL tempaltes to the log. */
    private boolean printSql = true;

    /** Enable quoting the SQL columns */
    boolean enableSqlQuoting = false;

    /** Write a warning if the column is not a relation and has no JDBC mapping. */
    boolean columnMappingWarning = true;

    @NotNull
    static final Map<Class<?>, Function<String,?>> initConverterMap() {
        var result = new MapBuilder();
        {
            result.putAllPrimitiveTypes();
            result.put(UUID.class, UUID::fromString);
            result.put(LocalTime.class, LocalTime::parse);
            result.put(LocalDate.class, LocalDate::parse);
            result.put(LocalDateTime.class, LocalDateTime::parse);
            result.put(BigInteger.class, BigInteger::new);
            result.put(BigDecimal.class, BigDecimal::new);
            result.put(Currency.class, Currency::getInstance);
            result.put(URI.class, URI::create);
            result.put(String.class, Function.identity());
        }
        return result.build();
    }

    /** Temporary class for building an immutable map */
    private static final class MapBuilder {
        private final Map<Class<?>, Function<String,?>> map = new HashMap<>();

        /** Add all converters for primitive and its object types. */
        public <V> void putAllPrimitiveTypes() {
            var newMap = Primitive.ofAllToMap(
                    Primitive::objectClass,
                    Primitive::textParser
            );
            map.putAll(newMap);
        }

        /** Put new converter */
        public <V> void put(@NotNull Class<V> key, @NotNull Function<String,V> value) {
            map.put(key, value);
        }

        /** Build immutable map */
        public Map<Class<?>, Function<String,?>> build() {
            return Map.copyOf(map);
        }
    }

}
