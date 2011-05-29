/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Total Count
 * @author Pavel Ponec
 */
final public class ListExt<T> {

    private int totalCount;
    private List<T> list = new ArrayList<T>();

    public int getTotalCount() {
        return totalCount;
    }

    protected void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> list() {
        return list;
    }

    public boolean add(T item) {
        return list.add(item);
    }

    @Override
    public String toString() {
        String item = list.isEmpty() ? "item" : list.get(0).getClass().getSimpleName();
        return item + "[" + list.size() + "]";
    }

}
