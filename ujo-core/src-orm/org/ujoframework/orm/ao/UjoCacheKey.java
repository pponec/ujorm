/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.ao;


import java.util.List;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;

/**
 * UJO CacheKey
 * @author pavel
 */
final class UjoCacheKey extends CacheKey {

    /** Value key */
    final private TableUjo tableUjo;
    /** Primary Keys */
    final List<OrmColumn> pk;

    public UjoCacheKey(final TableUjo tableUjo) {
        this(tableUjo, null);
    }

    /**
     * Constructor
     * @param tableUjo BO
     * @param pkey The parameter not mandatory but the one is used for a performance improvements.
     */
    public UjoCacheKey(final TableUjo tableUjo, final OrmPKey pkey) {
        this.tableUjo = tableUjo;
        this.pk = pkey!=null ? OrmPKey.COLUMNS.of(pkey) : getPK() ;
    }

    /** TableUjo class */
    @Override
    public Class getType() {
        return tableUjo.getClass();
    }

    /** Returns valueof PK */
    @Override
    public Object getValue(final int index) {
        return pk.get(index).getValue(tableUjo);
    }

    /** Returns a count of PK */
    @Override
    public int size() {
        return pk.size();
    }

    /** Returns PK of the tableUjo */
    private List<OrmColumn> getPK() {
        final OrmTable table = tableUjo.readSession().getHandler().findTableModel(tableUjo.getClass());
        final OrmPKey ormPKey = OrmTable.PK.of(table);
        final List<OrmColumn> columns = OrmPKey.COLUMNS.of(ormPKey);
        return columns;
    }

}
