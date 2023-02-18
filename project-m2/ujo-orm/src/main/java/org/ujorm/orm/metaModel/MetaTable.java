/*
 *  Copyright 2009-2022 Pavel Ponec
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.core.annot.Transient;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;
import org.ujorm.orm.TableWrapper;
import org.ujorm.orm.UjoSequencer;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;
import org.ujorm.orm.annot.View;
import org.ujorm.orm.ao.Orm2ddlPolicy;
import org.ujorm.orm.ao.QuoteEnum;
import org.ujorm.orm.impl.TableWrapperImpl;
import org.ujorm.tools.Assert;
import org.ujorm.tools.msg.MsgFormatter;
import static org.ujorm.orm.metaModel.MetaParams.INDEX_MODEL_BUILDER;
import static org.ujorm.tools.Check.hasLength;

/**
 * DB table or view metamodel.
 * @author Pavel Ponec
 * @composed 1 - * MetaRelation2Many
 * @composed 1 - * MetaColumn
 * @composed 1 - 1 MetaPKey
 * @composed 1 - * MetaIndex
 */
@Unmodifiable
final public class MetaTable extends AbstractMetaModel implements TableWrapper {
    private static final Class<MetaTable> CLASS = MetaTable.class;

    /** Property Factory */
    private static final KeyFactory<MetaTable> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** The metamodel id */
    @XmlAttribute
    public static final Key<MetaTable,String> ID = fa.newKey("id", Table.NULL);
    /** Table name is quoted */
    public static final Key<MetaTable,String> NAME = fa.newKey("name", Table.NULL);
    /** Quoting request for table */
    public static final Key<MetaTable,QuoteEnum> QUOTED = fa.newKey("quoted", QuoteEnum.BY_CONFIG);
    /** The unique table/view name over all Databases in scope one OrmHandler */
    public static final Key<MetaTable,String> ALIAS = fa.newKey("alias", Table.NULL);
    /** Name of table schema. */
    public static final Key<MetaTable,String> SCHEMA = fa.newKey("schema", Table.NULL);
    /** The state read-only for the database. */
    public static final Key<MetaTable,Boolean> READ_ONLY = fa.newKey("readOnly", false);
    /** A policy to defining the database structure by a DDL.
     * @see Orm2ddlPolicy Parameter values
     */
    public static final Key<MetaTable,Orm2ddlPolicy> ORM2DLL_POLICY = fa.newKey("orm2ddlPolicy", Orm2ddlPolicy.INHERITED);
    /** Name of DB sequence. The value is not used by default,
     * however a special implementation of the UjoSequencer can do it. */
    public static final Key<MetaTable,String> SEQUENCE = fa.newKey("sequence", Table.NULL);
    /** Is the current object a model of a database view ? */
    @XmlAttribute
    public static final Key<MetaTable,Boolean> VIEW = fa.newKey("view", false);
    /** SQL SELECT statement */
    public static final Key<MetaTable,String> SELECT = fa.newKey("select", "");
    /** Comment of the database table */
    public static final Key<MetaTable,String> COMMENT = fa.newKey("comment", Comment.NULL);
    /** Table Columns (no relations) */
    public static final ListKey<MetaTable,MetaColumn> COLUMNS = fa.newListKey("column");
    /** Table relations to many */
    public static final ListKey<MetaTable,MetaRelation2Many> RELATIONS = fa.newListKey("relation2m");

    /** SQL SELECT model. Note: this key must not be persistent due a blank spaces in key names! */
    @Transient
    public static final Key<MetaTable,MetaSelect> SELECT_MODEL = fa.newKey("selectModel");
    /** Unique Primary Key */
    @Transient
    public static final Key<MetaTable,MetaPKey> PK = fa.newKey("pk");
    /** Database relative <strong>key</strong> (a base definition of table) */
    @Transient
    public static final Key<MetaTable,RelationToMany> DB_PROPERTY = fa.newKey("dbProperty");
    /** Database */
    @Transient
    public static final Key<MetaTable,MetaDatabase> DATABASE = fa.newKey("database");

    /** The Key initialization */
    static{fa.lock();}

    /** Ujo sequencer */
    final private UjoSequencer sequencer;

    /** Cache of the parameter. */
    private Boolean sequenceSchemaSymbol;

    /** No parameter constructor. */
    public MetaTable() {
        sequencer = null;
    }

