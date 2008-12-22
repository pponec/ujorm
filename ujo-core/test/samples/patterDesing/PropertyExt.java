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
public class PropertyExt<VALUE> extends Property<VALUE> {
    
    private final Class type;
    
    public PropertyExt(Class type) {
        this.type = type;
    }
    
    public Class getType() {
        return type;
    }
    
    public void setValue(UnifiedAccess map, VALUE value) {
        map.writeValue(this, value);
    }
    public VALUE getValue(UnifiedAccess map)  {
        return (VALUE) map.readValue(this);
    }    
    
    
}
