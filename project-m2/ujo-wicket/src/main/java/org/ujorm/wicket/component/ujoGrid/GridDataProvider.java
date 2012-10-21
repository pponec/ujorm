/*
 *  Copyright 2011-2012 Pavel Ponec
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

package org.ujorm.wicket.component.ujoGrid;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;

/**
 * Data Grid Provider
 * @author Pavel Ponec
 * @param <UJO> Base Domain Object
 */
public interface GridDataProvider<UJO extends Ujo> extends IDataProvider<UJO> {

    /** Returns a default Table Columns */
    public KeyRing<UJO> getTableColumns();
    
    /** Set order by  */
    public void setOrderBy(int column, boolean descending);

    /** Is the column sortable ? */
    public boolean isSortable(int column);

    /** Return a default sorted column */
    public Key<UJO,?> getDefultSortedColumn();

    /**
     * Returns a property index
     * @param Table column (nullable value)
     * @return value -1 is table column was not found.
     */
    public int getColumnIndex(Key<UJO,?> column);
   
}
