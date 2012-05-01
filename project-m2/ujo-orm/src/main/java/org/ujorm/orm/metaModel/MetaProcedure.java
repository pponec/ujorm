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

import java.lang.reflect.Field;
import org.ujorm.UjoProperty;
import org.ujorm.core.UjoManager;
import org.ujorm.core.annot.Immutable;
import org.ujorm.core.annot.Transient;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.annot.Table;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.DbProcedure;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.annot.Parameter;
import org.ujorm.orm.annot.Procedure;


/**
 * DB procudure or function meta-model.
 * @author Pavel Ponec
 * @composed 1 - * MetaColumn
 */
@Immutable
final public class MetaProcedure extends AbstractMetaModel {
    private static final Class CLASS = MetaProcedure.class;


    /** The meta-model id */
    @XmlAttribute
    public static final Property<MetaProcedure,String> ID = newProperty("id", Table.NULL);
    /** Procedure name */
    public static final Property<MetaProcedure,String> NAME = newProperty("name", Table.NULL);
    /** Name of table schema. */
    public static final Property<MetaProcedure,String> SCHEMA = newProperty("schema", Table.NULL);
    /** Procedure parameters */
    public static final ListProperty<MetaProcedure,MetaColumn> PARAMETERS = newListProperty("parameter", MetaColumn.class);
    /** Procedure <strong>property</strong> (a base definition of the procedure) */
    @Transient
    public static final Property<MetaProcedure,UjoProperty> DB_PROPERTY = newProperty("dbProperty", UjoProperty.class);
    /** Database */
    @Transient
    public static final Property<MetaProcedure,MetaDatabase> DATABASE = newProperty("database", MetaDatabase.class);
    /** Dummy relation for internal use only. */
    @Transient
    @SuppressWarnings("unchecked")
    private static final RelationToMany r2m = new RelationToMany("[PROCEDURE]", DbProcedure.class);

    /** The property initialization */
    static{init(CLASS);}

    /** Full procedure name */
    private String procedureName;
    /** Input signs */
    final boolean[] input;
    /** Output signs */
    final boolean[] output;


    /** No parameter constructor. */
    public MetaProcedure() {
        input = new boolean[readProperties().size()];
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
    public MetaProcedure(MetaDatabase database, UjoProperty dbProperty, MetaProcedure parProcedure) {
        this();

        ID.setValue(this, dbProperty.getName());
        DATABASE.setValue(this, database);
        DB_PROPERTY.setValue(this, dbProperty);

        final Field field = UjoManager.getInstance().getPropertyField(MetaDatabase.ROOT.of(database), dbProperty);
        Procedure proc1 = field.getAnnotation(Procedure.class);
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

        UjoManager ujoManager = UjoManager.getInstance();
        MetaTable table = new MetaTable(database, r2m, null);

        for (UjoProperty p : ujoManager.readProperties(dbProperty.getType())) {

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

    /** Is it an INPUT property ? */
    public boolean isInput(final MetaColumn column) {
        boolean result = input[column.getProperty().getIndex()];
        return result;
    }

    /** Is it an OUTPUT property ? */
    public boolean isOutput(final MetaColumn column) {
        int index = column.getProperty().getIndex();
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
                sb.append(param.getProperty());
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
