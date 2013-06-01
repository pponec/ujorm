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
package org.ujorm.hotels.config.demoData;

import java.util.List;
import java.util.Scanner;
import org.ujorm.core.UjoManagerCSV;
import org.ujorm.hotels.domains.Customer;
import org.ujorm.hotels.domains.Hotel;
import org.ujorm.orm.InitializationBatch;
import org.ujorm.orm.Session;

/**
 * Data loader from CSV resources
 * @author ponec
 */
public class ResourceLoader implements InitializationBatch {

    /** Load data from a CSV file */
    @Override
    public void run(Session session) throws Exception {
        if (!session.exists(Hotel.class)) {
            session.save(getHotels());
        }
        if (!session.exists(Customer.class)) {
            session.save(getCustomers());
        }
    }

    /** Get hotels from CSV file */
    private List<Hotel> getHotels() throws Exception {
        final Scanner scanner = new Scanner(getClass().getResourceAsStream("ResourceHotels.csv"));
        while (!scanner.nextLine().isEmpty()){}

        UjoManagerCSV manager = UjoManagerCSV.getInstance
                ( Hotel.NAME
                , Hotel.NOTE
                , Hotel.CITY
                , Hotel.STREET
                , Hotel.PHONE
                , Hotel.STARS
                , Hotel.HOME_PAGE
                , Hotel.PRICE
                , Hotel.ACTIVE
                );
        return manager.loadCSV(scanner, "CSV import");
    }

    /** Get hotels from CSV file */
    private List<Customer> getCustomers() throws Exception {
        final Scanner scanner = new Scanner(getClass().getResourceAsStream("ResourceCustomers.csv"));
        UjoManagerCSV manager = UjoManagerCSV.getInstance
                ( Customer.LOGIN
                , Customer.PASSWORD
                , Customer.PASSWORD_HASH
                , Customer.ACTIVE
                , Customer.FIRSTNAME
                , Customer.SURENAME
                , Customer.EMAIL
                );
        return manager.loadCSV(scanner, "CSV import");
    }

}
