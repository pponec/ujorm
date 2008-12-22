/**
 * Sample of implementation
 */

package samples.map.personUjo;

import java.util.HashMap;
import org.ujoframework.*;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.implementation.map.*;

@SuppressWarnings("unchecked")
public class Person implements Ujo {
    
  public static final MapProperty<Person, String > NAME = new MapProperty("Name", String.class);
  public static final MapProperty<Person, Boolean> MALE = new MapProperty("Male", Boolean.class);
  public static final MapProperty<Person, Double > CASH = new MapProperty("Cash", 0d);
  
  // --- The begin of the Ujo implementation ---
  private HashMap map = new HashMap();

  public Object readValue(UjoProperty property) {
    Object result = map.get(property);
    return result!=null ? result : property.getDefault();
  }

  public void writeValue(UjoProperty property, Object value) {
    map.put(property, value);
  }

  public UjoProperty[] readProperties() {
    return UjoManager.getInstance().readProperties(getClass());
  }

  public boolean readAuthorization(UjoAction action, UjoProperty property, Object value) {
    return true;
  }
  // --- The end of UJO implementation --

    
  public void addCash(double cash) {
    double newPrice = CASH.of(this) + cash;
    CASH.setValue(this, newPrice);
  }
}