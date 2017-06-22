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

import java.sql.Types;

/**
 * Supported Database Types
 * @author Pavel Ponec
 */
public enum DbType {

    /** Get the type by a Java key */
    Automatic(Integer.MIN_VALUE),
    INT(Types.INTEGER),
    BOOLEAN(Types.BOOLEAN),
    TINYINT(Types.TINYINT),
    SMALLINT(Types.SMALLINT),
    BIGINT(Types.BIGINT),
    /* Oracle support for replacing the BIGINT */
    //NUMBER(Types.BIGINT),
    DECIMAL(Types.DECIMAL),
    FLOAT(Types.FLOAT),
    DOUBLE(Types.DOUBLE),
    REAL(Types.REAL),
    TIME(Types.TIME),
    DATE(Types.DATE),
    TIMESTAMP(Types.TIMESTAMP),
    TIMESTAMP_WITH_TIME_ZONE(Types.TIMESTAMP_WITH_TIMEZONE),
    //IDENTITY(Types.IDENTITY),
    BINARY(Types.BINARY),
    //OTHER(Types.OTHER),
    VARCHAR(Types.VARCHAR),
    VARCHAR_IGNORECASE(Types.VARCHAR),
    CHAR(Types.CHAR),
    BLOB(Types.BLOB),
    CLOB(Types.CLOB),
    NULL(Types.NULL),
    UUID(Types.OTHER),
    //ARRAY(Types.ARRAY),
    ;

    private DbType(int sqlType) {
        this.sqlType = sqlType;
    }

    /** Returns the JDBC SQL type
     * @see java.sql.Types
     */
    private final int sqlType;


    /** Returns an JDBC SQL type
     * @see java.sql.Types
     */
    public int getSqlType() {
        return sqlType;
    }

}
