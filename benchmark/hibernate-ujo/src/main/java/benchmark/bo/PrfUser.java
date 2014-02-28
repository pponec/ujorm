/*
 *  Copyright 2009 Pavel Ponec
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
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.QuickUjo;

/**
 * The User
 * @author Ponec
 */
public class PrfUser extends QuickUjo {
    private static final KeyFactory<PrfUser> f = newCamelFactory(PrfUser.class);

    public static final Key<PrfUser,Long> ID = f.newKey();
    public static final Key<PrfUser,String> PERSONAL_ID = f.newKey();
    public static final Key<PrfUser,String> SURENAME = f.newKey();
    public static final Key<PrfUser,String> LASTNAME = f.newKey();

    // Optional code for better performance when creating instance:
    static { f.lock(); }

    @Override
    public KeyList<PrfUser> readKeys() { return f.getKeys(); }

    public Long getId() {
        return ID.of(this);
    }

    public void setId(Long id) {
        PrfUser.ID.setValue(this, id);
    }

    public String getPersonalId() {
        return PERSONAL_ID.of(this);
    }

    public void setPersonalId(String personalId) {
        PrfUser.PERSONAL_ID.setValue(this, personalId);
    }

    public String getSurename() {
        return SURENAME.of(this);
    }

    public void setSurename(String surename) {
        PrfUser.SURENAME.setValue(this, surename);
    }

    public String getLastname() {
        return LASTNAME.of(this);
    }

    public void setLastname(String lastname) {
        PrfUser.LASTNAME.setValue(this, lastname);
    }

}
