/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm;

import org.ujoframework.orm.sample.BoDatabase;

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
            DbHandler.getInstance().createDatabase(BoDatabase.class);
            session = DbHandler.getInstance().getSession();
        }

        return session;

    }

}
