/*
 * UnifiedAccess.java
 *
 * Created on 11. listopad 2007, 8:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.patterDesing;

/**
 *
 * @author Pavel Ponec
 */
public class UnifiedAccess extends MyMap {
    
    protected Object readValue(PropertyExt key) {
        Object result = super.readValue(key);
        
        if (result==null
        && key.getType()==Integer.class ){
            return 0;
        } else {
            return result;
        }
    }
    
    
    
}
