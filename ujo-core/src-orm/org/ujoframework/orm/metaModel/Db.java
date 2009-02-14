/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.orm.metaModel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.annot.Database;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.implementation.orm.RelationToMany;
import java.sql.*;

/**
 * A logical database description.
 * @author pavel
 */
public class Db extends AbstractMetaModel {

    /** Database name */
    @XmlAttribute
    public static final UjoProperty<Db,String> NAME = newProperty("name", "");
    /** List of tables */
    public static final ListProperty<Db,DbTable> TABLES = newPropertyList("table", DbTable.class);
    /** JDBC URL connection */
    public static final UjoProperty<Db,String> CONNECTION = newProperty("connection", "");
    /** JDBC Class */
    public static final UjoProperty<Db,String> JDBC_CLASS = newProperty("jdbcClass", "");
    /** DB class root instance */
    @Transient
    public static final UjoProperty<Db,TableUjo> ROOT = newProperty("root", TableUjo.class);
    /** LDPA */
    public static final UjoProperty<Db,String> LDAP = newProperty("ldap", "");

    public Db(TableUjo database) {
        ROOT.setValue(this, database);

        Database annotDB = database.getClass().getAnnotation(Database.class);
        if (annotDB!=null) {
            NAME.setValue(this, annotDB.name());
            CONNECTION.setValue(this, annotDB.jdbcUrl());
            JDBC_CLASS.setValue(this, annotDB.jdbcClass());
            LDAP.setValue(this, annotDB.ldap());
        }
        if (NAME.isDefault(this)) {
            NAME.setValue(this, database.getClass().getSimpleName());
        }


        for (UjoProperty tableProperty : database.readProperties()) {

            if (tableProperty instanceof RelationToMany) {
                RelationToMany tProperty = (RelationToMany) tableProperty;

                DbTable table = new DbTable(this, tProperty);
                TABLES.addItem(this, table);
            }
        }

    }

    /** Change DbType by a Java property */
    public void changeDbType(DbColumn column) {
       UjoProperty property = DbColumn.PROPERTY.of(column);

       Class type = property.getType();

        if (String.class==type) {
            DbColumn.TYPE.setValue(column, DbType.VARCHAR);
            changeDefault(column, DbColumn.MAX_LENGTH, 128);
        }
        else if (Integer.class==type) {
            DbColumn.TYPE.setValue(column, DbType.INT);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
        }
        else if (BigInteger.class.isAssignableFrom(type)) {
            DbColumn.TYPE.setValue(column, DbType.BIGINT);
            changeDefault(column, DbColumn.MAX_LENGTH, 16);
        }
        else if (Double.class==type) {
            DbColumn.TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
            changeDefault(column, DbColumn.PRECISION, 2);
        }
        else if (BigDecimal.class==type) {
            DbColumn.TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
            changeDefault(column, DbColumn.PRECISION, 2);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            DbColumn.TYPE.setValue(column, DbType.DATE);
        }
        else if (Date.class.isAssignableFrom(type)) {
            DbColumn.TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (TableUjo.class.isAssignableFrom(type)) {
            DbColumn.TYPE.setValue(column, DbType.INT);
        }
    }

    /** Name of Database. */
    @Override
    public String toString() {
        return NAME.of(this);
    }

    /** Create connection */
    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        final Connection result = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
        return result;
    }

}
