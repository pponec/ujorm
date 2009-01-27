/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.core.orm.ao;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.orm.DbType;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.map.MapUjo;

/**
 *
 * @author pavel
 */
public class DbColumn extends MapUjo {

    /** DB column name */
    public static final UjoProperty<DbColumn,String> NAME = newProperty("name", String.class);
    /** Class property */
    public static final UjoProperty<DbColumn,UjoProperty> PROPERTY = newProperty("property", UjoProperty.class);
    /** Column NOT-NULL */
    public static final UjoProperty<DbColumn,Boolean> MANDATORY = newProperty("mandatory", false);
    /** Database Type */
    public static final UjoProperty<DbColumn,DbType> TYPE = newProperty("dbType", DbType.UNDEFINED);
    /** Column value length */
    public static final UjoProperty<DbColumn,Integer> MAX_LENGTH = newProperty("maxLength", -1);
    /** Column value precision */
    public static final UjoProperty<DbColumn,Integer> PRECISION = newProperty("precision", -1);
    /** DB Default value */
    public static final UjoProperty<DbColumn,String> DEFAULT_VALUE = newProperty("default", String.class);
    /** DB table */
    public static final ListProperty<DbColumn,DbTable> TABLE = newPropertyList("table", DbTable.class);


    /** Get property value */
    @SuppressWarnings("unchecked")
    final public Object getValue(Ujo ujo) {
        final Object result = PROPERTY.of(this).of(ujo);
        return result;
    }
}
