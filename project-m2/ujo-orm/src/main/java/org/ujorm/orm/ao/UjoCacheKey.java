/*
 *  Copyright 2020-2026 Pavel Ponec
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

package org.ujorm.orm.ao;


import java.util.List;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaPKey;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * UJO CacheKey
 * @author Pavel Ponec
 */
final class UjoCacheKey extends CacheKey {

    /** Value key */
    final private OrmUjo bo;
    /** Primary Keys */
    final List<MetaColumn> pk;

    public UjoCacheKey(final OrmUjo bo) {
        this(bo, null);
    }

    /**
     * Constructor
     * @param ormUjo BO
     * @param pkey The parameter not mandatory but the one is used for a performance improvements.
     */
    public UjoCacheKey(final OrmUjo bo, final MetaPKey pkey) {
        this.bo = bo;
        this.pk = pkey!=null ? MetaPKey.COLUMNS.of(pkey) : getPK() ;
    }

    /** OrmUjo class */
    @Override
    public Class getType() {
        return bo.getClass();
    }

    /** Returns valueof PK */
    @Override
    public Object getValue(final int index) {
        return pk.get(index).getValue(bo);
    }

    /** Returns a count of PK */
    @Override
    public int size() {
        return pk.size();
    }

    /** Returns PK of the OrmUjo */
    private List<MetaColumn> getPK() {
        final MetaTable table = bo.readSession().getHandler().findTableModel(bo.getClass());
        final MetaPKey ormPKey = MetaTable.PK.of(table);
        final List<MetaColumn> columns = MetaPKey.COLUMNS.of(ormPKey);
        return columns;
    }

}
