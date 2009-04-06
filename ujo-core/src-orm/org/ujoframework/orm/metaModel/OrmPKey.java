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

    public OrmPKey() {
        COLUMNS.setValue(this, new ArrayList<OrmColumn>(0));
    }

    /** Compare two objects by PrimaryKey */
    @SuppressWarnings("unchecked")
    public boolean equals(Ujo ujo1, Ujo ujo2) {

        for (OrmColumn column : COLUMNS.of(this)) {
            
            final UjoProperty property = column.getProperty();
            final Object o2  = property.of(ujo2);
            final boolean ok = property.equals(ujo1, o2);
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    /** Compares two primary keys. */
    @Override
    public boolean equals(Object obj) {
        OrmPKey other = (OrmPKey) obj;

        List<OrmColumn> columns1 = COLUMNS.getList(this);
        List<OrmColumn> columns2 = COLUMNS.getList(other);

        if (columns1.size()!=columns2.size()) {
            return false;
        }

        for (int i=columns1.size()-1; i>=0; i--) {
            final OrmColumn c1 = columns1.get(i);
            final OrmColumn c2 = columns2.get(i);

            final UjoProperty p1 = c1.getProperty();
            final UjoProperty p2 = c2.getProperty();

            if (p1!=p2) {
                return false;
            }
        }
        return true;
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
            switch (OrmColumn.PRIMARY_KEY_GEN.of(column)) {
                case MEMO_SEQUENCE:
                    UjoProperty property = column.getProperty();
                    if (property.of(table)!=null) {
                        return false;
                    }
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
        return COLUMNS.of(this).get(0);
    }

}
