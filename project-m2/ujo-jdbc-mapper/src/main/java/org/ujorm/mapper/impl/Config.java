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

    /** The first key in the sequence represents the primary key. */
    private boolean firstPropertyIsIdentifier = true;


    /** Maximum size of the cache in the ResultSet Mapper */
    private int maxCacheSize = 512;

    /** Batch size for the INSERT */
    private int insertBatchSize = 512;

    /** Printa all SQL template to the log. */
    private boolean printSql = true;

    /** Enable quoting the SQL columns */
    boolean enableSqlQuoting = false;

    /** Write a warning if the column is not a relation and has no JDBC mapping. */
    boolean columnMappingWarning = true;

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
