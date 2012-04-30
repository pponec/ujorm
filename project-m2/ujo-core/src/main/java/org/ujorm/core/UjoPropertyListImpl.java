/*
 *  Copyright 2009-2010 Pavel Ponec
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm.core;

import org.ujorm.Ujo;
import org.ujorm.UjoProperty;


/**
 * The immutable list of UjoProperties.
 * The UjoPropertyList class is a subset of the methods from class List&lt;UjoProperty&gt;.
 * @author Pavel Ponec
 * @deprecated Use the PropertyStore indead of this.
 */
@Deprecated
final public class UjoPropertyListImpl<UJO extends Ujo> extends PropertyStore<UJO> {

    /** An empty array of the UJO properties */
    final static public UjoProperty[] EMPTY = new UjoProperty[0];

    public UjoPropertyListImpl(Class<UJO> baseClass, UjoProperty<UJO,?>[] properties) {
        super(baseClass, properties);
    }

}
