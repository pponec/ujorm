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

import org.ujoframework.UjoProperty;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.DbProcedure;
import org.ujoframework.orm.annot.Parameter;
import org.ujoframework.orm.annot.Procedure;

/**
 * The callable procedure meta-model
 * @author Pavel Ponec
 */
final public class MetaProcedure extends AbstractMetaModel {

    private static final Class CLASS = MetaProcedure.class;
    /** Dummy relation. */
    @Transient @SuppressWarnings("unchecked")
    private static final RelationToMany r2m = new RelationToMany("[PROCEDURE]", DbProcedure.class);
    /** DB columns */
    public static final ListProperty<MetaProcedure, MetaColumn> COLUMNS = newListProperty("columns", MetaColumn.class);

    /** The property initialization */
    static {
        init(CLASS);
    }
    
    /** Database */
    final private MetaDatabase database;
    final private String procedureName;
    final boolean[] input;
    final boolean[] output;

    public MetaProcedure(DbProcedure procedure, MetaDatabase database) {
        this.database = database;
        input = new boolean[procedure.readProperties().size()];
        output = new boolean[input.length];

        MetaTable table = new MetaTable(database, r2m, null);

        Procedure pe = procedure.getClass().getAnnotation(Procedure.class);
        procedureName = Procedure.NULL.endsWith(pe.schema())
            ? pe.name()
            : pe.schema() + "." + pe.name()
            ;

        for (UjoProperty p : procedure.readProperties()) {
            MetaColumn c = new MetaColumn(table, p, null);
            COLUMNS.addItem(this, c);
            c.initTypeCode(database.getParams());

            boolean returnProperty = p.getIndex()==0;
            Parameter par = returnProperty ? null : p.getClass().getAnnotation(Parameter.class);
            input [p.getIndex()] = par!=null ? par.input()  : !returnProperty ;
            output[p.getIndex()] = par!=null ? par.output() :  returnProperty ;
        }
        super.setReadOnly(false);
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

    /** Return the database */
    public MetaDatabase getDatabase() {
        return database;
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

        for (MetaColumn column : COLUMNS.getList(this)) {
            if (!column.isVoid()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(column.getProperty());
            }
        }
        sb.append(")");
        return sb.toString();
    }

}
