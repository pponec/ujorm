/*
 * Person.java
 *
 * Created on 19. October 2007, 19:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.xml;

import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.map.*;
import static org.ujoframework.extensions.UjoAction.*;

/**
 * @author Pavel Ponec
 */
public class Person extends MapUjo {
    
  public static final Property<Person,String>  NAME   = newProperty("Name" , String.class);
  public static final Property<Person,Boolean> MALE   = newProperty("Male" , Boolean.class);
  public static final Property<Person,Integer> HEIGHT = newProperty("Height", Integer.class);

    @Override
  public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
    switch(action.getType()) {
      case ACTION_XML_EXPORT: 
          return property!=NAME;
      default: {
          return super.readAuthorization(action, property, value);
      }
    }
  }
}
