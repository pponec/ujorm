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

import java.util.ArrayList;
import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.TableUjo;

/**
 * The table primary key.
 * @author Pavel Ponec
 * @composed 1 - * OrmColumn
 */
public class OrmPKey extends AbstractMetaModel {

    /** DB columns */
    public static final UjoProperty<OrmPKey,OrmTable> TABLE = newProperty("table", OrmTable.class);

    /** DB table */
    public static final ListProperty<OrmPKey,OrmColumn> COLUMNS = newPropertyList("columns", OrmColumn.class);

    /** Primary key counter. */
    private long primaryKeyCounter = 0;
    final private OrmDatabase database;

    public OrmPKey(OrmTable table) {
        this.database = OrmTable.DATABASE.of(table);
        COLUMNS.setValue(this, new ArrayList<OrmColumn>(0));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(10);

        for (OrmColumn column : COLUMNS.getList(this)) {
            if (sb.length()>0) { sb.append(','); }
            sb.append(column.toString());
        }
        return sb.toString();
    }


    /** Returns a next primary key. The minimal value is 1. */
    protected synchronized long nextPrimaryKey() {
        return ++primaryKeyCounter;
    }

    /** Assign a PK from framework in case the PK generator is type of MEMO_SEQUENCE. */
    @SuppressWarnings("unchecked")
    public boolean assignPrimaryKey(TableUjo table) {
        int count = COLUMNS.getItemCount(this);
        if (count==1) {

            OrmColumn column = COLUMNS.getItem(this, 0);
            UjoProperty property = column.getProperty();
            if (property.of(table)!=null) {
                return false;
            }

            switch (OrmColumn.PRIMARY_KEY_GEN.of(column)) {
                case DB_SEQUENCE:

                    final long value = database.getSequencer().nextValue(table.readSession());
                    if (Long.class==property.getType()) {
                        property.setValue(table, value);
                        return true;
                    }
                    if (Integer.class==property.getType()) {
                        property.setValue(table, (int) value);
                        return true;
                    }
                case MEMO_SEQUENCE:
                    if (Long.class==property.getType()) {
                        property.setValue(table, nextPrimaryKey());
                        return true;
                    }
                    if (Integer.class==property.getType()) {
                        property.setValue(table, (int) nextPrimaryKey());
                        return true;
                    }
               default:
                   return false;
            }
        }
        throw new IllegalArgumentException("Table " + table + " must have defined only one primary key type of Long or Integer");
    }

    /** Returns the first column. */
    public OrmColumn getFirstColumn() {
        return getColumn(0);
    }

    /** Returns column on the selected position. */
    public OrmColumn getColumn(int i) {
        return COLUMNS.of(this).get(i);
    }
}
