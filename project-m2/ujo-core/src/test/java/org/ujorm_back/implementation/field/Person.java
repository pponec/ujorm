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

package org.ujorm_back.implementation.field;

import java.util.List;
import org.ujorm.extensions.ValueAgent;
import org.ujorm.implementation.field.FieldProperty;
import org.ujorm.implementation.field.FieldPropertyList;
import org.ujorm.implementation.field.FieldUjo;

/**
 * DEMO
 * @author Pavel Ponec
 */
public class Person extends FieldUjo {
    
    private Long cash;
    private List<Person> childs;
    
    public static FieldProperty<Person,Long> CASH 
        = newProperty("CASH", Long.class
        , new ValueAgent<Person,Long>() {
        public void writeValue(
            Person ujo, Long value) { 
               ujo.cash = value; 
            }
        public Long readValue (Person ujo) { 
            return ujo.cash;  
        }
    });    
    
    public static FieldPropertyList<Person,Person> CHILDS 
        = newListProperty("CHILDS", Person.class
        , new ValueAgent<Person,List<Person>>() {
        public void writeValue(Person ujo, List<Person> value) {
            ujo.childs = value; 
        }
        public List<Person> readValue(Person ujo) {
            return ujo.childs; 
        }
    });
}
