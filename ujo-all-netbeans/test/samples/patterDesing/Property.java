/*
 * Property.java
 *
 * Created on 1. listopad 2007, 21:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.patterDesing;

@SuppressWarnings("unchecked")
public class Property<VALUE> {
    public void setValue(MyMap map, VALUE value) {
        map.writeValue(this, value);
    }
    public VALUE getValue(MyMap map)  {
        return (VALUE) map.readValue(this);
    }
}
