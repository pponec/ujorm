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
package org.ujorm.orm.metaModel;

import java.lang.reflect.Field;
import javax.annotation.concurrent.Immutable;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.core.annot.Transient;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.DbProcedure;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.annot.Parameter;
import org.ujorm.orm.annot.Procedure;
import org.ujorm.orm.annot.Table;


/**
 * DB procudure or function meta-model.
 * @author Pavel Ponec
 * @composed 1 - * MetaColumn
 */
@Immutable
final public class MetaProcedure extends AbstractMetaModel {
    private static final Class<MetaProcedure> CLASS = MetaProcedure.class;


    /** Property Factory */
    private static final KeyFactory<MetaProcedure> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** The meta-model id */
    @XmlAttribute
    public static final Key<MetaProcedure,String> ID = fa.newKey("id", Table.NULL);
    /** Procedure name */
    public static final Key<MetaProcedure,String> NAME = fa.newKey("name", Table.NULL);
    /** Name of table schema. */
    public static final Key<MetaProcedure,String> SCHEMA = fa.newKey("schema", Table.NULL);
    /** Procedure parameters */
    public static final ListKey<MetaProcedure,MetaColumn> PARAMETERS = fa.newListKey("parameter");
    /** Procedure <strong>key</strong> (a base definition of the procedure) */
    @Transient
    public static final Key<MetaProcedure,Key> DB_PROPERTY = fa.newKey("dbProperty");
    /** Database */
    @Transient
    public static final Key<MetaProcedure,MetaDatabase> DATABASE = fa.newKey("database");
    /** Dummy relation for internal use only. The field is NOT Key. */
    @Transient
    @SuppressWarnings("unchecked")
    private static final RelationToMany r2m = new RelationToMany("[PROCEDURE]", DbProcedure.class);

    /** The key initialization */
    static{ fa.lock(); }

    /** Full procedure name */
    private String procedureName;
    /** Input signs */
    final boolean[] input;
    /** Output signs */
    final boolean[] output;


    /** No parameter constructor. */
    public MetaProcedure() {
        input = new boolean[readKeys().size()];
        output = new boolean[input.length];
        procedureName = "";
    }

    /**
     * Create the new stored procedure meta-model.
     * @param database
     * @param dbProperty
     * @param parProcedure Configuration data from a XML file

     */
    @SuppressWarnings("unchecked")
    public MetaProcedure(MetaDatabase database, Key dbProperty, MetaProcedure parProcedure) {
        this();

        ID.setValue(this, dbProperty.getName());
        DATABASE.setValue(this, database);
        DB_PROPERTY.setValue(this, dbProperty);

        final Field field = UjoManager.getInstance().getPropertyField(MetaDatabase.ROOT.of(database), dbProperty);
        Procedure proc1 =  field!=null ? field.getAnnotation(Procedure.class) : null;
        Procedure proc2 = (Procedure) dbProperty.getType().getAnnotation(Procedure.class);

        if (parProcedure!=null) {
            changeDefault(this, NAME  , NAME.of(parProcedure));
            changeDefault(this, SCHEMA, SCHEMA.of(parProcedure));
        }
        if (proc1!=null) changeDefault(this, NAME  , proc1.name());
        if (proc1!=null) changeDefault(this, NAME  , proc1.value());
        if (proc1!=null) changeDefault(this, SCHEMA, proc1.schema());
        if (proc2!=null) changeDefault(this, NAME  , proc2.name());
        if (proc2!=null) changeDefault(this, NAME  , proc2.value());
        if (proc2!=null) changeDefault(this, SCHEMA, proc2.schema());

        changeDefault(this, SCHEMA, MetaDatabase.SCHEMA.of(database));
        changeDefault(this, NAME, dbProperty.getName());

        procedureName = Procedure.NULL.endsWith(SCHEMA.of(this))
            ? NAME.of(this)
            : SCHEMA.of(this) + "." + NAME.of(this)
            ;

        // -----------------------------------------------

        UjoManager.newInstance(dbProperty.getType());  // Initialize static Keys
        UjoManager ujoManager = UjoManager.getInstance();
        MetaTable table = new MetaTable(database, r2m, null);

        final Class<Ujo> ujoType = dbProperty.getType();
        for (Key p : ujoManager.readKeys(ujoType)) {

            MetaColumn c = new MetaColumn(table, p, null);
            PARAMETERS.addItem(this, c);
            c.initTypeCode();

            boolean returnProperty = p.getIndex()==0;
            Parameter par = returnProperty ? null : p.getClass().getAnnotation(Parameter.class);
            input [p.getIndex()] = par!=null ? par.input()  : !returnProperty ;
            output[p.getIndex()] = par!=null ? par.output() :  returnProperty ;

            // dbHandler.addColumnModel(c); // ??
        }
    }

    /** Returns a base table class. */
    @SuppressWarnings("unchecked")
    final public Class<OrmUjo> getType() {
        return DB_PROPERTY.of(this).getType();
    }

    /** Returns the database */
    final public MetaDatabase getDatabase() {
        return DATABASE.of(this);
    }

    /** Is it an INPUT key ? */
    public boolean isInput(final MetaColumn column) {
        boolean result = input[column.getKey().getIndex()];
        return result;
    }

    /** Is it an OUTPUT key ? */
    public boolean isOutput(final MetaColumn column) {
        int index = column.getKey().getIndex();
        final boolean result = index==0 || output[index];
        return result;
    }

    /** Procedure name */
    public String getProcedureName() {
        return procedureName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(10);

        sb.append(procedureName);
        sb.append("(");

        for (MetaColumn param : PARAMETERS.getList(this)) {
            if (!param.isVoid()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(param.getKey());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /** Compare object by the same instance. */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return this==obj;
    }

}
