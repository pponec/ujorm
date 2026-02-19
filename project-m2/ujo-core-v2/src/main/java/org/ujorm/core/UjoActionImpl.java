/*
 *  Copyright 2007-2026 Pavel Ponec
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

package org.ujorm.core;

import org.jetbrains.annotations.Nullable;
import org.ujorm.UjoAction;

/**
 * A default implementation of the UjoAction.
 * @author Pavel Ponec
 */
public class UjoActionImpl implements UjoAction {

    private final int type;
    @Nullable
    private final Object context;

    public UjoActionImpl(int type, @Nullable Object context) {
        this.type = type;
        this.context = context;
    }

    public UjoActionImpl(Object context) {
        this(ACTION_UNDEFINED, context);
    }

    public UjoActionImpl(int type) {
        this(type, null);
    }

    /** Returns a type of the action. The default type is ACTION_UNDEFINED.
     * <ul>
     * <li>Numbers are reserved in range (from 0 to 999, inclusive) for an internal usage of the Ujorm.</li>
     * <li>Zero is an undefined action</li>
     * <li>Negative values are free for general usage too</li>
     * </ul>
     * <br>The number can be useful for a resolution of an action for a different purpose (e.g. export to 2 different XML files).
     */
    @Override
    public final int getType() {
        return type;
    }

    /** Returns a conetxt of the action. The value is dedicated to a user usage and the value can be null. */
    @Nullable
    @Override
    public final Object getContext() {
        return context;
    }

    /** String value */
    @Override
    public String toString() {
        String result = type + ", " + context;
        return result;
    }
}
