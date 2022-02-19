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
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.SmartUjo;

/**
 * Company
 * @author Pavel Ponec
 */
public class Company extends SmartUjo<Element> {

    private static final KeyFactory<Company> f = newCamelFactory(Company.class);

    public static final Key<Company, String> NAME = f.newKey();
    public static final Key<Company, Customer> DIRECTOR = f.newKey();
    public static final ListKey<Company, Customer> EMPLOYEE = f.newListKey();

    // Lock the Key factory
    static { f.lock(); }

}
