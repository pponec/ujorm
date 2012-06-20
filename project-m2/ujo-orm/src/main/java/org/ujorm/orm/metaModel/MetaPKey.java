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

import java.math.BigInteger;
import java.util.ArrayList;
import org.ujorm.UjoProperty;
import org.ujorm.core.annot.Immutable;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;
import org.ujorm.orm.TypeService;

/**
 * The table primary key.
 * @author Pavel Ponec
 * @composed 1 - * MetaColumn
 */
@Immutable
final public class MetaPKey extends AbstractMetaModel {
    private static final Class CLASS = MetaPKey.class;

    /** DB table */
    public static final Property<MetaPKey,MetaTable> TABLE = newProperty("table");

    /** DB columns */
    public static final ListProperty<MetaPKey,MetaColumn> COLUMNS = newListProperty("columns");

    /** The property initialization */
    static{init(CLASS);}

    public MetaPKey(MetaTable table) {
        TABLE.setValue(this, table);
        COLUMNS.setValue(this, new ArrayList<MetaColumn>(0));
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

    /** Assign a PK from framework in case the PK generator is type of MEMO_SEQUENCE.
     * @return Value 'true' means, that primary key was assigned successfully
     *         and 'false' means that a primary key was defined earlier.
     * @throws java.lang.IllegalArgumentException The PK can't be assigned.
     */
    @SuppressWarnings("unchecked")
    public boolean assignPrimaryKey(final OrmUjo bo, final Session session) throws IllegalArgumentException {
        int count = COLUMNS.getItemCount(this);
        if (count==1) {

            MetaColumn column = COLUMNS.getItem(this, 0);
            UjoProperty property = column.getProperty();
            if (property.of(bo)!=null) {
                return false;
            }

            final long value = TABLE.of(this).getSequencer().nextValue(session);

            switch (column.getTypeCode()) {
                case TypeService.LONG    : bo.writeValue(property,         value); return true;
                case TypeService.INT     : bo.writeValue(property, (int  ) value); return true;
                case TypeService.SHORT   : bo.writeValue(property, (short) value); return true;
                case TypeService.BYTE    : bo.writeValue(property, (byte ) value); return true;
                case TypeService.BIG_INTE: bo.writeValue(property, BigInteger.valueOf(value)); return true;
                case TypeService.STRING  : bo.writeValue(property, String.valueOf(value)); return true;
                default: return false;
            }
        } else {
            for (int i = 0; i < count; i++) {
                final MetaColumn column = COLUMNS.getItem(this, i);
                final UjoProperty property = column.getProperty();
                if (property.of(bo) == null) {
                    String msg = "Table " + bo + " must have defined only one primary key type of Long, Integer, Short, Byte, BigInteger or String for an auto-increment support";
                    throw new IllegalArgumentException(msg);
                }
            }
            return false;
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

    /** Returns a count of the Primary Keys */
    public int getCount() {
        return COLUMNS.getItemCount(this);
    }
}
