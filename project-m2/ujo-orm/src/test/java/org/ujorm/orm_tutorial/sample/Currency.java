/*
 *  Copyright 2020-2022 Pavel Ponec
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
package org.ujorm.orm_tutorial.sample;

import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, how the Keys are created by the KeyFactory.
 * @hidden
 */
@Table("ord_currency")
public final class Currency extends OrmTable<Currency> {
    private static final KeyFactory<Currency> f = newCamelFactory(Currency.class);

    /** Unique key */
    @Column(pk = true, length = 3)
    public static final Key<Currency, String> CODE = f.newKey();
    /** NOTE */
    public static final Key<Currency, Integer> NOTE = f.newKey();

    // Lock the Key factory
    static { f.lock(); }

    /** An optional method for a better performance.
     * @return Return all direct Keys (An implementation from the Ujo API)
     */
    @Override
    public KeyList<Currency> readKeys() {
        return f.getKeys();
    }

    // --- An optional implementation of commonly used setters and getters ---


}
