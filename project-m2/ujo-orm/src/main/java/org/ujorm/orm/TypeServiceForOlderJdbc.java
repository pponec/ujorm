/*
 *  Copyright 2017-2022 Pavel Ponec
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

import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.orm.metaModel.MetaColumn;
import static org.ujorm.orm.TypeService.*;

/**
 * A type service support for some JDBC version older than 4.2 .
 * The class converts Java8 date types to older API of JDBC.
 * The supported types are:
 * <ul>
 *   <li>{@link java.time.LocalDate}</li>
 *   <li>{@link java.time.LocalDateTime}</li>
 *   <li>{@link java.time.LocalTime}</li>
 * </ul>
 *
 * @author Pavel Ponec
 * @since 1.72
 */
public class TypeServiceForOlderJdbc extends TypeService {

   /** Set a value to obsolete PreparedStatement. */
    @Override
    public void setValue
        ( @NotNull final MetaColumn mColumn
        , @NotNull final PreparedStatement rs
        , @Nullable final Object value
        , final int c) throws SQLException {
        if (value != null) {
            switch (mColumn.getTypeCode()) {
                case LOCAL_DATE:
                    rs.setDate(c, java.sql.Date.valueOf((LocalDate) value));
                    return;
                case LOCAL_TIME:
                    rs.setTime(c, java.sql.Time.valueOf((LocalTime) value));
                    return;
                case LOCAL_DATE_TIME:
                    rs.setTimestamp(c, java.sql.Timestamp.valueOf((LocalDateTime) value));
                    return;
                case OFFSET_DATE_TIME:
                    throw new IllegalStateException("Unsupported type: " + mColumn.getTypeCode());
            }
        }
        super.setValue(mColumn, rs, value, c);
    }

   /** Get a value from an older CallableStatement. */
    @Override
    public Object getValue
        ( @NotNull final MetaColumn mColumn
        , @NotNull final CallableStatement rs
        , final int c) throws SQLException, IllegalUjormException {
        switch (mColumn.getTypeCode()) {
            case LOCAL_DATE:
                final Date sqlDate = rs.getDate(c);
                return sqlDate !=null ? sqlDate.toLocalDate() : null;
            case LOCAL_TIME:
                final Time sqlTime = rs.getTime(c);
                return sqlTime != null ? sqlTime.toLocalTime() : null;
            case LOCAL_DATE_TIME:
                final Timestamp timestamp = rs.getTimestamp(c);
                return timestamp !=null ? timestamp.toLocalDateTime() : null;
            case OFFSET_DATE_TIME:
                throw new IllegalUjormException("Unsupported type: " + mColumn.getType());
            default:
                return super.getValue(mColumn, rs, c);
        }
    }

   /** Get a value from an older ResultSet. */
    @Override
    public Object getValue
        ( @NotNull final MetaColumn mColumn
        , @NotNull final ResultSet rs
        , final int c) throws SQLException, IllegalUjormException {
        switch (mColumn.getTypeCode()) {
            case LOCAL_DATE:
                final Date sqlDate = rs.getDate(c);
                return sqlDate !=null ? sqlDate.toLocalDate() : null;
            case LOCAL_TIME:
                final Time sqlTime = rs.getTime(c);
                return sqlTime != null ? sqlTime.toLocalTime() : null;
            case LOCAL_DATE_TIME:
                final Timestamp timestamp = rs.getTimestamp(c);
                return timestamp !=null ? timestamp.toLocalDateTime() : null;
            case OFFSET_DATE_TIME:
                throw new IllegalUjormException("Unsupported type: " + mColumn.getType());
            default:
                return super.getValue(mColumn, rs, c);
        }
    }

}
