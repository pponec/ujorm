/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client.ao;

/**
 * Validation Exception
 * @author Ponec
 */
public class ValidationException extends RuntimeException {

    private final ValidationMessage validationMessage;

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
