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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.extensions.ValueWrapper;
import org.ujorm.tools.Assert;

/**
 * A generic interface to mapping any Java Type to any JDBC type.
 * A default constructor with one argument type of {@code DbType} is required and
 * the one static {@code ValueWrapper newInstance() } with a non-null value is required for a Technical issues.
 * The instance is {@code Comparable} only if the {@code applValue} is {@code Comparable}.
 * @author Pavel Ponec
 * @param <DbValue> The type that JDBC knows how to handle out of the box.
 * @param <AppValue> The type that JDBC doesn't know how to handle.
 */
public abstract class AbstractValueWrapper<DbValue, AppValue> implements ValueWrapper<DbValue, AppValue>, Comparable<AbstractValueWrapper> {

    @Nonnull
    protected final AppValue applValue;

    /** Constructor */
    public AbstractValueWrapper(@Nonnull AppValue appValue) {
        Assert.notNull(appValue);
        this.applValue = appValue;
    }

    /** Get the application value */
    @Override
    public final AppValue get() {
        return applValue;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass().equals(obj.getClass())
            && applValue.equals(((AbstractValueWrapper)obj).applValue);
    }

    @Override
    public int hashCode() {
        return applValue.hashCode();
    }

    @Override
    public int compareTo(AbstractValueWrapper o) {
        return ((Comparable)applValue).compareTo(o.applValue);
    }

    @Override @Nonnull
    public String toString() {
        return String.valueOf(applValue);
    }
}
