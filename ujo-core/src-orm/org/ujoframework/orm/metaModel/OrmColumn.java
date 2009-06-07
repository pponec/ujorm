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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.UniqueKey;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.orm.annot.GenerationType;

/**
 * Database column metadata
 * @author Pavel Ponec
 * @composed 1 - * DbType
 */
public class OrmColumn extends OrmRelation2Many {

    /** Property count */
    protected static int propertyCount = OrmRelation2Many.propertyCount;

    /** DB primary key */
    public static final UjoProperty<OrmColumn,Boolean> PRIMARY_KEY = newProperty("primaryKey", false, propertyCount++);
    /** Database Type */
    public static final UjoProperty<OrmColumn,DbType> DB_TYPE = newProperty("dbType", DbType.Automatic, propertyCount++);
    /** Column NOT-NULL */
    public static final UjoProperty<OrmColumn,Boolean> MANDATORY = newProperty("mandatory", false, propertyCount++);
    /** Column value length */
    public static final UjoProperty<OrmColumn,Integer> MAX_LENGTH = newProperty("maxLength", -1, propertyCount++);
    /** Column value precision */
    public static final UjoProperty<OrmColumn,Integer> PRECISION = newProperty("precision", -1, propertyCount++);
    /** DB Default value */
    public static final UjoProperty<OrmColumn,String> DEFAULT_VALUE = newProperty("default", "", propertyCount++);
    /** The column is included in the index of the name */
    public static final UjoProperty<OrmColumn,String> INDEX_NAME = newProperty("indexName", "", propertyCount++);
    /** DB primary key generator */
    public static final UjoProperty<OrmColumn,GenerationType> PRIMARY_KEY_GEN = newProperty("primaryKeyGenerator", GenerationType.DB_SEQUENCE, propertyCount++);


    /** Foreign column names. */
    private String[] foreignNames = null;
    private static final String[] EMPTY_NAMES = new String[0];


    public OrmColumn() {
    }

    public OrmColumn(OrmTable table, UjoProperty tableProperty, OrmColumn param) {
        super(table, tableProperty, param);

        Field field = UjoManager.getInstance().getPropertyField(OrmTable.DB_PROPERTY.of(table).getItemType(), tableProperty);
        Column column = field.getAnnotation(Column.class);

        if (param!=null) {
            changeDefault(this, PRIMARY_KEY, PRIMARY_KEY.of(param));
            changeDefault(this, MANDATORY  , MANDATORY.of(param) || PRIMARY_KEY.of(param));
            changeDefault(this, MAX_LENGTH , MAX_LENGTH.of(param));
            changeDefault(this, PRECISION  , PRECISION.of(param));
            changeDefault(this, DB_TYPE    , DB_TYPE.of(param));
            changeDefault(this, INDEX_NAME , INDEX_NAME.of(param));
        }
        if (column!=null) {
            changeDefault(this, PRIMARY_KEY, column.pk());
            changeDefault(this, MANDATORY  , column.mandatory() || column.pk());
            changeDefault(this, MAX_LENGTH , column.maxLenght());
            changeDefault(this, PRECISION  , column.precision());
            changeDefault(this, DB_TYPE    , column.type());
            changeDefault(this, INDEX_NAME , column.indexName());
        }

        if (DB_TYPE.isDefault(this)) {
            OrmTable.DATABASE.of(table).changeDbType(this);
        }
        if (MAX_LENGTH.isDefault(this)) {
            OrmTable.DATABASE.of(table).changeDbLength(this);
        }
    }

    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    /** It is a DB column (either a value of a foreign key) */
    @Override
    public boolean isColumn() {
        return true;
    }

    /** Is it a Foreign Key ? */
    @Override
    public boolean isForeignKey() {
        final boolean result = getProperty().isTypeOf(OrmUjo.class);
        return result;
    }

    /** Is it a Primary Key? */
    public boolean isPrimaryKey() {
        final boolean result = PRIMARY_KEY.of(this);
        return result;
    }

