package org.version2;

import org.version2.bo.generated.$User;
import org.version2.bo.generated.$Address;
import java.util.Date;
import org.version2.bo.*;
import org.version2.tools.UjoPojoConverter;

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
        Address address = new Address();
        address.setId(10);
        address.setCity("Brno");
        address.setCountry("Czech Republic");

        $Address ujoAddress = new $Address(address);

        assert ujoAddress.getId().intValue() == 10;
        assert ujoAddress.getCity() == "Brno";
    }

    /** Use a composite Key */
    public void run_03() {
        $User user = new $User();
        String city1 = "Kroměříž";
        String city2 = null;
        //
        user.set($User.ADDRESS.add($Address.CITY), city1);
        city2 = user.get($User.ADDRESS.add($Address.CITY));

        assert city1 == city2;
        assert city1 == user.getAddress().getCity();
        assert city1 == user.original().getAddress().getCity();
    }

    /** Use POJO to UJO converter */
    public void run_04() {
        User user = new User();
        String surname = "Kovář";
        user.setSurname(surname);
        $User ujoUser = new UjoPojoConverter<$User>().marshal(user);

        assert ujoUser.getSurname() == surname;
    }

}
