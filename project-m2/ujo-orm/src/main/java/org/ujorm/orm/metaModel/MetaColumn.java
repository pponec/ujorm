/*
 *  Copyright 2009-2015 Pavel Ponec
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.ValueWrapper;
import org.ujorm.implementation.orm.RelationToOne;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.DbType;
import org.ujorm.orm.ForeignKey;
import org.ujorm.orm.ITypeService;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.SqlNameProvider;
import org.ujorm.orm.TableWrapper;
import org.ujorm.orm.TypeService;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.ao.QuoteEnum;
import org.ujorm.orm.ao.UjoStatement;
import org.ujorm.tools.Assert;
import org.ujorm.tools.MsgFormatter;
import org.ujorm.validator.ValidatorUtils;
import static org.ujorm.tools.Check.isEmpty;

/**
 * Database column meta-data
 * @author Pavel Ponec
 * @composed 1 - * DbType
 */
@Immutable
public final class MetaColumn extends MetaRelation2Many implements ColumnWrapper {
    private static final Class<MetaColumn> CLASS = MetaColumn.class;

    /** A constant for an auto index name from the {@link SqlNameProvider} generator
     * @see SqlNameProvider
     */
    public static final String AUTO_INDEX_NAME = "AUTO";

    /** Property Factory */
    private static final KeyFactory<MetaColumn> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** DB primary key */
    public static final Key<MetaColumn,Boolean> PRIMARY_KEY = fa.newKey("primaryKey", false);
    /** Column name is quoted */
    public static final Key<MetaColumn,QuoteEnum> QUOTED = fa.newKey("quoted", QuoteEnum.BY_CONFIG);
    /** Database Type */
    public static final Key<MetaColumn,DbType> DB_TYPE = fa.newKey("dbType", DbType.NULL);
    /** Column NOT-NULL */
    public static final Key<MetaColumn,Boolean> MANDATORY = fa.newKey("mandatory", false);
    /** Column value length */
    public static final Key<MetaColumn,Integer> MAX_LENGTH = fa.newKey("maxLength", -1);
    /** Column value precision */
    public static final Key<MetaColumn,Integer> PRECISION = fa.newKey("precision", -1);
    /** DB Default value */
    public static final Key<MetaColumn,String> DEFAULT_VALUE = fa.newKey("default", "");
    /** A name of the non-unique database index for the column, where the same index can contain more columns.
     * If the name is "AUTO" then automatic name based on column setting is generated.
     * If a single column of the index is marked as unique, so the entire index will be unique. */
    public static final ListKey<MetaColumn,String> INDEX = fa.newListKey("index");
    /** A name of the unique database index for the column, where the same index can contain more columns.
     * If the name is "AUTO" then automatic name based on column setting is generated.
     * If a single column of the index is marked as unique, so the entire index will be unique. */
    public static final ListKey<MetaColumn,String> UNIQUE_INDEX = fa.newListKey("uniqueIndex");
    /** A name of the constraint for the case a foreign key */
    public static final Key<MetaColumn,String> CONSTRAINT_NAME = fa.newKey("constraintName", "");
    /** Convert, save and read application data from/to the database */
    public static final Key<MetaColumn,Class<? extends ITypeService>> CONVERTER = fa.newClassKey("converter", ITypeService.class);
    /** Comment of the database column */
    public static final Key<MetaColumn,String> COMMENT = fa.newKey("comment", Comment.NULL);

    /** The key initialization */
    static {
        fa.lock();
    }

    /** If current column is a foreign key than related model is a related table column (primary key by default). */
    private List<MetaColumn> relatedModel;
    /** Foreign column names. */
    private String[] foreignNames = null;
    private static final String[] EMPTY_NAMES = new String[0];
    /** A <b>Java Type Code<b> to a quick JDBC management.
     * @see TypeService#getTypeCode(org.ujorm.orm.metaModel.MetaColumn) */
    private char typeCode;
    /** If the column is a {@code foreign key} */
    private final boolean foreignKey;
    /** If the type is instance of the class {@code } */
    private final boolean isValueWrapper;
    /** Type converter. Value is Notnull always. */
    private final ITypeService converter;