    /** Returns a maximal db column length in the database.
     * @return If property is undefined then the method returns value -1.
     */
    public int getMaxLength() {
        return MAX_LENGTH.of(this);
    }

    /** Returns the db column precision.
     * @return If property is undefined then the method returns value -1.
     */
    public int getPrecision() {
        return PRECISION.of(this);
    }

    /** Returns true if the related db column is NOT NULL. */
    public boolean isMandatory() {
        return MANDATORY.of(this);
    }


    /** Returns an original foreign columns in case a foreign column. */
    @SuppressWarnings("unchecked")
    public List<OrmColumn> getForeignColumns() {
        List<OrmColumn> result;
        Class type = getProperty().getType();
        OrmTable table = getHandler().findTableModel(type);
        if (table!=null) {
            OrmPKey pk = OrmTable.PK.of(table);
            result = OrmPKey.COLUMNS.getList(pk);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /** Returns names of foreign columns.
     * <br>TODO: Is a time to an optimalization ?
     */
    @SuppressWarnings("unchecked")
    private String[] getForeignColumnNames() {
        if (foreignNames==null) {
            final Class type = getProperty().getType();
            final OrmTable foreignTable = getHandler().findTableModel(type);
            if (foreignTable!=null && isForeignKey()) {
                final OrmPKey pk = OrmTable.PK.of(foreignTable);
                final List<OrmColumn> dbColumns = OrmPKey.COLUMNS.getList(pk);
                final StringTokenizer tokenizer = new StringTokenizer(dbColumns.size()==1 ? NAME.of(this) : "", ", ");

                ArrayList<String> fNames = new ArrayList<String>(dbColumns.size());
                for (int i=0; i<dbColumns.size(); i++) {
                    String name;
                    if (tokenizer.hasMoreTokens()) {
                        name = tokenizer.nextToken();
                    } else {
                        name = "fk_"
                          // + OrmTable.NAME.of(foreignTable)
                             + OrmColumn.NAME.of(this)
                             + "_"
                             + OrmColumn.NAME.of(dbColumns.get(i))
                             ;
                    }
                    fNames.add(name);
                }
                foreignNames = fNames.toArray(new String[fNames.size()]);
            } else {
                foreignNames = EMPTY_NAMES;
            }
        }
        return foreignNames;
    }

    /** Returns a name of foreign column by index */
    public String getForeignColumnName(int index) {
        final String result = getForeignColumnNames()[index];
        return result;
    }

    /** Returns a property value from a table */
    @SuppressWarnings("unchecked")
    public Object getValue(final OrmUjo ormUjo) {
        final UjoProperty property = getProperty();
        final Object result = property.of(ormUjo);
        return result;
    }

    /** Returns a property value from a table */
    @SuppressWarnings("unchecked")
    public void setValue(final OrmUjo ormUjo, Object value) {
        final UjoProperty property = getProperty();

        if (isForeignKey()
        &&   value !=null
        && !(value instanceof OrmUjo)) {
           value = new UniqueKey(value);
        }

        property.setValue(ormUjo, value);
    }


    /** Returns a Java Class of value */
    public Class getType() {
        final UjoProperty property = getProperty();
        return property.getType();
    }

    /** Returns a TABLE and COLUMN names. */
    @Override
    public String toString() {
        return getFullName();
    }

    /** Returns a DB, TABLE and COLUMN name. */
    public String getFullName() {
        try {
            final String result = TABLE.of(this)
                .getDatabase()
                .getDialect()
                .printFullName(this, new StringBuilder(32))
                .toString()
                ;
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Print a full 'alias' name of foreign column by index */
    public void printForeignColumnFullName(int index, Appendable out) throws IOException {
        OrmTable table = TABLE.of(this);
        out.append(table.getAlias());
        out.append('.');
        out.append(getForeignColumnNames()[index]);
    }



}
