/*
 *  Copyright 2018-2022 Pavel Ponec
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
import org.ujorm.extensions.ValueWrapper;
import org.ujorm.tools.Assert;

/**
 * Wrap an {@code Integer} type to an {@code unsigned Short} and back. <br/>
 * This is a sample implementation of the {@code ValueWrapper} interface.
 * @author Pavel Ponec
 */
public class UnsignedShort extends AbstractValueWrapper<Short, Integer> {

    /** Min app value */
    public static final int MIN_VALUE = 0;
    /** Max app value  */
    public static final int MAX_VALUE = Short.MAX_VALUE - Short.MIN_VALUE;

    /** Persistent value 0 */
    protected static final Short DB_ZERO = Short.MIN_VALUE;
    /** Persistent value 1 */
    protected static final Short DB_ONE = (short) (DB_ZERO + 1);
    /** Persistent value 2 */
    protected static final Short DB_TWO = (short) (DB_ZERO + 2);
    /** Persistent value 3 */
    protected static final Short DB_THREE = (short) (DB_ZERO + 3);

    /** Default persistent value a request of the {@link interface} */
    public static final Short _PERSISTENT_DEFAULT_VALUE = DB_ZERO;

    /** Public constructor for a <stong>persistent</stong> value where the type {@link Object} is required. */
    public UnsignedShort(@NotNull final Short dbValue) {
        super(toApplValue(dbValue));
    }

    /** Protected constructor */
    protected UnsignedShort(@NotNull final Integer appValue) {
        super(appValue);
    }

    /** Value converter from a {@code dbType} to an {@code appType} */
    private static int toApplValue(final Short dbValue) {
        return dbValue - DB_ZERO;
    }

    /** Value of of border is trimmed. */
    @Override
    public Short readPersistentValue() {
        int value = applValue;
        if (value > MAX_VALUE) {
            value = MAX_VALUE;
            Assert.isTrue(adjustValueToRange(), "Value {} is out the limit {}.", value, MAX_VALUE);
        } else
        if (value < MIN_VALUE) {
            value = MIN_VALUE;
            Assert.isTrue(adjustValueToRange(), "Value {} is out the limit {}.", value, MIN_VALUE);
        }
        switch (value) {
            case 0:
                return DB_ZERO;
            case 1:
                return DB_ONE;
            case 2:
                return DB_TWO;
            case 3:
                return DB_THREE;
            default:
                return (short) (DB_ZERO + value);
        }
    }

    /** Database class */
    @Override
    public Class<Short> readPersistentClass() {
        return Short.class;
    }

    /** Adjust the current value to the range or throw an exception, default value is {@code true} */
    protected boolean adjustValueToRange() {
        return true;
    }

    // --- Utils ----

    /** Add a value */
    public UnsignedShort plus(final int value) {
        return UnsignedShort.of(applValue + value);
    }

    /** Add a value */
    public UnsignedShort minus(final int value) {
        return UnsignedShort.of(applValue - value);
    }

    // --- Static method ---

    @NotNull
    public static UnsignedShort of(@NotNull final Integer value) {
        return new UnsignedShort(value);
    }

    /** Instance factory for a Technical issues where value is not significant */
    @NotNull
    public static ValueWrapper getInstance() throws ReflectiveOperationException {
        return of(0);
    }


}