    public MetaColumn() {
        this(null);
    }

    public MetaColumn(ITypeService converter) {
        this.converter = converter;
        foreignKey = false;
        isValueWrapper = false;
    }

    @SuppressWarnings({"LeakingThisInConstructor", "unchecked"})
    public MetaColumn(@Nonnull final MetaTable table, @Nonnull final Key tableProperty, @Nullable final MetaColumn param) {
        super(table, tableProperty, param);
        this.foreignKey = isTypeOf(OrmUjo.class);
        this.isValueWrapper = isTypeOf(ValueWrapper.class);

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
            changeDefault(this, QUOTED     , QUOTED.of(param));
        }
        if (column!=null) {
            changeDefault(this, PRIMARY_KEY, column.pk());
            changeDefault(this, MANDATORY  , column.mandatory());
            changeDefault(this, MAX_LENGTH , column.length());
            changeDefault(this, PRECISION  , column.precision());
            changeDefault(this, DB_TYPE    , column.type());
            changeDefault(this, INDEX      , toList(column.index()));
            changeDefault(this, UNIQUE_INDEX,toList(column.uniqueIndex()));
            changeDefault(this, CONSTRAINT_NAME, column.constraintName());
            changeDefault(this, CONVERTER  , column.converter());
            changeDefault(this, QUOTED     , column.quoted());
        }

        final Validator validator = tableProperty.getValidator();
        if (validator != null) {
            changeDefault(this, MANDATORY , ValidatorUtils.isMandatoryValidator(validator));
            changeDefault(this, MAX_LENGTH, ValidatorUtils.getMaxLength(validator));
        }

        // Assign Comments:
        if (field != null) {
            final Comment comment = field.getAnnotation(Comment.class);
            if (comment != null) {
                changeDefault(this, COMMENT  , comment.value());
            }
        }

        // Assign the Converter:
        final Class converterType = CONVERTER.isDefault(this) ? null : CONVERTER.of(this);
        converter = getHandler().getParameters().getConverter(converterType);

        // DB Type must be assigned after to create the converter instance:
        if (DB_TYPE.isDefault(this)) {
            table.getDatabase().changeDbType(this);
        }

        // The MAX_LENGTH must be after the DB_TYPE:
        if (MAX_LENGTH.isDefault(this)) {
            table.getDatabase().changeDbLength(this);
        }

