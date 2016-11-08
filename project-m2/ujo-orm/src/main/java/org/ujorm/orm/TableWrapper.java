/*
 *  Copyright 2012 Pavel Ponec
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

import java.util.List;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * Wrapper for a MetaTable
 * @author Pavel Ponec
 */
public interface TableWrapper {

    /** Returns a meta-table model */
    public MetaTable getModel();

    /** Returns a unique table name in the one SQL statement. */
    public String getAlias();

    /** Is the instance a database relation model? */
    public boolean isView();

    /** Get all table columns */
    public List<? extends ColumnWrapper> getColumns();
}
