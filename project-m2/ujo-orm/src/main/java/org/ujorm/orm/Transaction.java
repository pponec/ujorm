/*
 *  Copyright 2012 pavel.
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

import java.sql.Savepoint;
import java.util.LinkedList;

/**
 * Transaction manager.
 * @author Pavel Ponec
 */
public class Transaction {

    final private Session session;

    /** Store of the savepoints */
    private LinkedList<Savepoint[]> savepoints ;

    /* DEFAULT*/ Transaction(Session session) {
        this.session = session;
    }

    /** Commit the current level of the beginTransaction */
    public void commit() {
    if (savepoints.size()==0) {
            throw new IllegalArgumentException("No transaction to commit");
        }
        session.commit(true, savepoints.removeLast());
    }

    /** Rollback the current level of the beginTransaction */
    public void rollback() {
        if (savepoints.size()==0) {
            throw new IllegalArgumentException("No transaction to rollback");
        }
        session.commit(false, savepoints.removeLast());
    }

    /** Create a nested transaction */
    public void nestedTransaction() {
        if (savepoints==null) {
            savepoints = new LinkedList<Savepoint[]>();
            savepoints.add(null);
        } else {
            savepoints.add(session.setSavepoint());
        }
    }

    /** Returns a (sub)transaction level */
    public int getTransactionLevel() {
        return savepoints!=null ? savepoints.size() : 0 ;
    }


    /** Get the current Session */
    public Session getSession() {
        return session;
    }

}
