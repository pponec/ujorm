/*
 *  Copyright 2009-2010 Pavel Ponec
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

package org.ujorm.orm.metaModel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.orm.RelationToOne;
import org.ujorm.orm.DbType;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.TypeService;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.ao.UjoStatement;

/**
 * Database column metadata
 * @author Pavel Ponec
 * @composed 1 - * DbType
 */
public final class MetaColumn extends MetaRelation2Many {
    private static final Class CLASS = MetaColumn.class;

    /** DB primary key */
    public static final Property<MetaColumn,Boolean> PRIMARY_KEY = newProperty("primaryKey", false);
    /** Database Type */
    public static final Property<MetaColumn,DbType> DB_TYPE = newProperty("dbType", DbType.Automatic);
    /** Column NOT-NULL */
    public static final Property<MetaColumn,Boolean> MANDATORY = newProperty("mandatory", false);
    /** Column value length */
    public static final Property<MetaColumn,Integer> MAX_LENGTH = newProperty("maxLength", -1);
    /** Column value precision */
    public static final Property<MetaColumn,Integer> PRECISION = newProperty("precision", -1);
    /** DB Default value */
    public static final Property<MetaColumn,String> DEFAULT_VALUE = newProperty("default", "");
    /** A name of the non-unique database index for the column, where the same index can contain more columns.
     * If a single column of the index is marked as unique, so the entire index will be unique. */
    public static final Property<MetaColumn,String> INDEX = newProperty("index", "");
    /** A name of the unique database index for the column, where the same index can contain more columns.
     * If a single column of the index is marked as unique, so the entire index will be unique. */
    public static final Property<MetaColumn,String> UNIQUE_INDEX = newProperty("uniqueIndex", "");
    /** A name of the constraint for the case a foreign key */
    public static final Property<MetaColumn,String> CONSTRAINT_NAME = newProperty("constraintName", "");
    /** Convert, save and read application data from/to the database */
    public static final Property<MetaColumn,Class<? extends TypeService>> CONVERTER = newProperty("converter", Class.class).writeDefault(TypeService.class);
    /** Comment of the database column */
    public static final Property<MetaColumn,String> COMMENT = newProperty("comment", Comment.NULL);
    /** The property initialization */
    static{init(CLASS);}

    /** If current column is a foreign key than related model is a related table column (primarky key by default). */
    private List<MetaColumn> relatedModel;
    /** Foreign column names. */
    private String[] foreignNames = null;
    private static final String[] EMPTY_NAMES = new String[0];
    /** A TypeCode
     * @see TypeService
     */
    private char typeCode;
    private boolean foreignKey;
    final private TypeService converter;


    public MetaColumn() {
        this(null);
    }

    public MetaColumn(TypeService converter) {
        this.converter = converter;
    }

    public MetaColumn(MetaTable table, UjoProperty tableProperty, MetaColumn param) {
        super(table, tableProperty, param);
        this.foreignKey = isTypeOf(OrmUjo.class);

        Field field = UjoManager.getInstance().getPropertyField(table.getType(), tableProperty);
        Column column = field!=null ? field.getAnnotation(Column.class) : null;

        if (param!=null) {
            changeDefault(this, PRIMARY_KEY, PRIMARY_KEY.of(param));
            changeDefault(this, MANDATORY  , MANDATORY.of(param));
            changeDefault(this, MAX_LENGTH , MAX_LENGTH.of(param));
            changeDefault(this, PRECISION  , PRECISION.of(param));
            changeDefault(this, DB_TYPE    , DB_TYPE.of(param));
            changeDefault(this, INDEX      , INDEX.of(param));
            changeDefault(this, UNIQUE_INDEX,UNIQUE_INDEX.of(param));
            changeDefault(this, COMMENT    , COMMENT.of(param));
            changeDefault(this, CONVERTER  , CONVERTER.of(param));
        }
        if (column!=null) {
            changeDefault(this, PRIMARY_KEY, column.pk());
            changeDefault(this, MANDATORY  , column.mandatory());
            changeDefault(this, MAX_LENGTH , column.length());
            changeDefault(this, PRECISION  , column.precision());
            changeDefault(this, DB_TYPE    , column.type());
            changeDefault(this, INDEX      , column.index());
            changeDefault(this, UNIQUE_INDEX,column.uniqueIndex());
            changeDefault(this, CONSTRAINT_NAME, column.constraintName());
            changeDefault(this, CONVERTER  , column.converter());
        }

        if (DB_TYPE.isDefault(this)) {
            MetaTable.DATABASE.of(table).changeDbType(this);
        }
        if (MAX_LENGTH.isDefault(this)) {
            MetaTable.DATABASE.of(table).changeDbLength(this);
        }

        // Assign Comments:
        if (field!=null) {
            Comment comment = field.getAnnotation(Comment.class);
            if (comment!=null) changeDefault(this, COMMENT  , comment.value());
        }

        // Assign a Converter:
        converter = getHandler().getParameters().getConverter(CONVERTER.isDefault(this)
                ? null
                : CONVERTER.of(this))
                ;
    }

    /** It is a DB column (either a value of a foreign key), 
     * not a relation to many.
     */
    @Override
    public boolean isColumn() {
        return true;
    }

    /** Is it a Foreign Key ? */
    @Override
    public boolean isForeignKey() {
        return foreignKey;
    }

    /** Is it a Primary Key? */
    public boolean isPrimaryKey() {
        final boolean result = PRIMARY_KEY.of(this);
        return result;
    }

    /** Has the instance assigned a non empty comment? */
    public boolean isCommented() {
        return !COMMENT.isDefault(this);
    }

