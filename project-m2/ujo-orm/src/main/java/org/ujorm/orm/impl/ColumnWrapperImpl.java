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

package org.ujorm.orm.impl;

import org.ujorm.Key;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.TableWrapper;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * Wrapper for a MetaColumn
 * @author Pavel Ponec
 */
public class ColumnWrapperImpl implements ColumnWrapper {

    private MetaColumn column;
    private TableWrapper table;
    private Key key;

    public ColumnWrapperImpl(MetaColumn column, TableWrapper table) {
        this(column, table, column.getKey());
    }

    public ColumnWrapperImpl(MetaColumn column, Key key) {
        this(column, null, key);
    }

    /**
     * Basic constructor
     * @param column Mansatory column
     * @param table Optional table
     * @param key Optional Key
     */
    public ColumnWrapperImpl(MetaColumn column, TableWrapper table, Key key) {
        assert column!=null : "The MetaColumn must not be null";
        this.column = column;
        this.table = table != null ? table : column.getTable();
        this.key = key != null ? key : column.getKey();
    }

    public MetaColumn getModel() {
        return column;
    }

    public TableWrapper getTable() {
        return table;
    }

    /** Returns an original Key */
    public Key getKey() {
        return key;
    }

    /** Is it the direct Key? */
    public boolean isDirectKey() {
        return key.isDirect();
    }

    /** Two models are the same if its key names are the same for the same domain type. */
    @Override
    public boolean equals(Object relation) {
        if (relation instanceof ColumnWrapper) {
            final Key argKey = ((ColumnWrapper) relation).getKey();
            return getKey().getName().equals(argKey.getName())
                && getKey().getDomainType() == argKey.getDomainType();
        } else {
            return false;
        }
    }

    /** The hashCode form the Key name */
    @Override
    public int hashCode() {
        return getKey().getName().hashCode();
    }

    /** Returns the Key */
    @Override
    public String toString() {
        return key.toStringFull();
    }




}
