/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.extensions;

/**
 * The ValueAgent make reading or writing a property value.
 * The interface is designed for implementation to a UjoProperty.
 * @author pavel
 */
public interface ValueAgent<UJO,VALUE> {
   
    /** WARNING: There is recommended to call the method from the method Ujo.writeValue(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(final UJO bean, final VALUE value);
    
    /** WARNING: There is recommended to call the method from the method <code>Ujo.readValue(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public VALUE readValue(final UJO bean);
    
}
