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

package org.ujoframework.orm;

import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.quick.QuickUjoMid;
import org.ujoframework.orm.metaModel.MetaProcedure;

/**
 * Abstract database procedure.
 * @author Ponec
 */
abstract public class DbProcedure<UJO extends DbProcedure> extends QuickUjoMid<UJO> {
    
    protected MetaProcedure metaProcedure = null;

    /** Clear all parameters */
    @SuppressWarnings("unchecked")
    public DbProcedure clear() {
        for (UjoProperty p : readProperties()) {
            p.setValue(this, null);
        }
        return this;
    }

    /** Call the procedure and return a value of the first Property */
    @SuppressWarnings("unchecked")
    public <T> T call(final Session session, final UjoProperty<UJO,T> result) {
        if (metaProcedure==null) {
            metaProcedure = new MetaProcedure(this, session.getHandler().getDatabases().get(0));
        }
        session.call(this);
        return result.of((UJO)this);
    }

    /** Call the procedure and return a value of the first Property */
    @SuppressWarnings("unchecked")
    public <T> T call(final Session session) {
        return (T) call(session, readProperties().get(0));
    }

    /** Returns MetaModel of the procedure */
    public MetaProcedure metaProcedure() {
        return metaProcedure;
    }

}
