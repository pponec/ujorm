/*
 *  Copyright 2009-2010 Pavel Ponec
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


package org.ujoframework.orm.ao;

/**
 * Values list to controll how the DLL (Data Definition Language) statmenets will be used
 * to a defining data structure modification.
 * The default value is {@link #CREATE_OR_UPDATE_DDL CREATE_OR_UPDATE_DDL}.
 * @author Pavel Ponec
 * @see org.ujoframework.orm.metaModel.MetaParams
 * @see #CREATE_OR_UPDATE_DDL Default value
 */
public enum Orm2ddlPolicy {

    /** Framework is expected to match the DDL structure with the ORM model and do not make any validation. */
    DO_NOTHING,
    /** Create full DDL structure in condition that the the database structure was not found. */
    CREATE_DDL,
    /** Create or update full DDL structure. It is the DEFAULT value. */
    CREATE_OR_UPDATE_DDL,
    /** Throw the IllegalStateException in case missing a table, index, or column in the connected database. */
    VALIDATE,
    /** The value is defined from a parent. 
     * The hiearchy from the parrent to a child is:
     * <ul>
     *   <li>Meta Parameters</li>
     *   <li>Database</li>
     *   <li>Table</li>
     * </ul>
     * In case the parent ROOT is undefined, then the {@see #CREATE_OR_UPDATE_DDL} will be used.
     * @see #CREATE_OR_UPDATE_DDL
     */
    INHERITED,
    ;

}
