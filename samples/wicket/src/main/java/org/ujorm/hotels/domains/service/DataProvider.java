/*
 *  Copyright 2012 pavel.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujorm.hotels.domains.service;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import org.ujorm.hotels.domains.Customer;

/**
 * Data DataProvider
 * @author pavel
 */
final public class DataProvider {

    public static List<Customer> getEmployees() {
        return getEmployees(100);
    }
    
    public static List<Customer> getEmployees(int limit) {
        final AbstractList<Customer> people = new ArrayList<Customer>();
        return people;
    }

    private static boolean student(int id) {
        return (id%2)==0;
    }
    
}
