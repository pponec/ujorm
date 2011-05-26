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
package org.ujoframework.orm.metaModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.annot.Table;
import org.ujoframework.orm.annot.View;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.UjoSequencer;
import org.ujoframework.orm.annot.Comment;
import org.ujoframework.orm.ao.Orm2ddlPolicy;


/**
 * DB table or view meta-model.
 * @author Pavel Ponec
 * @composed 1 - * MetaRelation2Many
 * @composed 1 - * MetaColumn
 * @composed 1 - 1 MetaPKey
 * @composed 1 - * MetaIndex
 */
final public class MetaTable extends AbstractMetaModel {
    private static final Class CLASS = MetaTable.class;


    /** The meta-model id */
    @XmlAttribute
    public static final Property<MetaTable,String> ID = newProperty("id", Table.NULL);
    /** DB table name */
    public static final Property<MetaTable,String> NAME = newProperty("name", Table.NULL);
    /** The unique table/view name over all Databases in scope one OrmHandler */
    public static final Property<MetaTable,String> ALIAS = newProperty("alias", Table.NULL);
    /** Name of table schema. */
    public static final Property<MetaTable,String> SCHEMA = newProperty("schema", Table.NULL);
    /** The state read-only for the database. */
    public static final Property<MetaTable,Boolean> READ_ONLY = newProperty("readOnly", false);
    /** A policy to defining the database structure by a DDL.
     * @see Orm2ddlPolicy Parameter values
     */
    public static final Property<MetaTable,Orm2ddlPolicy> ORM2DLL_POLICY = newProperty("orm2ddlPolicy", Orm2ddlPolicy.INHERITED);
    /** Name of DB sequence. The value is not used by default,
     * however a special implementation of the UjoSequencer can do it. */
    public static final Property<MetaTable,String> SEQUENCE = newProperty("sequence", Table.NULL);
    /** Table Columns (no relations) */
    public static final ListProperty<MetaTable,MetaColumn> COLUMNS = newListProperty("column", MetaColumn.class);
    /** Table relations to many */
    public static final ListProperty<MetaTable,MetaRelation2Many> RELATIONS = newListProperty("relation2m", MetaRelation2Many.class);
    /** Is it a model of a database view or table ? */
    @XmlAttribute
    public static final Property<MetaTable,Boolean> VIEW = newProperty("view", false);
    /** SQL SELECT statement */
    public static final Property<MetaTable,String> SELECT = newProperty("select", "");
    /** Comment of the database table */
    public static final Property<MetaTable,String> COMMENT = newProperty("comment", Comment.NULL);
    /** SQL SELECT model. Note: this property must not be persistent due a blank spaces in key names! */
    @Transient
    public static final Property<MetaTable,MetaSelect> SELECT_MODEL = newProperty("selectModel", MetaSelect.class);
    /** Unique Primary Key */
    @Transient
    public static final Property<MetaTable,MetaPKey> PK = newProperty("pk", MetaPKey.class);
    /** Database relative <strong>property</strong> (a base definition of table) */
    @Transient
    public static final Property<MetaTable,RelationToMany> DB_PROPERTY = newProperty("dbProperty", RelationToMany.class);
    /** Database */
    @Transient
    public static final Property<MetaTable,MetaDatabase> DATABASE = newProperty("database", MetaDatabase.class);
    /** The property initialization */
    static{init(CLASS);}

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
     * @param dbProperty Configuration property
     * @param parTable Configuration data from a XML file

