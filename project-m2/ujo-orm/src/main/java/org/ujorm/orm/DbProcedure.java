/*
 *  Copyright 2010 Ponec.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ujorm.orm;

import org.ujorm.Key;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.implementation.quick.SmartUjo;
import org.ujorm.orm.metaModel.MetaProcedure;

/**
 * Abstract database procedure.
 * @author Ponec
 */
abstract public class DbProcedure<UJO extends DbProcedure> extends SmartUjo<UJO> {

    /** Meta - model */
    transient protected MetaProcedure metaProcedure = null;

    /** Clear all parameters */
    @SuppressWarnings("unchecked")
    public DbProcedure clear() {
        for (Key p : readKeys()) {
            p.setValue(this, null);
        }
        return this;
    }

    /** Call the procedure and return a type-safe value of the required Property */
    @SuppressWarnings("unchecked")
    public <T> T call(final Session session, final Key<UJO,T> result) {
        if (metaProcedure==null) {
            metaProcedure = session.getHandler().findProcedureModel(getClass());
        }
        session.call(this);
        return result.of((UJO)this);
    }

    /** Call the procedure and return a value of the first Property. <br>
     * WARNING: The result is NOT type-save value, use rather {@link #call(org.ujorm.orm.Session, org.ujorm.Key)}.
     * @see #call(org.ujorm.orm.Session, org.ujorm.Key)
     */
    @SuppressWarnings("unchecked")
    public <T> T call(final Session session) {
        return call(session, readKeyList().get(0));
    }

    /** Returns MetaModel of the procedure */
    public MetaProcedure metaProcedure() {
        return metaProcedure;
    }

    /** A PropertyIterator Factory creates an new key and assign a next index.
     * @hidden
     */
    protected static <UJO extends OrmTable, ITEM extends OrmTable> RelationToMany<UJO,ITEM> newRelation(String name, Class<ITEM> type) {
        return new RelationToMany<> (name, type, -1, false);
    }

    /** A PropertyIterator Factory creates an new key and assign a next index.
     * @hidden
     */
    protected static <UJO extends OrmTable, ITEM extends OrmTable> RelationToMany<UJO,ITEM> newRelation(Class<ITEM> type) {
        return newRelation(null, type);
    }


}
