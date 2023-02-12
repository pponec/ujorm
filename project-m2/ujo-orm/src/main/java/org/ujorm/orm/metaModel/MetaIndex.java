/*
 *  Copyright 2020-2022 Pavel Ponec
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

import java.util.List;
import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.Transient;
import org.ujorm.orm.AbstractMetaModel;


/**
 * DB index metamodel.
 * @author Pavel Ponec
 */
@Unmodifiable
final public class MetaIndex extends AbstractMetaModel {
    private static final Class<MetaIndex> CLASS = MetaIndex.class;

    /** Property Factory */
    private static final KeyFactory<MetaIndex> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** Index database name */
    @Transient
    public static final Key<MetaIndex,String> NAME = fa.newKey("name");
    /** Table */
    @Transient
    public static final Key<MetaIndex,MetaTable> TABLE = fa.newKey("table");
    /** Is the index unique ? */
    @Transient
    public static final Key<MetaIndex,Boolean> UNIQUE = fa.newKey("unique", true);
    /** Index Columns */
    @Transient
    public static final ListKey<MetaIndex,MetaColumn> COLUMNS = fa.newListKey("column");

    /** The key initialization */
    static{fa.lock();}

    public MetaIndex(String index, MetaTable table) {
        NAME.setValue(this, index);
        TABLE.setValue(this, table);
    }

    /** Show an index name + table */
    @Override
    public String toString() {
        final String result = NAME.of(this)
            + " ["
            + COLUMNS.getItemCount(this)
            + "] of the table: "
            + get(TABLE).get(MetaTable.NAME)
            ;
        return result;
    }

    /** Index Columns */
    public List<MetaColumn> getColumns() {
        return COLUMNS.getList(this);
    }

}
