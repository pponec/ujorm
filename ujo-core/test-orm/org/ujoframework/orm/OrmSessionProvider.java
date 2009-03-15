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

package org.ujoframework.orm;

import org.ujoframework.orm.sample.Database;

/**
 * Singleton
 * @author pavel
 */
public class OrmSessionProvider {

    public static final OrmSessionProvider ormSessionProvider = new OrmSessionProvider();

    private Session session = null;


    private OrmSessionProvider() {
    }


    /** Returns an session or create the one if missing */
    public Session getSession() {

        if (session==null) {
            OrmHandler.getInstance().createDatabase(Database.class);
            session = OrmHandler.getInstance().getSession();
        }

        return session;

    }

}
