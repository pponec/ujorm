/*
 *  Copyright 2018-2026 Pavel Ponec
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

import java.io.Serializable;
import java.lang.reflect.Field;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.msg.MsgFormatter;

/**
 * A generic interface to mapping any Java Type to any JDBC type.
 * A default constructor with one argument type of {@code DbType} is required and
 * the one public static constant {@code _PERSISTENT_DEFAULT_VALUE} with non-null is required for non-null value - for a Technical issues only).
 *
 * <h3>How to get a persistent type:</h3>
 * <code pre="code">
 * ValueWrapper.getInstance(TargetValueWrapper.class).readPersistentClass();
 * </code>
 *
 * <h3>How to get default persistent value:</h3>
 * <code pre="code">
 * ValueWrapper.getInstance(TargetValueWrapper.class).readPersistentValue();
 * </code>
 *
 * <h3>How to get default application value:</h3>
 * <code pre="code">
 * ValueWrapper.getInstance(TargetValueWrapper.class).get();
 * </code>
 *
 * @author Pavel Ponec
 * @param <DbValue> The type that JDBC knows how to handle out of the box.
 * @param <AppValue> The type that JDBC doesn't know how to handle.
 * @see StringWrapper
 * @see StringWrapper javax.xml.bind.annotation.adapters.XmlAdapter
 */
public interface ValueWrapper<DbValue, AppValue>
        extends Serializable /*, Supplier<AppValue> -- It is not good idea */ {

    /** Name a static final non-null attributed with a <strong>defaut persistent value</strong> ready to create new instance. */
    String _PERSISTENT_DEFAULT_VALUE_NAME = "_PERSISTENT_DEFAULT_VALUE";

    /** Get an application type */
    @NotNull AppValue get();

    /** Get a value as a database type */
    @NotNull DbValue readPersistentValue();

    /** The type that JDBC knows how to handle out of the box. */
    @NotNull Class<DbValue> readPersistentClass();

    /** Instance factory for a Technical issues where value is not significant */
    @NotNull
    static <T extends ValueWrapper> T getInstance(@NotNull final Class<T> wrapperClass) {
        try {
            final Field field = wrapperClass.getField(_PERSISTENT_DEFAULT_VALUE_NAME);
            return wrapperClass.getConstructor(field.getType()).newInstance(field.get(null));
        } catch (ReflectiveOperationException e) {
            final String msg = MsgFormatter.format
                    ( "The class {} has not implemented {} interface correctly."
                    , wrapperClass
                    , ValueWrapper.class.getSimpleName());
            throw new IllegalArgumentException(msg, e);
        }
    }

    /** Instance factory for a Technical issues where value is not significant */
    @NotNull
    static <T extends ValueWrapper> T getInstance(@NotNull final Class<T> wrapperClass, @Nullable final Object dbValue) {
        if (dbValue == null) {
            return null;
        }
        try {
            return wrapperClass.getConstructor(wrapperClass.getField(_PERSISTENT_DEFAULT_VALUE_NAME).getType()).newInstance(dbValue);
        } catch (ReflectiveOperationException e) {
            final String msg = MsgFormatter.format
                    ( "The class {} has not implemented {} interface correctly."
                    , wrapperClass
                    , ValueWrapper.class.getSimpleName());
            throw new IllegalArgumentException(msg, e);
        }
    }
}
