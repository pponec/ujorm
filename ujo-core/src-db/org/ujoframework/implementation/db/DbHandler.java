/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db;

import org.ujoframework.Ujo;
import org.ujoframework.core.UjoIterator;

/**
 *
 * @author pavel
 */
public class DbHandler {

     private static DbHandler handler = new DbHandler();

    /** Load Iterator from DB */
    public UjoIterator loadIterator(Ujo ujo, UjoRelation property) {
        // todo
        return null;
    }

    /** Get DbConnection */
    public DbConnection getConnection() {
        // TODO
        return new DbConnection();
    }

    public static DbHandler getInstance() {
        return handler;
    }



}
