/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.ao;


import java.util.List;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;

/**
 * UJO CacheKey
 * @author pavel
 */
final class UjoCacheKey extends CacheKey {

    /** Value key */
    final private OrmUjo ormUjo;
    /** Primary Keys */
    final List<OrmColumn> pk;

    public UjoCacheKey(final OrmUjo ormUjo) {
        this(ormUjo, null);
    }

    /**
     * Constructor
     * @param ormUjo BO
     * @param pkey The parameter not mandatory but the one is used for a performance improvements.
     */
    public UjoCacheKey(final OrmUjo ormUjo, final OrmPKey pkey) {
        this.ormUjo = ormUjo;
        this.pk = pkey!=null ? OrmPKey.COLUMNS.of(pkey) : getPK() ;
    }

    /** OrmUjo class */
    @Override
    public Class getType() {
        return ormUjo.getClass();
    }

    /** Returns valueof PK */
    @Override
    public Object getValue(final int index) {
        return pk.get(index).getValue(ormUjo);
    }

    /** Returns a count of PK */
    @Override
    public int size() {
        return pk.size();
    }

    /** Returns PK of the OrmUjo */
    private List<OrmColumn> getPK() {
        final OrmTable table = ormUjo.readSession().getHandler().findTableModel(ormUjo.getClass());
        final OrmPKey ormPKey = OrmTable.PK.of(table);
        final List<OrmColumn> columns = OrmPKey.COLUMNS.of(ormPKey);
        return columns;
    }

}
