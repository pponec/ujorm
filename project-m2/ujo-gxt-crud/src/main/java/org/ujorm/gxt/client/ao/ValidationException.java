/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.ao;

import java.io.Serializable;
import org.ujorm.gxt.client.CMessageException;

/**
 * Validation Exception
 * @author Ponec
 */
public class ValidationException extends CMessageException implements Serializable {

    private ValidationMessage validationMessage;

    public ValidationException() {
        this("");
    }

    public ValidationException(String message) {
        this(new ValidationMessage(null, message));
    }

    public ValidationException(ValidationMessage validationMessage) {
        this.validationMessage = validationMessage;
    }

    public ValidationException(ValidationMessage validationMessage, Throwable cause) {
        super(cause);
        this.validationMessage = validationMessage;
    }


    public ValidationMessage getValidationMessage() {
        return validationMessage;
    }

    public ValidationException newInstance(CharSequence propertyName, String message) {
        ValidationMessage validationMessage = new ValidationMessage(propertyName, message);
        return new ValidationException(validationMessage);
    }



}
