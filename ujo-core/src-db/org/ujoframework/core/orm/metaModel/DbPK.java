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

package org.ujoframework.core.orm.metaModel;

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;

/**
 * The table primary key.
 * @author pavel
 */
public class DbPK extends AbstractMetaModel {

    /** DB columns */
    public static final UjoProperty<DbPK,DbTable> TABLE = newProperty("table", DbTable.class);

    /** DB table */
    public static final ListProperty<DbPK,DbColumn> COLUMNS = newPropertyList("columns", DbColumn.class);

    /** Compare two objects by PrimaryKey */
    @SuppressWarnings("unchecked")
    public boolean equals(Ujo ujo1, Ujo ujo2) {

        for (DbColumn column : COLUMNS.of(this)) {
            
            final UjoProperty property = DbColumn.PROPERTY.of(column);
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

            final UjoProperty p1 = DbColumn.PROPERTY.of(c1);
            final UjoProperty p2 = DbColumn.PROPERTY.of(c2);

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

}
