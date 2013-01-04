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

package org.ujorm.orm;

import org.ujorm.Key;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * Wrapper for a MetaColumn
 * @author Pavel Ponec
 */
public interface ColumnWrapper {

    /** Returns a meta-tablemodel */
    public MetaColumn getModel();

    /** Returns a unique table name in the one SQL statement. */
    public TableWrapper getTable();

    /** Returns an original key */
    public Key getKey();

    /** Returns if key is Direct */
    public boolean isDirectKey();

    /** Method retuns the {@code true} value if two attribut Keys are the same */
    @Override
    public boolean equals(Object column);

}
