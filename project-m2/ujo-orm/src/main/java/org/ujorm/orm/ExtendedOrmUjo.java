/*
 *  Copyright 2020-2022 Pavel Ponec
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

/**
 * Extended ORM Ujo. Interface methods are not necessary to run the ORM.
 * @author Ponec
 */
public interface ExtendedOrmUjo<U extends ExtendedOrmUjo> extends OrmUjo {

   /** Read the foreign key.
     * This is useful to obtain the foreign key value without (lazy) loading the entire object.
     * If the lazy object is loaded, the method will need the Session to build the ForeignKey instance.
     * <br>NOTE: The method is designed for developers only, the Ujorm doesn't call it newer.
     * @return If no related object is available, then the result has the NULL value.
     * @throws IllegalStateException Method throws an exception for a wrong key type.
     * @throws NullPointerException Method throws an exception if a Session is missing after a lazy initialization of the key.
     */
    public <UJO extends U> ForeignKey readFK(Key<UJO, ? extends OrmUjo> key) throws IllegalStateException;

}