        // Quoted column name by configuraton:
        if (QUOTED.isDefault(this)) {
            switch (getHandler().getParameters().get(MetaParams.QUOTATION_POLICY)) {
                case QUOTE_SQL_NAMES:
                    QUOTED.setValue(this, QuoteEnum.YES);
                    break;
                case QUOTE_ONLY_SQL_KEYWORDS:
                    // String name = NAME.of(this);
                    // break;
                    QUOTED.setValue(this, QuoteEnum.BY_CONFIG);
                default:
                    QUOTED.setValue(this, QuoteEnum.NO);
                    break;
            }
        }
    }

    /** Create an UnmodifiableList */
    private static final List<String> toList(String[] items) {
        if (items == null || items.length == 0) {
            return Collections.emptyList();
        } else if (items.length == 1 && isEmpty(items[0])) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(Arrays.asList(items));
        }
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
    public final boolean isForeignKey() {
        return foreignKey;
    }

    /** Is the value type of ValueWrapper? */
    @Override
    public final boolean isValueWrapper() {
        return isValueWrapper;
    }

    /** Is it a Primary Key? */
    public final boolean isPrimaryKey() {
        return PRIMARY_KEY.of(this);
    }

    /** Returns true if the column is an optional relation */
    public final boolean isOptionalRelation() {
        return foreignKey && !MANDATORY.of(this);
    }

    /** Has the instance assigned a non empty comment? */
    public final boolean isCommented() {
        return !COMMENT.isDefault(this);
    }

    /** Get a Comment from meta-model annotation.
     * @see org.ujorm.orm.annot.Comment
     */
    public final String getComment() {
        return COMMENT.of(this);
    }

    /** Returns a maximal db column length in the database.
     * @return If key is undefined then the method returns value -1.
     */
    public final int getMaxLength() {
        return MAX_LENGTH.of(this);
    }

    /** Returns the db column precision.
     * @return If key is undefined then the method returns value -1.
     */
    public final int getPrecision() {
        return PRECISION.of(this);
    }

    /** Returns true if the related db column is NOT NULL. */
    public final boolean isMandatory() {
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
            relatedModel = createForeignColumns();
        }
        return relatedModel;
    }

    /** Returns an original foreign columns in case a foreign column.
     * @return Returns an original foreign columns in case a foreign column.
     * @throws IllegalStateException The relation column have no foreign keys! */
    @SuppressWarnings("unchecked")
    private List<MetaColumn> createForeignColumns() throws IllegalUjormException {
        List<MetaColumn> result;

        MetaTable table;
        if (getKey() instanceof RelationToOne) {
            RelationToOne rto = (RelationToOne) TABLE_KEY.of(this);
            MetaColumn mc = (MetaColumn) getHandler().findColumnModel(rto.getRelatedKey(), true);
            result = new ArrayList<>(1);
            result.add(mc);
        } else {
            table = getHandler().findTableModel(getType());
            if (table!=null) {
                MetaPKey pk = MetaTable.PK.of(table);
                result = MetaPKey.COLUMNS.getList(pk);
            } else {
                result = Collections.emptyList();
            }
        }

        Assert.hasLength(result, "The relation column {} have no foreign keys", this);
        return result;
    }

    /** Returns names of foreign columns.
     * <br>TODO: Is a time to an optimization ?
     */
    @SuppressWarnings("unchecked")
    private String[] getForeignColumnNames() {
        if (foreignNames == null) {
            if (isForeignKey()) {
                List<MetaColumn> dbColumns = getForeignColumns();
                final StringTokenizer tokenizer = new StringTokenizer(dbColumns.size()==1
                        ? getName() : "", ", ");

                ArrayList<String> fNames = new ArrayList<>(dbColumns.size());
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
    public final String getForeignColumnName(final int index) {
        return getForeignColumnNames()[index];
    }

    /** Returns a key value from a table */
    @SuppressWarnings("unchecked")
    public Object getValue(final OrmUjo bo) {
        final Key key = super.getKey();
        final Object result = key.of(bo);
        return result;
    }

    /** Returns a key value from a table
     * @param ujo Related Ujo object where a candiate to the ValueWrapper is supported
     * @param value A value to assign.
     */
    @SuppressWarnings("unchecked")
    public void setValueRaw(@Nonnull final Ujo bo, @Nullable final Object value) {
        if (isValueWrapper) {
            setValue(bo, ValueWrapper.getInstance(getType(), value));
        } else {
            setValue(bo, value);
        }
    }

    /** Returns a key value from a table
     * @param ujo Related Ujo object
     * @param value A value to assign.
     */
    @SuppressWarnings("unchecked")
    public void setValue(@Nonnull final Ujo bo, @Nullable Object value) {
        final Key key = super.getKey();

        if (isForeignKey()
        &&   value !=null
        && !(value instanceof OrmUjo)) {
             value = new ForeignKey(value);
        }

        key.setValue(bo, value);
    }

    /** Returns a Java Class of value */
    public Class getType() {
        return TABLE_KEY.of(this).getType();
    }

    /** Returns a column name */
    @Override
    public final String getName() {
        return NAME.of(this);
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
            out.append(getName());
            return out.toString();
        } catch (IOException e) {
            throw new IllegalUjormException(e);
        }
    }

    /** Returns a full SQL column alias name by sample: "TABLE_ALIAS"."ORIG_COLUMN" */
    public String getColumnAlias() {
        try {
            final String result = TABLE.of(this)
                .getDatabase()
                .getDialect()
                .printColumnAlias(this, new StringBuilder(32))
                .toString();
            return result;
        } catch (IOException e) {
            throw new IllegalUjormException(e);
        }
    }

    /** Print a full 'alias' name of foreign column by index */
    public void printForeignColumnFullName(int index, Appendable out) throws IOException {
        SqlDialect dialect = TABLE.of(this)
                .getDatabase()
                .getDialect();
        dialect.printQuotedName(getTableAlias(), QuoteEnum.BY_CONFIG, out);
        out.append('.');
        dialect.printQuotedName(getForeignColumnNames()[index], QuoteEnum.BY_CONFIG, out);
    }

    /** A TypeCode
     * @see TypeService
     */
    public char getTypeCode() {
        return typeCode;
    }

    /** Has the key a default value (not null) ?
     * If the default value is an empty String than method returns false.
     */
    public boolean hasDefaultValue() {
        final Object value = super.getKey().getDefault();
        boolean result = value instanceof String
            ?  ((String)value).length()>0
            : value!=null
            ;
        return result;
    }

    /** Returns a default value in a JDBC friendly type.
     * The real result type depends in an implementation of the ITypeService.
     * For example a Java Enumerator default value can return either the Integer or String type too.
     * @see ITypeService
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

        // Assign the Type code:
        typeCode = TypeService.getTypeCode(this);

        // Modify a relation type:
        if (isForeignKey()) {
            List<MetaColumn> cols = getForeignColumns();
            if (cols.size()>0) {
                DB_TYPE.setValue(this, DB_TYPE.of(cols.get(0)));
            }
        }
    }

    /** Is the related key type void? */
    public boolean isVoid() {
        return isTypeOf(Void.class);
    }

    /** Returns a constraint name for case a foreign key */
    public String getConstraintName() {
        return CONSTRAINT_NAME.of(this);
    }

    /** Returns not {@code null} converter */
    public ITypeService getConverter() {
        return converter;
    }
    /** Returns a native database code for a DDL statements */
    public Class getDbTypeClass() {
        if (!readOnly()) {
            if (isForeignKey()) {
                // The foreign type is not initialized correctly yet.
                return getType();
            } else {
                this.initTypeCode();
            }
        }
        return converter.getDbTypeClass(this);
    }

    /** Get link to a column meta-model */
    @Override
    public MetaColumn getModel() {
        return this;
    }

    /** Build new table wrapper */
    @Override
    public TableWrapper buildTableWrapper() {
        return getTable().addAlias(getTableAlias());
    }

    /** Create new object column for the new alias
     * @param alias Nullable alias value
     * @return New instance of ColumnWrapper for a different alias.
     */
    public final ColumnWrapper addTableAlias(final String alias) {
        return alias != null
            ? ColumnWrapper.forAlias(this, alias)
            : this ;
    }

    /** Find a related key */
    @Nonnull
    public MetaColumn findRelatedColumn(@Nonnull final Session session) {
        if (!foreignKey) {
            final String msg = MsgFormatter.format("The {} column is not relation", this);
            throw new IllegalArgumentException(msg);
        }
        final Key localKey = TABLE_KEY.of(this);
        if (localKey instanceof RelationToOne) {
            final Key relatedKey = ((RelationToOne)localKey).getRelatedKey();
            return session.getHandler().findColumnModel(relatedKey, true);
        } else {
            final MetaTable relatedTable = session.getHandler().findTableModel(localKey.getType(), true);
            return MetaTable.PK.of(relatedTable).getFirstColumn();
        }
    }

    /** Quotation request */
    public boolean isQuoted() {
        switch (QUOTED.of(this)) {
            case YES:
                return true;
            default:
                return false;
        }
    }
}
