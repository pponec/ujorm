/*
 * XmlAttribute.java
 *
 * Created on 13. duben 2008, 11:55
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.core.annot;
import java.lang.annotation.*;

/** 
 * Use the annotation to mark a Key static field to a transient state.
 * Values of the transient properties are not stored by any way.
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface Transient {
    
}
