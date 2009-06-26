/**
 * Sample of implementation
 */

package samples.map.personUjo;

import java.util.HashMap;
import org.ujoframework.*;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.UjoPropertyImpl;
import org.ujoframework.extensions.UjoAction;


public class Person implements Ujo {
    
  public static final UjoProperty<Person, String > NAME = UjoPropertyImpl.newInstance("Name", String.class);
  public static final UjoProperty<Person, Boolean> MALE = UjoPropertyImpl.newInstance("Male", Boolean.class);
  public static final UjoProperty<Person, Double > CASH = UjoPropertyImpl.newInstance("Cash", 0d);
  
  // --- The begin of the Ujo implementation ---
  private HashMap map = new HashMap();

  public Object readValue(UjoProperty property) {
    Object result = map.get(property);
    return result!=null ? result : property.getDefault();
  }

    @SuppressWarnings("unchecked")
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