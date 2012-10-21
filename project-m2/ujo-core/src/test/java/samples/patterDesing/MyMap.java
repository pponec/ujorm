/*
 * MyMap.java
 *
 * Created on 1. listopad 2007, 21:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.patterDesing;

import java.util.HashMap;
import java.util.Map;


public class MyMap {
    
    public static final Key<String> NAME = new Property<String>();
    public static final Key<Integer> AGE = new Property<Integer>();
    private Map<Property, Object> data = new HashMap<Property, Object>();
    
    protected Object readValue(Property key) {
        return data.get(key);
    }
    protected void writeValue(Property key, Object value) {
        data.put(key, value);
    }
    
    // Test:
    static class UsageTest {
        public UsageTest() {
            MyMap map = new MyMap();
            MyMap.NAME.setValue(map, "Peter Prokop");
            MyMap.AGE .setValue(map, 22);
            
            String name = MyMap.NAME.getValue(map);
            int    age  = MyMap.AGE.getValue(map);
            System.out.println(name + " is " + age);
        }
    }
}




