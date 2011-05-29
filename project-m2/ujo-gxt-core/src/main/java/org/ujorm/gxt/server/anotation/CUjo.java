/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.server.anotation;
import java.lang.annotation.*;

/** Annotation can be used to pair with a class. */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface CUjo {

    /** Relation to CUJO class */
    Class<org.ujorm.gxt.client.Cujo> value();
    
}
