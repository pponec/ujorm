/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
