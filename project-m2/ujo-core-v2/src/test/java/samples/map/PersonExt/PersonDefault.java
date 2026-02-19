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
public class PersonDefault extends MapUjoExt<PersonDefault> {
    
  public static final Key<PersonDefault, String > NAME = newKey("Name", "");
  public static final Key<PersonDefault, Boolean> MALE = newKey("Male", true);
  public static final Key<PersonDefault, Double > CASH = newKey("Cash", 0d);

  static {
     init(PersonDefault.class );
  }
    
  public void addCash(double cash) {
    //double newPrice2 = get(Person.CASH) + cash;
    double newPrice = get(CASH) + cash;
    set(CASH, newPrice);
  }
}
