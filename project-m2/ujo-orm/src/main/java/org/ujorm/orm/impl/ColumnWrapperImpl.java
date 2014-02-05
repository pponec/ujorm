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
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * Wrapper for a MetaColumn
 * @author Pavel Ponec
 */
public class ColumnWrapperImpl implements ColumnWrapper {

    private MetaColumn column;
    private String tableAlias;
    private Key key;

    public ColumnWrapperImpl(MetaColumn column, String tableAlias) {
        this(column, tableAlias, column.getKey());
    }

    public ColumnWrapperImpl(MetaColumn column, Key key) {
        this(column, null, key);
    }

    /**
     * Basic constructor
     * @param column Mandatory column
     * @param table Optional table
     * @param key Optional Key
     */
    public ColumnWrapperImpl(MetaColumn column, String tableAlias, Key key) {
        assert column!=null : "The MetaColumn must not be null";
        this.column = column;
        this.tableAlias = tableAlias != null ? tableAlias : column.getTableAlias();
        this.key = key != null ? key : column.getKey();
    }

    /** Returns an original column model */
    public MetaColumn getModel() {
        return column;
    }

    /** Returns an original colum name */
    public String getName() {
        return column.getName();
    }

    /** Returns always the NonNull alias of the related database table */
    public String getTableAlias() {
        return tableAlias;
    }

    /** Returns an original Key */
    public Key getKey() {
        return key;
    }

    /** Is it a composite Key? */
    public boolean isCompositeKey() {
        return key.isComposite();
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
