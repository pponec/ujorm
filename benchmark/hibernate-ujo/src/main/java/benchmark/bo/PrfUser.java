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

import org.ujorm.UjoPropertyList;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.quick.QuickUjo;

/**
 * The User
 * @author Ponec
 */
public class PrfUser extends QuickUjo {

    public static final Property<PrfUser,Long> id = newProperty(Long.class);
    public static final Property<PrfUser,String> personalId = newProperty(String.class);
    public static final Property<PrfUser,String> surename = newProperty(String.class);
    public static final Property<PrfUser,String> lastname = newProperty(String.class);

    // Optional code for better performance when creating instance:
    private static UjoPropertyList properties = init(PrfUser.class);
    @Override public UjoPropertyList readProperties() { return properties; }


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
