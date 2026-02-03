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
package org.ujorm.implementation.quick;

import java.util.Collections;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.extensions.UjoLockable;
import org.ujorm.tools.Check;

/**
 * The smart Ujo implementation with a Lock support.
 * @author Pavel Ponec
 * @composed 1 - * Property
 * @since Ujorm 1.48
 */
abstract public class SmartUjoLockable<U extends SmartUjoLockable>
    extends SmartUjo<U> implements UjoLockable {

    /** A read-only state */
    private boolean readOnly;

    /** Lock the object and all properties type of {@link List}. */
    @Override
    public void lock() {
        if (!readOnly) {
            readOnly = true;

            // Make a immutable lists:
            for (final Key<U, ?> p : readKeyList()) {
                if (p.isTypeOf(List.class)) {
                    final List list = (List) p.of((U)this);
                    // Skip validators:
                    writeValue(p, Check.isEmpty(list)
                    ? Collections.EMPTY_LIST
                    : Collections.unmodifiableList(list));
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean readOnly() {
        return readOnly;
    }

    /** A write method checking a lock sigh. */
    @Override
    public void writeValue(final Key<?,?> key, final Object value) throws UnsupportedOperationException {
        if (readOnly) {
            throw new UnsupportedOperationException("The object is locked: " + this);
        }
        super.writeValue(key, value);
    }

}
