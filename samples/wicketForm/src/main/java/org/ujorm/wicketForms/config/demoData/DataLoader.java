/*
 * Copyright 2013 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.wicketForms.config.demoData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.ujorm.core.UjoManagerCSV;
import org.ujorm.wicketForms.entity.City;
import org.ujorm.wicketForms.entity.Customer;
import org.ujorm.wicketForms.entity.Hotel;
import static org.ujorm.core.UjoService.*;

/**
 * Data loader from CSV resources
 * @author ponec
 */
public class DataLoader {

    /** Get hotels from CSV file as a Map */
    public Map<Integer,City> getCityMap() {
        final List<City> cities = getCities();
        final Map<Integer,City> result = new HashMap<>(cities.size());
        for (City city : cities) {
            result.put(city.getId(), city);
        }
        return result;
    }

    /** Get hotels from CSV file */
    public List<City> getCities() {
        final Scanner scanner = new Scanner(getClass().getResourceAsStream("ResourceCity.csv"), UTF_8.name());
        while (!scanner.nextLine().isEmpty()){}

        UjoManagerCSV manager = UjoManagerCSV.of
                ( City.ID
                , City.NAME
                , City.COUNTRY
                , City.COUNTRY_NAME
                , City.LATITUDE
                , City.LONGITUDE
                );
        return manager.loadCSV(scanner, "CSV import");
    }

    /** Get hotels from CSV file */
    public List<Hotel> getHotels() {
        final Scanner scanner = new Scanner(getClass().getResourceAsStream("ResourceHotel.csv"), UTF_8.name());
        while (!scanner.nextLine().isEmpty()){}

        UjoManagerCSV manager = UjoManagerCSV.of
                ( Hotel.NAME
                , Hotel.NOTE
                , Hotel.CITY.add(City.ID) // The value is a foreign key!
                , Hotel.STREET
                , Hotel.PHONE
                , Hotel.STARS
                , Hotel.HOME_PAGE
                , Hotel.PRICE
                , Hotel.ACTIVE
                );
        List<Hotel> result = manager.loadCSV(scanner, "CSV import");

        // Optionaly assign negative IDs to sign a demo-data:
        for (int i = 0, max = result.size(); i < max; i++) {
            result.get(i).setId(-1L - i);
        }
        return result;
    }

    /** Get hotels from CSV file */
    public List<Customer> getCustomers() {
        final Scanner scanner = new Scanner(getClass().getResourceAsStream("ResourceCustomer.csv"), UTF_8.name());
        UjoManagerCSV manager = UjoManagerCSV.of
                ( Customer.LOGIN
                , Customer.PASSWORD
                , Customer.PASSWORD_HASH
                , Customer.TITLE
                , Customer.FIRSTNAME
                , Customer.SURNAME
                , Customer.EMAIL
                , Customer.ADMIN
                , Customer.ACTIVE
                );
        return manager.loadCSV(scanner, "CSV import");
    }

}
