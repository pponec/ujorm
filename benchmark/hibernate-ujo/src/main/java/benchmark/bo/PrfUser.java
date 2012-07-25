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
import org.ujorm.implementation.quick.QuickUjo;

/**
 * The User
 * @author Ponec
 */
public class PrfUser extends QuickUjo {

    public static final Key<PrfUser,Long> id = newKey();
    public static final Key<PrfUser,String> personalId = newKey();
    public static final Key<PrfUser,String> surename = newKey();
    public static final Key<PrfUser,String> lastname = newKey();

    // Optional code for better performance when creating instance:
    private static KeyList properties = init(PrfUser.class);
    @Override public KeyList readKeys() { return properties; }


    // Setters and Getters:
    public Long getId() {
        return id.of(this);  // Note: the of() method is an alias for getValue()
    }

    public void setId(Long anId) {
        id.setValue(this, anId);
    }

    public String getLastname() {
        return lastname.of(this);
    }

    public void setLastname(String aLastname) {
        lastname.setValue(this, aLastname);
    }

    public String getPersonalId() {
        return personalId.of(this);
    }

    public void setPersonalId(String aPersonalId) {
        personalId.setValue(this, aPersonalId);
    }

    public String getSurename() {
        return surename.of(this);
    }

    public void setSurename(String aSurename) {
        surename.setValue(this, aSurename);
    }

}
