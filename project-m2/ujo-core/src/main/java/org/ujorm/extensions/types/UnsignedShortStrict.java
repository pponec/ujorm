/*
 *  Copyright 2018-2018 Pavel Ponec
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
package org.ujorm.extensions.types;

import org.jetbrains.annotations.NotNull;

/**
 * Wrap an {@code Integer} type to an {@code unsigned Short} and back. <br/>
 * This is a sample implementation of the {@code ValueWrapper} interface.
 * @author Pavel Ponec
 */
public class UnsignedShortStrict extends UnsignedShort {

    public UnsignedShortStrict(@NotNull final Short dbValue) {
        super(dbValue);
    }

    public UnsignedShortStrict(@NotNull final Integer appValue) {
        super(appValue);
    }

    /** Adjust the current value to the range or throw an exception, default value is {@code false} */
    @Override
    protected boolean adjustValueToRange() {
        return false;
    }

    // --- Utils ----

    /** Add a value */
    public UnsignedShort plus(final int value) {
        return UnsignedShortStrict.of(applValue + value);
    }

    /** Add a value */
    public UnsignedShort minus(final int value) {
        return UnsignedShortStrict.of(applValue - value);
    }

    // --- Static method ---

    @NotNull
    public static UnsignedShortStrict of(@NotNull final Integer value) {
        return new UnsignedShortStrict(value);
    }

}
