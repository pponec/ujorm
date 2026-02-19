/*
 *  Copyright 2008-2026 Pavel Ponec
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

package org.ujorm.listener;

import org.ujorm.Key;
import org.ujorm.Ujo;

/**
 * EventRegistrar
 * @since ujo-tool
 * @author Pavel Ponec
 */
public interface EventRegistrar<UJO extends Ujo> {

    /**
     * Add listener
     * @param key Property
     * @param listener Listener
     * @param before The null value means that listener will be called before as well as after reading/writting value to UJO.
     * @return A result of the operation
     */
    boolean addPropertyChangeListener
        ( final Key<? super UJO,?> key
        , final Boolean before
        , final UjoPropertyChangeListener listener
        );


    /** Remove listener */
    boolean removePropertyChangeListener
        ( final Key<? super UJO,?> key
        , final Boolean before
        , final UjoPropertyChangeListener listener
        );

}
