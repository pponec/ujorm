/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.xmlList;

import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.map.*;
public class Person extends MapUjo { 
  public static final UjoProperty    <Person, String> NAME = newProperty("Name", String.class);
  public static final ListProperty<Person, Person> CHILDS = newListProperty("Child", Person.class);
}
