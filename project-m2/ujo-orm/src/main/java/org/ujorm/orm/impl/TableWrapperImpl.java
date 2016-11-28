/*
 *  Copyright 2012-2016 Pavel Ponec
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

package org.ujorm.orm.impl;

import java.util.ArrayList;
import java.util.List;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.TableWrapper;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * Wrapper for a MetaTable
 * @author Pavel Ponec
 */
public class TableWrapperImpl implements TableWrapper {

    private final MetaTable table;
    private final String alias;
    private final List<ColumnWrapper> columns;

    public TableWrapperImpl(MetaTable table, String alias) {
        this.table = table;
        this.alias = alias;
        this.columns = new ArrayList<ColumnWrapper>(16);
    }

    @Override
    public MetaTable getModel() {
        return table;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public List<? extends ColumnWrapper> getColumns() {
        return columns;
    }

    /** Table model and alias must be the same */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash +  this.table.hashCode();
        hash = 71 * hash + (this.alias != null ? this.alias.hashCode() : 0);
        return hash;
    }

    /** Table model and alias must be the same */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TableWrapperImpl)) {
            return false;
        }
        final TableWrapperImpl other = (TableWrapperImpl) obj;
        if (this.table != other.table) {
            return false;
        }
        if (this.alias != other.alias
        && (this.alias == null || !this.alias.equals(other.alias))
        ){
            return false;
        }
        return true;
    }

}
