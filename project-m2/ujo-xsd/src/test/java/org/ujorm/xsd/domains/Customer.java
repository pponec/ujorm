/*
 *  Copyright 2014-2022 Pavel Ponec
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
package org.ujorm.xsd.domains;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.SmartUjo;

/**
 *
 * @author Pavel Ponec
 */
public class Customer extends SmartUjo<Element> {

    private static final KeyFactory<Customer> f = newCamelFactory(Customer.class);

    public static final Key<Customer, Integer> PID = f.newKey();
    public static final Key<Customer, TitleEnum> TITLE = f.newKey();
    public static final Key<Customer, String> NAME = f.newKey();

    // Lock the Key factory
    static { f.lock(); }

}
