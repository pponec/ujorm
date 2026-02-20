package org.ujorm.mapper.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** JDBC provider */
public abstract class JdbcTypeProvider {

    private static final Map<Class<?>, JDBCType> TYPE_MAP = createTypeMap();

    /** Static methods only */
    private JdbcTypeProvider() {
    }

    private static Map<Class<?>, JDBCType> createTypeMap() {
        var result = new HashMap<Class<?>, JDBCType>();

        // String
        result.put(String.class, JDBCType.VARCHAR);

        // Integer
        result.put(Integer.class, JDBCType.INTEGER);
        result.put(int.class, JDBCType.INTEGER);

        // Long
        result.put(Long.class, JDBCType.BIGINT);
        result.put(long.class, JDBCType.BIGINT);

        // Short
        result.put(Short.class, JDBCType.SMALLINT);
        result.put(short.class, JDBCType.SMALLINT);

        // Byte
        result.put(Byte.class, JDBCType.TINYINT);
        result.put(byte.class, JDBCType.TINYINT);

        // Boolean
        result.put(Boolean.class, JDBCType.BOOLEAN);
        result.put(boolean.class, JDBCType.BOOLEAN);

        // Double
        result.put(Double.class, JDBCType.DOUBLE);
        result.put(double.class, JDBCType.DOUBLE);

        // Float
        result.put(Float.class, JDBCType.FLOAT);
        result.put(float.class, JDBCType.FLOAT);

        // BigDecimal
        result.put(BigDecimal.class, JDBCType.DECIMAL);

        // Java Time API
        result.put(LocalDate.class, JDBCType.DATE);
        result.put(LocalTime.class, JDBCType.TIME);
        result.put(LocalDateTime.class, JDBCType.TIMESTAMP);

        // java.sql types
        result.put(java.sql.Date.class, JDBCType.DATE);
        result.put(java.sql.Time.class, JDBCType.TIME);
        result.put(java.sql.Timestamp.class, JDBCType.TIMESTAMP);

        // Binary
        result.put(byte[].class, JDBCType.BINARY);

        // UUID
        result.put(UUID.class, JDBCType.OTHER);

        return Map.copyOf(result); // make map immutable
    }

    /** Package private access */
    @Nullable
    static JDBCType findJdbcType(@NotNull Class<?> clazz) throws IllegalArgumentException {
        if (clazz == null) {
            throw new IllegalArgumentException("class must not be null");
        }
        return TYPE_MAP.get(clazz);
    }
}
