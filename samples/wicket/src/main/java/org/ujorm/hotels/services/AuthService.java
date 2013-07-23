/*
 * Copyright 2013, Pavel Ponec
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
package org.ujorm.hotels.services;

import org.apache.wicket.Session;
import org.ujorm.hotels.entity.Customer;

/**
 * Common database service
 * @author Pavel Ponec
 */
public interface AuthService {

    /** Authenticate the user and save the result to the Wicket session */
    public boolean authenticate(Customer customer, Session session);

    /** Logout */
    public void logout(Session session);

    /** Is logged user ? */
    public boolean isCustomer(Session session);

    /** Is logged admin */
    public boolean isAdmin(Session session);

    /** Get current customer from session  */
    public Customer getCurrentCustomer(Session session);

    /** Get a hash from the text */
    public long getHash(String text) throws IllegalStateException;


}
