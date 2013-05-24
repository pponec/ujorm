/*
 *  Copyright 2013-2013 Pavel Ponec
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

package org.ujorm.orm;

/**
 * An inicializaton batch implementation can be called after building the ORM meta-model.
 */
public interface InitializationBatch {

    /**
     * Inicializaton batch implementation can be called after building a meta-model.
     * @param session A session ready to use, the session is commited in the parent method.
     * @throws Exception Any exception causes the call session.rollback() method.
     */
    public void run(Session session) throws Exception;


}
