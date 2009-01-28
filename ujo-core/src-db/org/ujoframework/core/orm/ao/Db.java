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

import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.map.MapUjo;

/**
 * A logical database description.
 * @author pavel
 */
public class Db extends MapUjo {

    /** List of tables */
    public static final ListProperty<Db,DbTable> TABLES = newPropertyList("table", DbTable.class);
    /** Database connection */
    public static final UjoProperty<Db,String> CONNECTION = newProperty("connection", String.class);


}
