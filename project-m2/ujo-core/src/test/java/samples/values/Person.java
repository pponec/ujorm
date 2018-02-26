/*
 *  Copyright 2014-2014 Pavel Ponec
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
package samples.values;

import java.util.Date;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.implementation.quick.SmartUjo;

public class Person extends SmartUjo<Person> {

    /** Key factory */
    private static final KeyFactory<Person> f = newFactory(Person.class);

    public static final Key<Person,String> NAME = f.newKey();
    public static final Key<Person,Boolean> MALE_GENDER = f.newKey();
    public static final Key<Person,Date> BIRTHDATE = f.newKey();
    public static final Key<Person,Person> MOTHER = f.newKey();
    public static final Key<Person,Person> FATHER = f.newKey();

    static {
        f.lock();
    }

    /** Mother's name ; */
    public static final CompositeKey<Person,String> MOTHERS_NAME = MOTHER.add(NAME);
    /** Father's name ; */
    public static final CompositeKey<Person,String> FATHERS_NAME = FATHER.add(NAME);
    /** Grandmothers name of the mother  */
    public static final CompositeKey<Person,String> GRANDMOTHERS_NAME = MOTHER.add(MOTHER).add(NAME);
    /** Grandfather's name of father */
    public static final CompositeKey<Person,String> GRANDFATHERS_NAME = FATHER.add(FATHER).add(NAME);


    /** Equals */
    public boolean equals(Object obj) {
        return UjoManager.getInstance().equalsUjo(this, (Ujo) obj);
    }

}
