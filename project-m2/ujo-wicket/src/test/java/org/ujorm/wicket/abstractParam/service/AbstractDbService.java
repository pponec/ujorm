/*
 * Copyright 2013, Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.wicket.abstractParam.service;

import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.spring.UjormTransactionManager;

/**
 * Abstract service with basic database tools
 * @author ponec
 */
abstract public class AbstractDbService {

    /** Orm Configuration */
    // @Autowired // TODO
    private UjormTransactionManager manager;

    /** Return local session */
    protected final Session getSession() {
        return manager.getLocalSession();
    }

    /** Create database query with Session */
    protected final <T extends OrmUjo> Query<T> createQuery(Criterion<T> criterion) {
        return getSession().createQuery(criterion);
    }

}
