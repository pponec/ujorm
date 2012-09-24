/*
 *  Copyright 2007-2012 Pavel Ponec
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

package org.ujorm_back.listener;

import org.ujorm.extensions.Property;
import org.ujorm.implementation.registrar.*;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends RegistrarUjoExt<Person> {
    
    public static final Property<Person,Integer> ID  = newProperty("id", 0);
    public static final Property<Person,String> NAME = newProperty("name", "");
    public static final Property<Person,Double> CASH = newProperty("cash", 0.0);

}
