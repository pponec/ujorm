/*
 *  Copyright 2016-2016 Pavel Ponec
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

package org.version2;

import org.version2.bo.generated.$User;
import org.version2.bo.generated.$Address;
import java.util.Date;
import org.ujorm.Key;
import org.version2.bo.*;
import org.version2.tools.DefaultUjoConverter;

/**
 * DEMO Sample
 * @author Pavel Ponec
 */
public class Sample2 {

    /** Ujo is type of POJO */
    public void run_01() {
        Address address = new $Address();
        address.setId(10);
        address.setCity("Brno");
        address.setCountry("Czech Republic");

        User user = new $User();
        user.setId(100);
        user.setForename("Jan");
        user.setSurname("Novák");
        user.setBirthday(new Date());
        user.setAddress(address);

        assert user.getForename() == "Jan";
        assert user.getAddress().getCity() == "Brno";
    }

    /** Create UJO from POJO */
    public void run_02() {
        Address pojo = new Address();
        pojo.setId(10);
        pojo.setCity("Brno");
        pojo.setCountry("Czech Republic");

        $Address ujo = new $Address(pojo);

        assert ujo != pojo;
        assert ujo.getId() == 10;
        assert ujo.getCity() == "Brno";
        assert ujo.getCity() == pojo.getCity();
    }

    /** Use a composite Key */
    public void run_03() {
        $User user = new $User();
        String city1 = "Kroměříž";
        String city2 = null;
        //
        Key<$User,String> compositeKeyCity = $User.ADDRESS.add($Address.CITY);
        user.set(compositeKeyCity, city1);
        city2 = user.get(compositeKeyCity);

        assert city1 == city2;
        assert city1 == user.getAddress().getCity();
        assert city1 == user.original().getAddress().getCity();
    }

    /** Convert POJO to UJO by a service */
    public void run_04() {
        final DefaultUjoConverter<$User> ujoConverter = new DefaultUjoConverter<$User>();
        //
        User pojo = new User();
        $User ujo = ujoConverter.marshal(pojo);
        assert pojo == ujoConverter.unmarshal(ujo);
        //
        String surname = "Kovář";
        pojo.setSurname(surname);
        assert ujo.getSurname() == surname;
    }

}