    /**
     * Create new MetaTable.
     * @param database Database for the table
     * @param dbProperty Configuration key
     * @param parTable Configuration data from a XML file
     */
    @SuppressWarnings({"unchecked", "LeakingThisInConstructor"})
    public MetaTable(MetaDatabase database, RelationToMany<?,?> dbProperty, MetaTable parTable) {
        sequencer = database.createSequencer(this);
        ID.setValue(this, dbProperty.getName());
        DATABASE.setValue(this, database);
        DB_PROPERTY.setValue(this, dbProperty);

        final Field field = UjoManager.getPropertyField(MetaDatabase.ROOT.of(database), dbProperty);
        View view1 = field!=null ? field.getAnnotation(View.class) : null;
        View view2 = dbProperty.getItemType().getAnnotation(View.class);
        VIEW.setValue(this, view1!=null || view2!=null);

        if (parTable!=null) {
            changeDefault(this, NAME  , NAME.of(parTable));
            changeDefault(this, ALIAS , ALIAS.of(parTable));
            changeDefault(this, SCHEMA, SCHEMA.of(parTable));
            changeDefault(this, READ_ONLY, READ_ONLY.of(parTable));
            changeDefault(this, ORM2DLL_POLICY, ORM2DLL_POLICY.of(parTable));
            changeDefault(this, SEQUENCE,SEQUENCE.of(parTable));
            changeDefault(this, SELECT, SELECT.of(parTable));
            changeDefault(this, VIEW  , VIEW.of(parTable));
            changeDefault(this, COMMENT, COMMENT.of(parTable));
            changeDefault(this, QUOTED, QUOTED.of(parTable));
        }

        if (VIEW.of(this)) {
            if (view1!=null) changeDefault(this, NAME  , view1.name());
            if (view1!=null) changeDefault(this, NAME  , view1.value());
            if (view1!=null) changeDefault(this, ALIAS , view1.alias());
            if (view1!=null) changeDefault(this, SCHEMA, view1.schema());
            if (view1!=null) changeDefault(this, SELECT, view1.select());
            if (view2!=null) changeDefault(this, NAME  , view2.name());
            if (view2!=null) changeDefault(this, NAME  , view2.value());
            if (view2!=null) changeDefault(this, ALIAS , view2.alias());
            if (view2!=null) changeDefault(this, SCHEMA, view2.schema());
            if (view2!=null) changeDefault(this, SELECT, view2.select());
        } else {
            Table table1 = field!=null ? field.getAnnotation(Table.class) : null;
            Table table2 = dbProperty.getItemType().getAnnotation(Table.class);
            if (table1!=null) changeDefault(this, NAME  , table1.name());
            if (table1!=null) changeDefault(this, NAME  , table1.value());
            if (table1!=null) changeDefault(this, ALIAS , table1.alias());
            if (table1!=null) changeDefault(this, SCHEMA, table1.schema());
            if (table1!=null) changeDefault(this, READ_ONLY, table1.readOnly());
            if (table1!=null) changeDefault(this, ORM2DLL_POLICY, table1.orm2ddlPolicy());
            if (table1!=null) changeDefault(this, SEQUENCE,table1.sequence());
            if (table1!=null) changeDefault(this, QUOTED, table1.quoted());

            if (table2!=null) changeDefault(this, NAME  , table2.name());
            if (table2!=null) changeDefault(this, NAME  , table2.value());
            if (table2!=null) changeDefault(this, ALIAS , table2.alias());
            if (table2!=null) changeDefault(this, SCHEMA, table2.schema());
            if (table2!=null) changeDefault(this, READ_ONLY, table2.readOnly());
            if (table2!=null) changeDefault(this, ORM2DLL_POLICY, table2.orm2ddlPolicy());
            if (table2!=null) changeDefault(this, SEQUENCE,table2.sequence());
            if (table2!=null) changeDefault(this, QUOTED, table2.quoted());
        }

        changeDefault(this, SCHEMA, MetaDatabase.SCHEMA.of(database));
        changeDefault(this, READ_ONLY, MetaDatabase.READ_ONLY.of(database));
        changeDefault(this, ORM2DLL_POLICY, MetaDatabase.ORM2DLL_POLICY.of(database));
        changeDefault(this, NAME, dbProperty.getName());
        String aliasPrefix = MetaParams.TABLE_ALIAS_PREFIX.of(database.getParams());
        String aliasSuffix = MetaParams.TABLE_ALIAS_SUFFIX.of(database.getParams());
        changeDefault(this, ALIAS, aliasPrefix+NAME.of(this)+aliasSuffix);

        // Assign Comments:
        Comment comment1 = field!=null ? field.getAnnotation(Comment.class) : null;
        Comment comment2 = dbProperty.getItemType().getAnnotation(Comment.class);
        if (comment1!=null) changeDefault(this, COMMENT  , comment1.value());
        if (comment2!=null) changeDefault(this, COMMENT  , comment2.value());

        if (VIEW.of(this) && !SELECT.isDefault(this)) {
            SELECT_MODEL.setValue(this, new MetaSelect(this));
        }

        // -----------------------------------------------

        MetaPKey dpk = new MetaPKey(this);
        PK.setValue(this, dpk);

        OrmHandler dbHandler = database.getOrmHandler();
        UjoManager ujoManager = UjoManager.getInstance();
        UjoManager.newInstance(dbProperty.getItemType()); // Initialize static Keys
        for (Key key : getColumns(dbProperty.getItemType())) {

            if (!ujoManager.isTransient(key)) {
                final String keyName = key.getName();

                if (key instanceof RelationToMany) {
                    MetaRelation2Many param = parTable!=null ? parTable.findRelation(keyName) : null;
                    MetaRelation2Many column = new MetaRelation2Many(this, key, param);
                    RELATIONS.addItem(this, column);
                    dbHandler.addColumnModel(column);

                } else {
                    MetaColumn param  = parTable!=null ? parTable.findColumn(keyName) : null;
                    MetaColumn column = new MetaColumn(this, key, param);
                    COLUMNS.addItem(this, column);
                    dbHandler.addColumnModel(column);

                    if (MetaColumn.PRIMARY_KEY.of(column)) {
                        MetaPKey.COLUMNS.addItem(dpk, column);
                    }
                }
            }
        }

        // Quoted table name by configuraton:
        if (QUOTED.isDefault(this)) {
            switch (dbHandler.getParameters().get(MetaParams.QUOTATION_POLICY)) {
                case QUOTE_SQL_NAMES:
                    QUOTED.setValue(this, QuoteEnum.YES);
                    break;
                case QUOTE_ONLY_SQL_KEYWORDS:
                    QUOTED.setValue(this, QuoteEnum.BY_CONFIG);
                    break;
                default:
                    QUOTED.setValue(this, QuoteEnum.NO);
                    break;
            }
        }
    }

