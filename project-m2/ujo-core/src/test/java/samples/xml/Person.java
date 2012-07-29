/*
 * Person.java
 *
 * Created on 19. October 2007, 19:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.xml;

import org.ujorm.Key;
import org.ujorm.UjoAction;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.*;
import static org.ujorm.UjoAction.*;

/**
 * @author Pavel Ponec
 */
public class Person extends MapUjo {
    
  public static final Key<Person,String>  NAME   = newKey("Name");
  public static final Key<Person,Boolean> MALE   = newKey("Male");
  public static final Key<Person,Integer> HEIGHT = newKey("Height");

    @Override
  public boolean readAuthorization(UjoAction action, Key property, Object value) {
    switch(action.getType()) {
      case ACTION_XML_EXPORT: 
          return property!=NAME;
      default: {
          return super.readAuthorization(action, property, value);
      }
    }
  }
}
