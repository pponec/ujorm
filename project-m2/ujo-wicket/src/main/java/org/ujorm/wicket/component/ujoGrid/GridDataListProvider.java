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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;
import org.ujorm.core.UjoComparator;

/**
 * An implementatation GridDataProvider for a data source {@code List}.
 * @author Pavel Ponec
 */
public class GridDataListProvider<UJO extends Ujo> implements GridDataProvider<UJO> {

    private List<UJO> rows;
    private KeyRing<UJO> tableColumns;
    int orderBy = -1;
    boolean descending = false;

    public GridDataListProvider(Class<UJO> classObject, List<UJO> boList, Key... tableColumns) {
        this(boList, KeyRing.of(classObject, tableColumns));
    }

    public GridDataListProvider(List<UJO> rows, KeyRing tableColumns) {
        this.rows = rows;
        this.tableColumns = tableColumns;
    }

    @Override
    public KeyRing<UJO> getTableColumns() {
        return tableColumns;
    }

    @Override
    public void setOrderBy(int columnNumber, boolean descending) {
        orderBy = columnNumber;
        Key p = tableColumns.get(columnNumber).descending(descending);
        UjoComparator.newInstance(p).sort(rows);
    }

    /** Is th column sorttable */
    @Override
    public boolean isSortable(int column) {
        return tableColumns.get(column).isTypeOf(Comparable.class);
    }

    /** Returns a defalt sorted column, else {@code null} */
    @Override
    public Key<UJO,?> getDefultSortedColumn() {
        for (int i = 0; i < tableColumns.size(); i++) {
            if (isSortable(i)) {
                return tableColumns.get(i);
            }
        }
        return null;
    }

    /**
     * Returns a property index
     * @param Table column Nullable value
     * @return value -1 is table column was not found.
     */
    @Override
    public int getColumnIndex(Key<UJO,?> column) {
        for (int i = 0, max = tableColumns.size(); i < max; i++) {
            if (tableColumns.get(i).equals(column)) {
                 return i;
            }
        }
        return -1;
    }

    @Override
    public Iterator<? extends UJO> iterator(int first, int count) {
        final Comparator<UJO> comparator = createComparator(orderBy);
        if (comparator != null) {
            Collections.sort(rows, comparator);
        }
        final List<UJO> pageUsers = rows.subList(first, first + count);
        return pageUsers.iterator();
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public IModel<UJO> model(UJO object) {
        return new Model((Serializable) object);
    }

    @Override
    public void detach() {
    }

    /** Create Comparator, default returns null. */
    protected Comparator<UJO> createComparator(int orderBy) {
        return null;
    }
}
