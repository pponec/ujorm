/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;

/**
 *
 * @author pavel
 */
public class DbHandler {

     private static DbHandler handler = new DbHandler();

    /** Load Iterator from DB */
    public UjoIterator loadIterator(Ujo ujo, UjoRelative property) {
        // todo
        return null;
    }

    /** Get Session */
    public Session getSession() {
        // TODO
        return new Session();
    }

    public static DbHandler getInstance() {
        return handler;
    }

    boolean isPersistent(UjoProperty property) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
