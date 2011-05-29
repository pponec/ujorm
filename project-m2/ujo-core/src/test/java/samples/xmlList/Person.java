/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.xmlList;

import org.ujorm.UjoProperty;
import org.ujorm.extensions.ListProperty;
import org.ujorm.implementation.map.*;
public class Person extends MapUjo { 
  public static final UjoProperty    <Person, String> NAME = newProperty("Name", String.class);
  public static final ListProperty<Person, Person> CHILDS = newListProperty("Child", Person.class);
}
