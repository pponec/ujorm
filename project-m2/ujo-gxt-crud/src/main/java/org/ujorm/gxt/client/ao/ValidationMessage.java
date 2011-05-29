/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.ao;

import org.ujorm.gxt.client.CujoProperty;
import java.io.Serializable;

/**
 * Validation Message
 * @author Pavel Ponec
 */
public class ValidationMessage implements Serializable {

    private String message;
    private String propertyName;
    private boolean error;
    private Long id;

    protected ValidationMessage() {
    }

    public ValidationMessage(Long id) {
        this.error = false;
        this.id = id;
    }


    public ValidationMessage(CharSequence propertyName, String message) {
        this.error = true;
        this.message = message;
        this.propertyName = propertyName!=null 
                          ? propertyName.toString() 
                          : "" 
                          ;
    }

    /** Returns propertyName */
    public String getPropertyName() {
        return propertyName;
    }

    public String getMessage() {
        return message;
    }

    /** Returns true if propertyNames are the same. */
    public boolean isProperty(CujoProperty p) {
        return p.getName().equals(propertyName);
    }

    @Override
    public String toString() {
        return message;
    }

    public boolean isError() {
        return error;
    }

    public boolean isOk() {
        return !error;
    }


    public Long getId() {
        return id;
    }

}
