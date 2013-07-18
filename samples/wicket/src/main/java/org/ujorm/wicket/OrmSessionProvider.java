/*
 * Copyright 2013 Pavel Ponec
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
package org.ujorm.wicket;

import org.apache.wicket.protocol.http.WebApplication;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.Session;

/**
 * ORM Session Provider
 * @author Pavel Ponec
 */
public class OrmSessionProvider {

    /** Orm Session */
    private Session session;

    /** Create current ORM Handler */
    public static OrmHandler getOrmHandler() throws IllegalStateException {
        final WebApplication application = WebApplication.get();
        if (application instanceof OrmHandlerProvider) {
            return ((OrmHandlerProvider) application).getOrmHandler();
        } else {
            throw new IllegalStateException("The WebApplication must to implement "
                    + OrmHandlerProvider.class);
        }
    }

    /** Create and cache the ORM Session, there is necessary to close the created session */
    public Session getSession() throws IllegalStateException {
        if (session == null) {
            session = getOrmHandler().createSession();
        }
        return session;
    }

    /** Close the session if any */
    public boolean closeSession() {
        final boolean result = session != null;
        if (result) {
            session.close();
            session = null;
        }
        return result;
    }

    /** Is the session open? */
    public boolean isOpenSesson() {
        return session != null;
    }

}
