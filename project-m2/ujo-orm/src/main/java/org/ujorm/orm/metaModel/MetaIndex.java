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

import org.ujorm.ListUjoProperty;
import org.ujorm.UjoProperty;
import org.ujorm.core.PropertyFactory;
import org.ujorm.core.annot.Immutable;
import org.ujorm.core.annot.Transient;
import org.ujorm.orm.AbstractMetaModel;


/**
 * DB index metamodel.
 * @author Pavel Ponec
 */
@Immutable
final public class MetaIndex extends AbstractMetaModel {
    private static final Class CLASS = MetaIndex.class;

    /** Property Factory */
    private static final PropertyFactory<MetaIndex> fa = PropertyFactory.CamelBuilder.get(CLASS);
    /** Index database name */
    @Transient
    public static final UjoProperty<MetaIndex,String> NAME = fa.newProperty("name");
    /** Table */
    @Transient
    public static final UjoProperty<MetaIndex,MetaTable> TABLE = fa.newProperty("table");
    /** Is the index unique ? */
    @Transient
    public static final UjoProperty<MetaIndex,Boolean> UNIQUE = fa.newProperty("unique", true);
    /** Table Columns */
    @Transient
    public static final ListUjoProperty<MetaIndex,MetaColumn> COLUMNS = fa.newListProperty("column");

    /** The property initialization */
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




}