     */
    @SuppressWarnings({"unchecked", "LeakingThisInConstructor"})
    public MetaTable(MetaDatabase database, RelationToMany dbProperty, MetaTable parTable) {
        sequencer = database.createSequencer(this);
        ID.setValue(this, dbProperty.getName());
        DATABASE.setValue(this, database);
        DB_PROPERTY.setValue(this, dbProperty);

        final Field field = UjoManager.getInstance().getPropertyField(MetaDatabase.ROOT.of(database), dbProperty);

        View view1 = field.getAnnotation(View.class);
        View view2 = (View) dbProperty.getItemType().getAnnotation(View.class);
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

            if (!SELECT.isDefault(this)) {
                SELECT_MODEL.setValue(this, new MetaSelect(SELECT.of(this)));
            }
        } else {
            Table table1 = field.getAnnotation(Table.class);
            Table table2 = (Table) dbProperty.getItemType().getAnnotation(Table.class);
            if (table1!=null) changeDefault(this, NAME  , table1.name());
            if (table1!=null) changeDefault(this, NAME  , table1.value());
            if (table1!=null) changeDefault(this, ALIAS , table1.alias());
            if (table1!=null) changeDefault(this, SCHEMA, table1.schema());
            if (table1!=null) changeDefault(this, READ_ONLY, table1.readOnly());
            if (table1!=null) changeDefault(this, ORM2DLL_POLICY, table1.orm2ddlPolicy());
            if (table1!=null) changeDefault(this, SEQUENCE,table1.sequence());
            if (table2!=null) changeDefault(this, NAME  , table2.name());
            if (table2!=null) changeDefault(this, NAME  , table2.value());
            if (table2!=null) changeDefault(this, ALIAS , table2.alias());
            if (table2!=null) changeDefault(this, SCHEMA, table2.schema());
            if (table2!=null) changeDefault(this, READ_ONLY, table2.readOnly());
            if (table2!=null) changeDefault(this, ORM2DLL_POLICY, table2.orm2ddlPolicy());
            if (table2!=null) changeDefault(this, SEQUENCE,table2.sequence());
        }

        changeDefault(this, SCHEMA, MetaDatabase.SCHEMA.of(database));
        changeDefault(this, READ_ONLY, MetaDatabase.READ_ONLY.of(database));
        changeDefault(this, ORM2DLL_POLICY, MetaDatabase.ORM2DLL_POLICY.of(database));
        changeDefault(this, NAME, dbProperty.getName());
        String aliasPrefix = MetaParams.TABLE_ALIAS_PREFIX.of(database.getParams());
        String aliasSuffix = MetaParams.TABLE_ALIAS_SUFFIX.of(database.getParams());
        changeDefault(this, ALIAS, aliasPrefix+NAME.of(this)+aliasSuffix);

        // Assign Comments:
        Comment comment1 = field.getAnnotation(Comment.class);
        Comment comment2 = (Comment) dbProperty.getItemType().getAnnotation(Comment.class);
        if (comment1!=null) changeDefault(this, COMMENT  , comment1.value());
        if (comment2!=null) changeDefault(this, COMMENT  , comment2.value());

        // -----------------------------------------------

        MetaPKey dpk = new MetaPKey(this);
        PK.setValue(this, dpk);

