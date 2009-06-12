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
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.orm.OrmUjo;

/**
 * The table primary key.
 * @author Pavel Ponec
 * @composed 1 - * MetaColumn
 */
public class MetaPKey extends AbstractMetaModel {

    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    /** DB table */
    public static final UjoProperty<MetaPKey,MetaTable> TABLE = newProperty("table", MetaTable.class, propertyCount++);

    /** DB columns */
    public static final ListProperty<MetaPKey,MetaColumn> COLUMNS = newPropertyList("columns", MetaColumn.class, propertyCount++);

    /** Database */
    final private MetaDatabase database;

    public MetaPKey(MetaTable table) {
        this.database = MetaTable.DATABASE.of(table);
        TABLE.setValue(this, table);
        COLUMNS.setValue(this, new ArrayList<MetaColumn>(0));
    }

    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(10);

        for (MetaColumn column : COLUMNS.getList(this)) {
            if (sb.length()>0) { sb.append(','); }
            sb.append(column.toString());
        }
        return sb.toString();
    }

    /** Assign a PK from framework in case the PK generator is type of MEMO_SEQUENCE. */
    @SuppressWarnings("unchecked")
    public boolean assignPrimaryKey(OrmUjo bo) {
        int count = COLUMNS.getItemCount(this);
        if (count==1) {

            MetaColumn column = COLUMNS.getItem(this, 0);
            UjoProperty property = column.getProperty();
            if (property.of(bo)!=null) {
                return false;
            }

            final long value = TABLE.of(this).getSequencer().nextValue();
            if (Long.class==property.getType()) {
                property.setValue(bo, value);
                return true;
            }
            if (Integer.class==property.getType()) {
                property.setValue(bo, (int) value);
                return true;
            }

            return false;
        } else {
            String msg = "Table " + bo + " must have defined only one primary key type of Long or Integer";
            throw new IllegalArgumentException(msg);
        }
    }

    /** Returns the first column. */
    public MetaColumn getFirstColumn() {
        return getColumn(0);
    }

    /** Returns column on the selected position. */
    public MetaColumn getColumn(int i) {
        return COLUMNS.of(this).get(i);
    }
}
