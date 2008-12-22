/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.UjoExt;
import org.ujoframework.implementation.map.MapProperty;

/**
 * User Factory
 * @author pavel
 */
public class UserFactory extends UjoManager {
    
    protected HashMap<String, List<UjoProperty>> properies = new HashMap<String, List<UjoProperty>>();
    
    
   static {
       instance = new UserFactory();
   }
    
   public static UserFactory getInstanc() {
       return (UserFactory) instance;
   }
   
   
   public void createType(String type) {
       properies.put(type, new ArrayList<UjoProperty>());
   }
   
   public Ujo newUjo(String type) {
       return null;
   }

   public UjoExt newUjoExt(String type) {
       return null;
   }
   
   @SuppressWarnings("unchecked")   
   public UjoProperty addProperty(String type, String name, Object defaultValue) {
       final UjoProperty result = new MapProperty(name, defaultValue);
       readPropertiesList(type).add(result);
       return result;
   }
   
   @SuppressWarnings("unchecked")   
   public UjoProperty addProperty(String type, String name, Class valueClass) {
       final UjoProperty result = new MapProperty(name, valueClass);
       readPropertiesList(type).add(result);
       return result;
   }

    @Override
    public UjoProperty[] readProperties(Class type) {
        throw new UnsupportedOperationException();
    }

    public UjoProperty[] readProperties(String type) {
        List<UjoProperty> properties = readPropertiesList(type);
        return properties.toArray(new UjoProperty[properties.size()]);
    }

    protected List<UjoProperty> readPropertiesList(String type) {
        List<UjoProperty> properties = properies.get(type);
        if (properties==null) {
            throw new IllegalArgumentException("Type is not found: " + type);
        } else {
            return properties;
        }
    }
    
    
   
}
