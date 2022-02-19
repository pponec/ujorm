/*
 *  Copyright 2014-2022 Pavel Ponec
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
import org.ujorm.Ujo;

/**
 * A common API to lock an UJO object
 * @author Pavel Ponec
 * @since Ujorm 1.48
 */
public interface UjoLockable extends Ujo {

    /** Lock the UJO to the <strong>read-only</strong> state
     * and release all resources if any.
     * All the {@link List} attributes can be unmodifiabled.
     * Locking a related Ujo objects depends on implementations.
     */
    public void lock();

    /** Returns the value {@code true} if the current object is locked to a read only */
    public boolean readOnly();

    /**
     * Write a value to object if it is possible
     * @param key The direct key
     * @param value New value
     * @throws UnsupportedOperationException Method throws the exception on write value to a locked object.
     */
    @Override
    public void writeValue(Key<?,?> key, Object value) throws UnsupportedOperationException;


}
