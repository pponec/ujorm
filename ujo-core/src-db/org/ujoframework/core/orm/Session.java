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

package org.ujoframework.core.orm;

import org.ujoframework.implementation.db.*;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.core.orm.sample.BoDatabase;
import org.ujoframework.tools.criteria.Expression;

/**
 *
 * @author pavel
 */
public class Session {

    public void commit() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> Query<UJO> createQuery(Class<UJO> aClass, Expression<UJO> expA) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public BoDatabase getDatabase() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void rollback() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void save(TableUjo ujo) {
        throw new UnsupportedOperationException("SAVE is not yet implemented");
    }

    public <UJO extends TableUjo> UJO Load(Class ujo, Object id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public <UJO extends TableUjo> UJO single(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> UjoIterator<UJO> iterate(Query<UJO> query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> UjoIterator<UJO> iterate(UjoProperty property) {
        throw new UnsupportedOperationException("Not yet implemented: " + property);
    }


}
