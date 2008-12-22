/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.field;

import java.util.ArrayList;
import org.ujoframework.extensions.ValueAgent;

/**
 * DEMO
 * @author pavel
 */
public class Person extends FieldUjo {
    
    private Long cash;
    private ArrayList<Person> childs;
    
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
        = newPropertyList("CHILDS", Person.class
        , new ValueAgent<Person,ArrayList<Person>>() {
        public void writeValue(Person ujo, ArrayList<Person> value) { 
            ujo.childs = value; 
        }
        public ArrayList<Person> readValue(Person ujo) { 
            return ujo.childs; 
        }
    });
}
