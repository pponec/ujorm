/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.map.PersonExt;

import org.ujorm.Key;
import org.ujorm.implementation.map.*;
public class Person extends MapUjoExt<Person> {
    
  public static final Key<Person, String > NAME = newProperty("Name", String.class);
  public static final Key<Person, Boolean> MALE = newProperty("Male", Boolean.class);
  public static final Key<Person, Double > CASH = newProperty("Cash", Double.class);
    
  public void addCash(double cash) {
    double newPrice = get(CASH) + cash;
    set(CASH, newPrice);
  }
  
  public void addCash_old(double cash) {
    double newPrice = CASH.getValue(this) + cash;
    CASH.setValue(this, newPrice);
  }  
}
