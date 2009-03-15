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

import java.io.IOException;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmTable;

/**
 * SQL renderer API
 * @author Ponec
 */
public interface SqlRenderer {

    /** Returns a default JDBC Driver */
    public String getJdbcUrl();

    /** Returns a JDBC Driver */
    public String getJdbcDriver();

    /** Print a SQL script to crate database */
    public void printCreateDatabase(OrmDatabase database, Appendable writer) throws IOException ;

    /** Print SQL SELECT */
    public void printSelect(Query query, StringBuilder result)  throws IOException;

    /** Print a SQL sript to create table */
    public void printTable(OrmTable table, Appendable result) throws IOException ;

    /** Print a SQL to create column */
    public void printColumnDeclaration(OrmColumn column, Appendable writer, String name) throws IOException;

    /** Print a SQL to create a Foreign Key. */
    public void printFKColumnsDeclaration(OrmColumn column, Appendable writer) throws IOException;

    /** Print an INSERT SQL statement.  */
    public void printInsert(TableUjo ujo, Appendable writer) throws IOException;

}
