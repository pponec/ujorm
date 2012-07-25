/*
 *  Copyright 2009 Paul Ponec
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
import org.ujorm.orm.annot.Column;

/**
 *
 * @author Pavel Ponec
 */
public class UjoUser extends OrmTable<UjoUser> {

    @Column(pk=true)
    public static final Key<UjoUser,Long> ID = newKey("id");
    @Column(length=8)
    public static final Key<UjoUser,String> PERSONAL_ID = newKey("personalId");
    public static final Key<UjoUser,String> SURENAME = newKey("surename");
    public static final Key<UjoUser,String> LASTNAME = newKey("lastname");



    // Optional code for better performance:
    private static KeyList properties = init(UjoUser.class);
    @Override public KeyList readKeys() { return properties; }
}