    /** Get a Comment from meta-model annotation.
     * @see org.ujorm.orm.annot.Comment
     */
    public String getComment() {
        return COMMENT.of(this);
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
    public MetaTable getForeignTable() {
        return getForeignColumns().get(0).getTable();
    }

    /** Returns an original foreign columns in case a foreign column. */
    @SuppressWarnings("unchecked")
    public List<MetaColumn> getForeignColumns() {
        if (relatedModel==null) {
            assignForeignColumns();
        }
        return relatedModel;
    }


    /** Returns an original foreign columns in case a foreign column. */
    @SuppressWarnings("unchecked")
    private void assignForeignColumns() {
        List<MetaColumn> result;
        final Class type = getProperty().getType();

        MetaTable table;
        if (TABLE_PROPERTY.of(this) instanceof RelationToOne) {
            RelationToOne rto = (RelationToOne) TABLE_PROPERTY.of(this);
            MetaColumn mc = (MetaColumn) getHandler().findColumnModel(rto.getRelatedKey());
            result = new ArrayList<MetaColumn>(1);
            result.add(mc);
        } else {
            table = getHandler().findTableModel(type);
            if (table!=null) {
                MetaPKey pk = MetaTable.PK.of(table);
                result = MetaPKey.COLUMNS.getList(pk);
            } else {
                result = Collections.emptyList();
            }
        }
        relatedModel = result;
    }

    /** Returns names of foreign columns.
     * <br>TODO: Is a time to an optimalization ?
     */
    @SuppressWarnings("unchecked")
    private String[] getForeignColumnNames() {
        if (foreignNames==null) {
            if (isForeignKey()) {
                List<MetaColumn> dbColumns = getForeignColumns();
                final StringTokenizer tokenizer = new StringTokenizer(dbColumns.size()==1 ? NAME.of(this) : "", ", ");

                ArrayList<String> fNames = new ArrayList<String>(dbColumns.size());
                for (MetaColumn dbColumn : dbColumns) {
                    String name;
                    if (tokenizer.hasMoreTokens()) {
                        name = tokenizer.nextToken();
                    } else {
                        name = "fk_"
                          // + MetaTable.NAME.of(foreignTable)
                             + MetaColumn.NAME.of(this)
                             + "_"
                             + MetaColumn.NAME.of(dbColumn)
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
    public Object getValue(final OrmUjo bo) {
        final UjoProperty property = getProperty();
        final Object result = property.of(bo);
        return result;
    }

    /** Returns a property value from a table */
    @SuppressWarnings("unchecked")
    public void setValue(final Ujo bo, Object value) {
        final UjoProperty property = getProperty();

        if (isForeignKey()
        &&   value !=null
        && !(value instanceof OrmUjo)) {
           value = new ForeignKey(value);
        }

        property.setValue(bo, value);
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

    /** Returns a DB, TABLE and COLUMN name */
    public String getFullName() {
        try {
            StringBuilder out = new StringBuilder(32);
            MetaTable table = TABLE.of(this);
            table.getDatabase()
                .getDialect()
                .printFullTableName(table, out);
            out.append('.');
            out.append(MetaColumn.NAME.of(this));
            return out.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Returns an ALIAS of table and COLUMN name. */
    public String getAliasName() {
        try {
            final String result = TABLE.of(this)
                .getDatabase()
                .getDialect()
                .printColumnAlias(this, new StringBuilder(32))
                .toString()
                ;
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Print a full 'alias' name of foreign column by index */
    public void printForeignColumnFullName(int index, Appendable out) throws IOException {
        MetaTable table = TABLE.of(this);
        out.append(table.getAlias());
        out.append('.');
        out.append(getForeignColumnNames()[index]);
    }

    /** A TypeCode
     * @see TypeService
     */
    public char getTypeCode() {
        return typeCode;
    }

    /** Has the property a default value (not null) ?
     * If the default value is an empty String than method returns false.
     */
    public boolean hasDefaultValue() {
        final Object value = getProperty().getDefault();
        boolean result = value instanceof String
            ?  ((String)value).length()>0
            : value!=null
            ;
        return result;
    }

    /** Returns a default value in a JDBC friendly type.
     * The real result type depends in an implementatin a TypeService.
     * For example a Java Enumerator default value can return either the Integer or String type too.
     * @see TypeService
     */
    public Object getJdbcFriendlyDefaultValue() {
        final Object result = new UjoStatement().getDefaultValue(this);
        return result;
    }

    /** Returns a SQL dialect class from a related Database */
    public Class getDialectClass() {
         final Class result = MetaColumn.TABLE.of(this).getDatabase().get(MetaDatabase.DIALECT);
         return result;
    }

    /** Returns a SQL dialect class from a related Database */
    public String getDialectName() {
         return getDialectClass().getSimpleName();
    }

    /** Initialize a type code - for an internal use only. */
    public void initTypeCode() {
        // Test for a read-only state:
        checkReadOnly(true);

        // Assign a type code:
        typeCode = converter.getTypeCode(this);

        // Modify a relation type:
        if (isForeignKey()) {
            List<MetaColumn> cols = getForeignColumns();
            if (cols.size()>0) {
                DB_TYPE.setValue(this, DB_TYPE.of(cols.get(0)));
            }
        }
    }

    /** Is the related property type void? */
    public boolean isVoid() {
        return isTypeOf(Void.class);
    }

    /** Returns a constraint name for case a foreigh key */
    public String getConstraintName() {
        return CONSTRAINT_NAME.of(this);
    }

    /** Returna not null converter */
    public TypeService getConverter() {
        return converter;
    }
}
