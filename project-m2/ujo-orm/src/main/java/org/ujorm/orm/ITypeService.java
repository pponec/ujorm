/*
 *  Copyright 2020-2022 Pavel Ponec
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * A type service for popular Java types and more.
 * Implementation must have got an no-parameter constructor.
 * @author Pavel Ponec
 * @param <J> Common types of the Java objects
 * @param <D> Converted Java types to a JDBC type ready
 */
public interface ITypeService<J,D> {

    /**
     * GetValue from the result set by position
     * It must be the same implementation as {@link #getValue(org.ujorm.orm.metaModel.MetaColumn, java.sql.CallableStatement, int)}.
     * @param mColumn Meta-model column, where the {@link MetaColumn#getTypeCode() typeCode} must be assigned before.
     * @param rs The ResultSet instance
     * @param c Database column index starting at #1
     * @return Value form the result set.
     * @throws SQLException
     */
    J getValue(final MetaColumn mColumn, final ResultSet rs, final int c) throws SQLException;

    /**
     * GetValue from the <b>stored precedure</b> by position.
     * It must be the same implementation as {@link #getValue(org.ujorm.orm.metaModel.MetaColumn, java.sql.ResultSet, int)}.
     * @param mColumn Meta-model column, where the {@link MetaColumn#getTypeCode() typeCode} must be assigned before.
     * @param rs The CallableStatement instance
     * @param c Catabase column index starting at #1
     * @return Value form the result set.
     * @throws SQLException
     */
    J getValue(final MetaColumn mColumn, final CallableStatement rs, final int c) throws SQLException;

    /** GetValue from the result set by position.
     * @param mColumn Meta-model column, where the {@link MetaColumn#getTypeCode() typeCode} must be assigned before.
     * @param rs PreparedStatement
     * @param value Value to assign
     * @param c The database column index starts at #1
     * @throws SQLException
     */
    void setValue
        ( final MetaColumn mColumn
        , final PreparedStatement rs
        , final J value
        , final int c
        ) throws SQLException;


    /** Returns converted Java type to use in database <b>DDL statements</b>. */
    Class<D> getDbTypeClass(final MetaColumn column);

}
