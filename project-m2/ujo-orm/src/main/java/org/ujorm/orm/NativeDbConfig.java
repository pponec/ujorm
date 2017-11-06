/*
 *  Copyright 2017-2017 Pavel Ponec
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
package org.ujorm.orm;

import javax.annotation.Nonnull;
import org.ujorm.KeyList;
import org.ujorm.core.IllegalUjormException;

/**
 * Bytes wrapper is used to store Java objects to BLOB or BYTEA databse column type.
 * @author Pavel Ponec
 * @see org.ujorm.extensions.StringWraper
 */
public class NativeDbConfig<U extends OrmUjo> implements DbConfig<U> {

    private final U dbModel;
    private final KeyList<U> keyList;

    public <U extends OrmUjo> NativeDbConfig(@Nonnull Class<U> dbClass) {
        this.dbModel = getInstance(dbClass);
        this.keyList = dbModel.readKeys();
    }

    /** Create an instance from the class */
    private U getInstance(@Nonnull Class<?> dbClass) {
        try {
            return (U) dbClass.newInstance();
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("Can't create instance of " + dbClass, e);
        }
    }

    @Override
    public U getDbModel() {
        return dbModel;
    }

    @Override
    public KeyList<U> getTableList() {
        return keyList;
    }

    /** Create new instance */
    public static <U extends OrmUjo> DbConfig<U> of(@Nonnull final Class<U> dbClass) {
        return new NativeDbConfig<U>(dbClass);
    }
}
