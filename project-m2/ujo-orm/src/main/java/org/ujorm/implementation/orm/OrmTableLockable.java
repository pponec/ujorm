/*
 *  Copyright 2014-2026 Pavel Ponec
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

package org.ujorm.implementation.orm;

import org.ujorm.Key;
import org.ujorm.extensions.UjoLockable;

/**
 * An abstract implementation of the OrmUjo with an object locking support.
 * @author Pavel Ponec
 * @since Ujorm 1.48
 */
public abstract class OrmTableLockable<U extends OrmTableLockable> extends OrmTable<U>
implements UjoLockable {

    /** A read-only state */
    private boolean readOnly;

    /** {@inheritDoc} */
    @Override
    public void lock() {
        readOnly = true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean readOnly() {
        return readOnly;
    }

    /** A write method checking a lock sign. */
    @Override
    public void writeValue(final Key<?,?> key, final Object value) throws UnsupportedOperationException {
        if (readOnly) {
            throw new UnsupportedOperationException("The object is locked: " + this);
        }
        super.writeValue(key, value);
    }
}
