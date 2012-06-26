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
 * The annotation select an property containing a <strong>body of the element</strong>.
 * There is recommended that only one property was signed by the annoataton in the class.
 * If more annotated properties are identified, than the framework will be considered the valid property with the highest index.
 * <br/>NOTE: If a property has an annotation {@link XmlAttribute} than the {@link XmlElementBody} is ignored.
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface XmlElementBody {
    
}
