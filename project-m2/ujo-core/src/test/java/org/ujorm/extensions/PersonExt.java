/*
 *  Copyright 2007-2022 Pavel Ponec
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


package org.ujorm.extensions;

import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.implementation.map.MapUjoExt;

/**
 *
 * @author Pavel Ponec
 */
public class PersonExt extends MapUjoExt<PersonExt> {
    
    public static final Key<PersonExt, Integer> ID = newKey("id");
    public static final Key<PersonExt, PersonExt> SUPERIOR = newKey("superior");
    public static final ListKey<PersonExt, PersonExt> PERS = newListKey("person");
    
    static {
        init(PersonExt.class);
    }
    
    public PersonExt(Integer id) {
        ID.setValue(this, id);
    }

}
