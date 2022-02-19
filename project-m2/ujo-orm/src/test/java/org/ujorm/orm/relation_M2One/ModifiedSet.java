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
package org.ujorm.orm.relation_M2One;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.ColumnSet;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table order (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Table(name = "ord_order")
public class ModifiedSet extends OrmTable<ModifiedSet> implements ColumnSet {

    /** Date of creation */
    public static final Key<ModifiedSet, Date> created = newKey();
    /** Date of creation */
    public static final Key<ModifiedSet, Date> modified = newKey();

    /** Date of creation */
    public Date getCreated() {
        return created.of(this);
    }

    /** Date of creation */
    public void setCreated(Date created) {
        ModifiedSet.created.setValue(this, created);
    }

    /** Date of creation */
    public Date getModified() {
        return modified.of(this);
    }

    /** Date of creation */
    public void setModified(Date modified) {
        ModifiedSet.modified.setValue(this, modified);
    }

}
