/*
 *  Copyright 2007-2022 Pavel Ponec
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

package org.ujorm.listener;

import org.ujorm.Key;
import org.ujorm.implementation.registrar.*;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends RegistrarUjoExt<Person> {
    
    public static final Key<Person,Integer> ID  = newKey("id", 0);
    public static final Key<Person,String> NAME = newKey("name", "");
    public static final Key<Person,Double> CASH = newKey("cash", 0.0);

}
