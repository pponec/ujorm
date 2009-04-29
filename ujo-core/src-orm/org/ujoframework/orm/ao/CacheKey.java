/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.ao;

import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;

/**
 * Cache key.
 * @author pavel
 */
final public class CacheKey {

    /** Value key */
    final private TableUjo tableUjo;
    private int hash = 0;

    public CacheKey(final TableUjo tableUjo) {
        this.tableUjo = tableUjo;
    }

    /** Has the two objects the same PK ? */
    @Override
    public boolean equals(Object obj) {
        final CacheKey key = (CacheKey) obj;
        if (this.tableUjo.getClass()!=key.tableUjo.getClass()) {
            return false;
        }
        final boolean result = getPK().equals(this.tableUjo, key.tableUjo);
        return result;
    }

    /** Returns PK of the BO. */
    private OrmPKey getPK() {
        final OrmTable table = tableUjo.readSession().getHandler().findTableModel(tableUjo.getClass());
        final OrmPKey ormPKey = OrmTable.PK.of(table);
        return ormPKey;
    }

    @Override
    public int hashCode() {
        if (hash==0) {
            int hsh = 7;
            final OrmPKey ormPKey = getPK();

            for (OrmColumn ormColumn : OrmPKey.COLUMNS.of(ormPKey)) {
                Object val = ormColumn.getValue(tableUjo);
                if (true) {
                   hsh = 67 * hsh + val.hashCode();
                } else {
                   // Primary key must not be NULL !!!
                   hsh = 67 * hsh + (val!=null ? val.hashCode() : 0);
                }
            }
            hash = hsh!=0 ? hsh : 1 ; // no zero
        }
        return hash;
    }

}
