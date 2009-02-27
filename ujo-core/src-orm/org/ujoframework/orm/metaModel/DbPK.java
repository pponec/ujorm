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

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.annot.GenerationType;

/**
 * The table primary key.
 * @author pavel
 */
public class DbPK extends AbstractMetaModel {

    /** DB columns */
    public static final UjoProperty<DbPK,DbTable> TABLE = newProperty("table", DbTable.class);

    /** DB table */
    public static final ListProperty<DbPK,DbColumn> COLUMNS = newPropertyList("columns", DbColumn.class);

    private long primaryKeyCounter = 0;


    /** Compare two objects by PrimaryKey */
    @SuppressWarnings("unchecked")
    public boolean equals(Ujo ujo1, Ujo ujo2) {

        for (DbColumn column : COLUMNS.of(this)) {
            
            final UjoProperty property = DbColumn.TABLE_PROPERTY.of(column);
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
        DbPK other = (DbPK) obj;

        List<DbColumn> columns1 = COLUMNS.getList(this);
        List<DbColumn> columns2 = COLUMNS.getList(other);

        if (columns1.size()!=columns2.size()) {
            return false;
        }

        for (int i=columns1.size()-1; i>=0; i--) {
            final DbColumn c1 = columns1.get(i);
            final DbColumn c2 = columns2.get(i);

            final UjoProperty p1 = DbColumn.TABLE_PROPERTY.of(c1);
            final UjoProperty p2 = DbColumn.TABLE_PROPERTY.of(c2);

            if (p1!=p2) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(10);

        for (DbColumn column : COLUMNS.getList(this)) {
            if (sb.length()>0) { sb.append(','); }
            sb.append(column.toString());
        }
        return sb.toString();
    }


    /** Returns a next primary key. The minimal value is 1. */
    public synchronized long nextPrimaryKey() {
        return ++primaryKeyCounter;
    }

    /** Assign a PK from framework in case the PK generator is type of MEMO_SEQUENCE. */
    @SuppressWarnings("unchecked")
    public boolean assignPrimaryKey(TableUjo table) {
        int count = COLUMNS.getItemCount(this);
        if (count==1) {
            DbColumn column = COLUMNS.getItem(this, 0);

            switch (DbColumn.PRIMARY_KEY_GEN.of(column)) {
                case MEMO_SEQUENCE:
                    UjoProperty property = DbColumn.TABLE_PROPERTY.of(column);
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

}
