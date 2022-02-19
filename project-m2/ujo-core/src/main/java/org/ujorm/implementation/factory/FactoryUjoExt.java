/*
 *  Copyright 2008-2022 Pavel Ponec
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

package org.ujorm.implementation.factory;

import org.ujorm.Ujo;
import org.ujorm.Key;


/**
 * En extended <code>Ujo</code> implementation. The method does not implement all <code>UjoExt</code> method!
 *
 * @see FactoryProperty
 * @author Pavel Ponec
 * @since ujo-tool
 * @composed 1 - * FactoryProperty
 */
abstract public class FactoryUjoExt<UJO extends FactoryUjoExt> extends FactoryUjo {

    /** Getter based on one Key */
    @SuppressWarnings("unchecked")
    public <VALUE> VALUE get
        ( Key<? super UJO, VALUE> key) {
        return key.of((UJO) this);
    }

}
