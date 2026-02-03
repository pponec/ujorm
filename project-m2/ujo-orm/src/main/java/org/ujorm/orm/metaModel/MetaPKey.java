/*
 *  Copyright 2020-2026 Pavel Ponec
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
import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.KeyFactory;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;
import org.ujorm.orm.TypeService;
import org.ujorm.tools.Assert;

/**
 * The table primary key.
 * @author Pavel Ponec
 * @composed 1 - * MetaColumn
 */
@Unmodifiable
final public class MetaPKey extends AbstractMetaModel {
    private static final Class<MetaPKey> CLASS = MetaPKey.class;

    /** Property Factory */
    private static final KeyFactory<MetaPKey> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** DB table */
    public static final Key<MetaPKey,MetaTable> TABLE = fa.newKey("table");
    /** DB columns */
    public static final ListKey<MetaPKey,MetaColumn> COLUMNS = fa.newListKey("columns");

    /** The key initialization */
    static{fa.lock();}

    public MetaPKey(MetaTable table) {
        TABLE.setValue(this, table);
        COLUMNS.setValue(this, new ArrayList<>(0));
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
    public boolean assignPrimaryKey(final OrmUjo bo, final Session session) throws IllegalUjormException {
        int count = COLUMNS.getItemCount(this);
        if (count==1) {

            MetaColumn column = COLUMNS.getItem(this, 0);
            Key key = column.getKey();
            if (key.of(bo) != null) {
                return false;
            }

            switch (column.getTypeCode()) {
                case TypeService.LONG    : bo.writeValue(key, nextValue(session)); return true;
                case TypeService.INT     : bo.writeValue(key, (int ) nextValue(session)); return true;
                case TypeService.SHORT   : bo.writeValue(key, (short) nextValue(session)); return true;
                case TypeService.BYTE    : bo.writeValue(key, (byte) nextValue(session)); return true;
                case TypeService.BIG_INTE: bo.writeValue(key, BigInteger.valueOf(nextValue(session))); return true;
                case TypeService.STRING  : bo.writeValue(key, String.valueOf(nextValue(session))); return true;
                case TypeService.UUID    : bo.writeValue(key, java.util.UUID.randomUUID()); return true;
                default: return false;
            }
        } else {
            for (int i = 0; i < count; i++) {
                final MetaColumn column = COLUMNS.getItem(this, i);
                final Key key = column.getKey();
                Assert.notNull(key.of(bo)
                        , "Table {} must have defined only one primary key type of Long, Integer, Short, Byte, BigInteger or String for an auto-increment support"
                        , bo);
            }
            return false;
        }
    }

    /** Generate a next value */
    private long nextValue(final Session session) {
        return TABLE.of(this).getSequencer().nextValue(session);
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