    /** Returns all columns from the class  */
    @SuppressWarnings("unchecked")
    private Iterable<Key<Ujo,Object>> getColumns(Class ujoClass) {
        return UjoManager.getInstance().readKeys(ujoClass);
    }

    /** Assign a PK from framework */
    public void assignPrimaryKey(final OrmUjo bo, final Session session) {
        final Class type = getType();
        Assert.isTrue(type.isInstance(bo), "Argument is not type of {}", type);

        try {
           final MetaPKey pk = PK.of(this);
           pk.assignPrimaryKey(bo, session);
        } catch (RuntimeException | OutOfMemoryError e) {
           throw new IllegalUjormException("DB SEQUENCE is not supported for " + type, e);
        }
    }

    /** Returns a new instance or the BO. */
    public OrmUjo createBO() throws ReflectiveOperationException {
        final OrmUjo result = getType().newInstance();
        return result;
    }

    /** Returns a base table class. */
    @SuppressWarnings("unchecked")
    public Class<OrmUjo> getType() {
        return DB_PROPERTY.of(this).getItemType();
    }

    /** Returns the first PK */
    public MetaColumn getFirstPK() {
        return PK.of(this).getFirstColumn();
    }

    /** Is the instance a database relation model? */
    public boolean isPersistent() {
        return getDatabase()!=null;
    }

    /** Has this table assigned the database default database schema ? */
    public boolean isDefaultSchema() {
        if (sequenceSchemaSymbol==null) {
            sequenceSchemaSymbol = getDatabase().getParams().get(MetaParams.SEQUENCE_SCHEMA_SYMBOL);
        }
        if (sequenceSchemaSymbol) {
            final String tableSchema = SCHEMA.of(this);
            final String defaultSchema = MetaDatabase.SCHEMA.of(getDatabase());
            return tableSchema.equals(defaultSchema);
        } else {
            return false;
        }
    }

    /** Is the current object a model of a database view ? */
    @Override
    public boolean isView() {
        return VIEW.of(this);
    }

    /** Is the instance a database persistent table?
     * The false value means that the object is a relation model or a view.
     */
    public boolean isTable() {
        return isPersistent() && !isView();
    }

    /** Has the instance assigned a non empty comment? */
    public boolean isCommented() {
        return !COMMENT.isDefault(this);
    }

    /** Get a Comment from metamodel annotation.
     * @see org.ujorm.orm.annot.Comment
     */
    public String getComment() {
        return COMMENT.of(this);
    }

    /** Is the query from a SQL select model ? */
    public boolean isSelectModel() {
        return SELECT_MODEL.of(this)!=null;
    }

    /** Database model is not persistent. A side efect is that the DATABASE key has hot a null value. */
    public void setNotPersistent() {
        DATABASE.setValue(this, null);
    }