        OrmHandler dbHandler = database.getOrmHandler();
        UjoManager ujoManager = UjoManager.getInstance();
        for (UjoProperty property : ujoManager.readProperties(dbProperty.getItemType())) {

            if (!ujoManager.isTransientProperty(property)) {

                if (property instanceof RelationToMany) {
                    MetaRelation2Many param = parTable!=null ? parTable.findRelation(property.getName()) : null;
                    MetaRelation2Many column = new MetaRelation2Many(this, property, param);
                    RELATIONS.addItem(this, column);
                    dbHandler.addColumnModel(column);

                } else {
                    MetaColumn param  = parTable!=null ? parTable.findColumn(property.getName()) : null;
                    MetaColumn column = new MetaColumn(this, property, param);
                    COLUMNS.addItem(this, column);
                    dbHandler.addColumnModel(column);

                    if (MetaColumn.PRIMARY_KEY.of(column)) {
                        MetaPKey.COLUMNS.addItem(dpk, column);
                    }
                }
            }
        }
    }

    /** Assign a PK from framework */
    public void assignPrimaryKey(final OrmUjo bo, final Session session) {
        final Class type = getType();
        if (type.isInstance(bo)) {
            try {
               final MetaPKey pk = PK.of(this);
               pk.assignPrimaryKey(bo, session);
            } catch (Throwable e) {
               throw new IllegalArgumentException("DB SEQUENCE is not supported for " + type, e);
            }
        } else {
            throw new IllegalArgumentException("Argument is not type of " + type);
        }
    }
    
    /** Returns a new instance or the BO. */
    public OrmUjo createBO() throws InstantiationException, IllegalAccessException {
        final OrmUjo result = getType().newInstance();
        return result;
    }

    /** Returns a base table class. */
    @SuppressWarnings("unchecked")
    final public Class<OrmUjo> getType() {
        return DB_PROPERTY.of(this).getItemType();
    }

    /** Returns the first PK */
    public MetaColumn getFirstPK() {
        return PK.of(this).getFirstColumn();
    }

    /** Is the instance a database relation model? */
    public boolean isPersistent() {
        return DATABASE.of(this)!=null;
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

    /** Is the instance a database relation model? */
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

    /** Get a Comment from meta-model annotation.
     * @see org.ujoframework.orm.annot.Comment
     */
    public String getComment() {
        return COMMENT.of(this);
    }

    /** Is the query from a SQL select model ? */
    public boolean isSelectModel() {
        return SELECT_MODEL.of(this)!=null;
    }

    /** Database model is not persistent. A side efect is that the DATABASE property has hot a null value. */
    public void setNotPersistent() {
        DATABASE.setValue(this, null);
    }

    /** Returns a unique table name over all Databases of the one OrmHandler. */
    public String getAlias() {
        return ALIAS.of(this);
    }

    /** Returns the database */
    final public MetaDatabase getDatabase() {
        return DATABASE.of(this);
    }

    /** Compare object by the same instance. */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return this==obj;
    }

    /** Finds the first column by ID or returns null. The method is for internal use only. */
    MetaColumn findColumn(String id) {

        if (isUsable(id)) for (MetaColumn column : COLUMNS.of(this)) {
            if (MetaColumn.ID.equals(column, id)) {
                return column;
            }
        }
        return null;
    }

    /** Finds the first relation by ID or returns null. The method is for internal use only. */
    MetaRelation2Many findRelation(String id) {

        if (isUsable(id)) for (MetaRelation2Many relation : RELATIONS.of(this)) {
            if (MetaRelation2Many.ID.equals(relation, id)) {
                return relation;
            }
        }
        return null;
    }

    /** Get all foreign columns */
    public List<MetaColumn> getForeignColumns() {
        final List<MetaColumn> result = new ArrayList<MetaColumn>();
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

    /** Create a collection of the table indexes. */
    public Collection<MetaIndex> getIndexCollection() {
        Map<String,MetaIndex> mapIndex = new HashMap<String,MetaIndex>();

        for (MetaColumn column : COLUMNS.getList(this)) {
            String[] idxs = {MetaColumn.INDEX.of(column), MetaColumn.UNIQUE_INDEX.of(column)};

            for (int i=0; i<2; ++i) {
                if (idxs[i].length()>0) {
                    String upperIdx = idxs[i].toUpperCase();
                    MetaIndex mIndex = mapIndex.get(upperIdx);
                    if (mIndex==null) {
                        mIndex = new MetaIndex(idxs[i], this);
                        mapIndex.put(upperIdx, mIndex);
                    }
                    if (i==0) {
                        MetaIndex.UNIQUE.setValue(mIndex, false);
                    } else if (upperIdx.equalsIgnoreCase(idxs[0])) {
                        break; // Ignore the same column in the index.
                    }
                    MetaIndex.COLUMNS.addItem(mIndex, column);
                }
            }
        }
        return mapIndex.values();
    }

    /** Returns a parrent of the parameter or the null if no parent was not found.<br/>
     * The method provides a parent in case of emulated inheritance.
     */
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

    /** Asssert that the table may be changed. */
    public void assertChangeAllowed() {
        if (isReadOnly()) {
            final String msg = "The table '" + NAME.of(this) + "' have got the READ-ONLY mode. Check the Ujorm meta-model configuration.";
            throw new IllegalStateException(msg);
        }
    }
    
    /** Returns Orm2DDl policy */
    public Orm2ddlPolicy getOrm2ddlPolicy() {
        return ORM2DLL_POLICY.of(this);
    }

}
