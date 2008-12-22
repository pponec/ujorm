/*
 * Person.java
 *
 * Created on 9. èerven 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.xmlList;

import org.ujoframework.implementation.map.*;
public class Person extends MapUjo { 
  public static final MapProperty    <Person, String> NAME = newProperty("Name", String.class);
  public static final MapPropertyList<Person, Person> CHILDS = newPropertyList("Child", Person.class);
}
