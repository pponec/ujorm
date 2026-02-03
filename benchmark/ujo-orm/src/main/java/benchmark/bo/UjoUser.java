/*
 *  Copyright 2020-2026 Pavel Ponec
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


package benchmark.bo;

import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.annot.Column;

/**
 * User
 * @author Pavel Ponec
 */
public class UjoUser extends OrmTable<UjoUser> {
    private static final OrmKeyFactory<UjoUser> f = newCamelFactory(UjoUser.class);

    @Column(pk=true)
    public static final Key<UjoUser,Long> ID = f.newKey("id");
    @Column(length=8)
    public static final Key<UjoUser,String> PERSONAL_ID = f.newKey("personalId");
    public static final Key<UjoUser,String> SURENAME = f.newKey("surename");
    public static final Key<UjoUser,String> LASTNAME = f.newKey("lastname");

    static { f.lock(); }

    /** An optional method for a better performance */
    @Override
    public KeyList<UjoUser> readKeys() {
        return f.getKeys();
    }
}
