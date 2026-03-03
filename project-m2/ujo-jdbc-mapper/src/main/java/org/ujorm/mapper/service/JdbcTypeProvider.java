/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.mapper.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.common.Primitive;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.JDBCType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** JDBC provider */
public abstract class JdbcTypeProvider {

    private static final Map<Class<?>, JDBCType> TYPE_MAP = createTypeMap();

    /** Static methods only */
    private JdbcTypeProvider() {
    }

    /** Create a map of Java types to JDBC types, excluding primitive types. */
    private static Map<Class<?>, JDBCType> createTypeMap() {
        var result = new HashMap<Class<?>, JDBCType>();

        // String & Character
        result.put(String.class, JDBCType.VARCHAR);
        result.put(Character.class, JDBCType.CHAR);

        // Numeric types
        result.put(Integer.class, JDBCType.INTEGER);
        result.put(Long.class, JDBCType.BIGINT);
        result.put(Short.class, JDBCType.SMALLINT);
        result.put(Byte.class, JDBCType.TINYINT);
        result.put(Double.class, JDBCType.DOUBLE);
        result.put(Float.class, JDBCType.REAL);
        result.put(BigDecimal.class, JDBCType.DECIMAL);
        result.put(BigInteger.class, JDBCType.NUMERIC);

        // Boolean
        result.put(Boolean.class, JDBCType.BOOLEAN);

        // Java Time API (Modern)
        result.put(LocalDate.class, JDBCType.DATE);
        result.put(LocalTime.class, JDBCType.TIME);
        result.put(LocalDateTime.class, JDBCType.TIMESTAMP);
        result.put(OffsetDateTime.class, JDBCType.TIMESTAMP_WITH_TIMEZONE);
        result.put(OffsetTime.class, JDBCType.TIME_WITH_TIMEZONE);
        result.put(ZonedDateTime.class, JDBCType.TIMESTAMP_WITH_TIMEZONE);
        result.put(Instant.class, JDBCType.TIMESTAMP);

        // Legacy SQL & Util types
        result.put(java.sql.Date.class, JDBCType.DATE);
        result.put(java.sql.Time.class, JDBCType.TIME);
        result.put(java.sql.Timestamp.class, JDBCType.TIMESTAMP);
        result.put(java.util.Date.class, JDBCType.TIMESTAMP);

        // Binary & Large Objects
        result.put(byte[].class, JDBCType.BINARY);
        result.put(Blob.class, JDBCType.BLOB);
        result.put(Clob.class, JDBCType.CLOB);

        // Others
        result.put(UUID.class, JDBCType.OTHER);

        return Map.copyOf(result); // make map immutable
    }

    /** Package private access */
    @Nullable
    static JDBCType findJdbcType(@NotNull Class<?> clazz) throws IllegalArgumentException {
        if (clazz == null) {
            throw new IllegalArgumentException("class must not be null");
        }
        return TYPE_MAP.get(Primitive.wrapPrimitive(clazz));
    }
}
