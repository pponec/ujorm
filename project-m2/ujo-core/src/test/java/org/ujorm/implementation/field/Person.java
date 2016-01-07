/*
 *  Copyright 2007-2014 Pavel Ponec
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


package org.ujorm.implementation.field;

import java.util.List;
import org.ujorm.extensions.ValueAgent;

/**
 * DEMO
 * @author Pavel Ponec
 */
public class Person extends FieldUjo {
    
    private Long cash;
    private List<Person> children;
    
    public static FieldProperty<Person,Long> CASH = newKey("CASH"
        , new ValueAgent<Person,Long>() {
        public void writeValue(
            Person ujo, Long value) { 
               ujo.cash = value; 
            }
        public Long readValue (Person ujo) { 
            return ujo.cash;  
        }
    });    
    
    public static FieldPropertyList<Person,Person> CHILDREN 
        = newListKey("CHILDREN"
        , new ValueAgent<Person,List<Person>>() {
        public void writeValue(Person ujo, List<Person> value) {
            ujo.children = value; 
        }
        public List<Person> readValue(Person ujo) {
            return ujo.children; 
        }
    });
}