    /** Returns a unique table name over all Databases of the one OrmHandler. */
    public String getAlias() {
        return ALIAS.of(this);
    }

    /** Returns the database */
    public MetaDatabase getDatabase() {
        return DATABASE.of(this);
    }

    /** Compare object by the same instance. */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return this==obj;
    }

    /** Finds the first column by ID or returns null. The method is for internal use only. */
    @Nullable
    MetaColumn findColumn(String id) {

        if (hasLength(id)) for (MetaColumn column : COLUMNS.getList(this)) {
            if (MetaColumn.ID.equals(column, id)) {
                return column;
            }
        }
        return null;
    }

    /** Finds the first relation by ID or returns null. The method is for internal use only. */
    MetaRelation2Many findRelation(String id) {

        if (hasLength(id)) for (MetaRelation2Many relation : RELATIONS.getList(this)) {
            if (MetaRelation2Many.ID.equals(relation, id)) {
                return relation;
            }
        }
        return null;
    }

    /** Get all foreign columns */
    public List<MetaColumn> getForeignColumns() {
        final List<MetaColumn> result = new ArrayList<>();
        for (MetaColumn column : COLUMNS.getList(this)) {
            if (column.isForeignKey()) {
                result.add(column);
            }
        }
        return result;
    }

    /** UJO sequencer */
    public UjoSequencer getSequencer() {
        return sequencer;
    }

    /** Returns database schema.
     * @since 1.72
     * @return No schema returns an empty value
     */
    @NotNull
    public String getSchema() {
        return SCHEMA.of(this);
    }

     /** Returns database schema.
     * @since 1.75
     * @return No schema returns an empty value
     */
    @NotNull
    public String getName() {
        return NAME.of(this);
    }

    /** Returns a table name including a database schema */
    public String getFullName() {
        final String schema = SCHEMA.of(this);
        return MsgFormatter.format("{}.{}", schema != null ? schema : "public", getName());
    }

    /** Returns schema + table */
    @Override @NotNull
    public String toString() {
        return getFullName();
    }

    /** Create a new collection of the table indexes.
     * @return Collection of the MetaIndex objects */
    public Collection<MetaIndex> getIndexCollection() {
        final MetaParams params = getDatabase().getParams();
        try {
            return params.getIndexModelBuilder(this).getIndexModels();
        } catch (Exception e) {
            final String msg = MsgFormatter.format
                    ( "The {} with an entity {} fails, see the parameter {}"
                    , params.get(INDEX_MODEL_BUILDER)
                    , getType().getSimpleName()
                    , INDEX_MODEL_BUILDER.getFullName()
            );
            throw new IllegalUjormException(msg, e);
        }
    }

    /** Returns a parent of the parameter or the null if no parent was not found.<br>
     * The method provides a parent in case of emulated inheritance.
     */
    @Nullable
    public OrmUjo getParent(final OrmUjo bo) {

        final MetaColumn metaColumn = getFirstPK();
        if (metaColumn.isForeignKey()) {
            return (OrmUjo) metaColumn.getValue(bo);
        } else {
            return null;
        }
    }

    /** Have the table got a READ-ONLU mode ? */
    public boolean isReadOnly() {
        return READ_ONLY.of(this);
    }

    /** Assert that the table may be changed. */
    public void assertChangeAllowed() throws IllegalUjormException {
        if (isReadOnly()) {
            final String msg = MsgFormatter.format("The table '{}' have got the READ-ONLY mode. Check the {} metamodel configuration."
                , NAME.of(this)
                , "Ujorm");
            throw new IllegalUjormException(msg);
        }
    }

    /** Returns Orm2DDl policy */
    public Orm2ddlPolicy getOrm2ddlPolicy() {
        return ORM2DLL_POLICY.of(this);
    }

    /** Return an instance of Meta Model */
    @Override
    public MetaTable getModel() {
        return this;
    }

    /** Returns all columns */
    @Override
    public List<? extends ColumnWrapper> getColumns() {
        return MetaTable.COLUMNS.getList(this);
    }

    /** Unlock the metamodel, the method is for internal use only.
     * The method must be enabled by parameter: {@link MoreParams#ENABLE_TO_UNLOCK_IMMUTABLE_METAMODEL}.
     */
    public void clearReadOnly() {
        super.clearReadOnly(this.getDatabase().getOrmHandler());
    }

    /** Add alias name to the new object */
    public TableWrapper addAlias(final String alias) {
        return alias != null
             ? new TableWrapperImpl(this, alias)
             : this ;
    }

    /** Quotation request */
    public boolean isQuoted() {
        return QUOTED.of(this) == QuoteEnum.YES;
    }

}
