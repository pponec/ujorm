/*
 *  Copyright 2009-2014 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.orm;

import java.sql.JDBCType;

/**
 * Supported Database Types
 * @author Pavel Ponec
 * @see Similar enum {@link java.sql.JDBCType} missing types: 
 * UUID, VARCHAR_IGNORECASE
 */
public enum DbType {

    /** Get the type by a Java key */
    NULL(JDBCType.NULL),
    //
    INTEGER(JDBCType.INTEGER),
    BOOLEAN(JDBCType.BOOLEAN),
    BIT(JDBCType.BIT),
    TINYINT(JDBCType.TINYINT),
    SMALLINT(JDBCType.SMALLINT),
    BIGINT(JDBCType.BIGINT),
    /* Oracle support for replacing the BIGINT */
    //NUMBER(Types.BIGINT),
    DECIMAL(JDBCType.DECIMAL),
    FLOAT(JDBCType.FLOAT),
    DOUBLE(JDBCType.DOUBLE),
    REAL(JDBCType.REAL),
    NUMERIC(JDBCType.NUMERIC),
    //
    TIME(JDBCType.TIME),
    DATE(JDBCType.DATE),
    TIMESTAMP(JDBCType.TIMESTAMP),
    TIMESTAMP_WITH_TIMEZONE(JDBCType.TIMESTAMP_WITH_TIMEZONE),
    //
    BINARY(JDBCType.BINARY),
    VARCHAR(JDBCType.VARCHAR),
    VARCHAR_IGNORECASE(JDBCType.VARCHAR),
    LONGVARCHAR(JDBCType.LONGVARCHAR),
    CHAR(JDBCType.CHAR),
    BLOB(JDBCType.BLOB),
    CLOB(JDBCType.CLOB),
    UUID(JDBCType.OTHER),
    ARRAY(JDBCType.ARRAY),
    //
    OTHER(JDBCType.OTHER),
    ;

    private DbType(JDBCType sqlType) {
        this.sqlType = sqlType;
    }

    /** Returns the JDBC SQL type
     * @see java.sql.Types
     */
    private final JDBCType sqlType;


    /** Returns an JDBC SQL type
     * @see java.sql.Types
     */
    public int getSqlType() {
        return sqlType.getVendorTypeNumber();
    }

    /** Compatibility with a method {@link java.sql.JDBCType#getVendorTypeNumber() } 
     * @deprecated Use the method {@link #getSqlType()} rather.
     */
    @Deprecated
    public int getVendorTypeNumber() {
        return sqlType.getVendorTypeNumber();
    }

    /** Returns JDBC type enum */
    public JDBCType getJebcTypeEnum() {
        return sqlType;
    }

}
