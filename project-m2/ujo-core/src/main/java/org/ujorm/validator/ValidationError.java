/*
 *  Copyright 2012-2014 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.validator;

import org.ujorm.tools.MessageService;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.KeyRing;
import org.ujorm.tools.Assert;
import static org.ujorm.validator.AbstractValidator.*;

/**
 * UJO Validatoin error
 * @author Pavel Ponec
 */
public class ValidationError implements Serializable {

    /** Service tools */
    private static final MessageService service = new MessageService();
    /** Wrong value */
    private final Object value;
    /** Related UJO Key container is wrapped due a the serialization */
    private final KeyRing key;
    /** Class of the original validator */
    private final Class<? extends Validator> validatorClass;
    /** Target Ujo Object */
    private final Ujo bo;
    /** Default Error message template (with no assigned arguments) */
    private final String messageTemplate;
    /** Localization key */
    private final String localizationKey;
    /** Message serializable arguments */
    private final Map<String, Object> arguments;

    /** Common constructor
     * @param localizationKey Mandatory attribute
     * @param arguments  Mandatory attribute
     * @param defaultMessage Mandatory attribute
     */
    public ValidationError(String localizationKey, Map<String, Object> arguments, String defaultMessage) {
        this(null, null, null, AbstractValidator.class, localizationKey, defaultMessage, arguments);
    }

    /** Constructor
     * @param value Optional attribute
     * @param key Optional attribute
     * @param bo Optional attribute
     * @param validatorClass Mandatory attribute
     * @param localizationKey Mandatory attribute
     * @param defaultMessage Mandatory attribute
     * @param arguments  Mandatory attribute
     */
    public ValidationError(Object value, Key key, Ujo bo, Class<? extends Validator> validatorClass, String localizationKey, String defaultMessage, Map<String, Object> arguments) {
        chechNotNull(validatorClass, "validatorClass", key);
        chechNotNull(localizationKey, "localizationKey", key);
        chechNotNull(defaultMessage, "defaultMessage", key);
        chechNotNull(arguments, "arguments", key);
        //
        this.value = value;
        this.key = KeyRing.of(key);
        this.bo = bo;
        this.messageTemplate = defaultMessage;
        this.validatorClass = validatorClass;
        this.localizationKey = localizationKey;
        this.arguments = arguments;
        // Additional message arguments:
        arguments.put(KEY.toString(), key!=null ? key.getFullName() : "''");
        arguments.put(INPUT.toString(), value);
        arguments.put(MARK.toString(), MessageService.PARAM_BEG);
    }

    /** Check a not null argument */
    private void chechNotNull(Object value, String argumentName, Key key) throws IllegalArgumentException {
        Assert.notNull(value, "The argument '{}' must not be the null in the {} validator"
                , argumentName
                , key);
    }

    /** @return the input value */
    public Object getValue() {
        return value;
    }

    /** @return the ujorm key */
    public Key getKey() {
        return key.getFirstKey();
    }

    /** @return the Ujo business object */
    public Ujo getBo() {
        return bo;
    }

    /** Class of the original validator */
    public Class<? extends Validator> getValidatorClass() {
        return validatorClass;
    }

    /** @return the localizationKey */
    public String getLocalizationKey() {
        return localizationKey;
    }

    /** @return the default message template (with no assigned arguments). */
    public String getDefaultTemplate() {
        return messageTemplate;
    }

    /** Get default message (default template + arguments) */
    public String getDefaultMessage() {
        return getMessage(messageTemplate, Locale.ENGLISH);
    }

    /** Get message from any text template with a named parameters according the sample: <strong>${argument}</strong>.
     * Each variable must be surrounded by two marks "${" and "}".
     * The first mark is forbidden in a common text and can be replaced by the variable #{MARK}.
     * The argument can contains an optional format expression by example: <strong>${argument,format}</strong>.
     * See the class {@link java.util.Formatter} for more information.
     * @param template A messate template. See a valid template and message with its parameters:
     * <pre>{@code "The input value ${KEY} must be less than: ${NUMBER,%+9.2f} EUR."}</pre>
     * <pre>{@code "The input value Cache must be less than: +12345.00 EUR."}</pre>
     * @param locale The target locale for an argument format, the {@code null} locale will be replaced by the ENGLISH locale.
     * @return The target message
     * @see java.util.Formatter
     */
    public String getMessage(String template, Locale locale) {
        return service.format(template, arguments, locale);
    }

    /** Get message from any text template with a named parameters according to the sample: <strong>${argument}</strong>.
     * The argument can contain an optional format expression by example: <strong>${argument,format}</strong>.
     * See the class {@link java.util.Formatter} for more information.
     * @param template A messate template. See a valid template and message with its parameters:
     * <pre>{@code "The input value ${KEY} must be less than: ${NUMBER,%+9.2f} EUR."}</pre>
     * <pre>{@code "The input value Cache must be less than: +12345.00 EUR."}</pre>
     * @return The target message, where arguments will be formatted by the ENGLISH locale.
     * @see java.util.Formatter
     */
    public String getMessage(String template) {
        return service.format(template, arguments, (Locale)null);
    }

    /** Get message from any text template with a named parameters according the sample: <strong>${argument}</strong>.
     * The argument can contains an optional format expression by example: <strong>${argument,format}</strong>.
     * See the class {@link java.util.Formatter} for more information.
     * @param template A messate template. See a valid template and message with its parameters:
     * <pre>{@code "The input value ${KEY} must be less than: ${NUMBER,%+9.2f} EUR."}</pre>
     * <pre>{@code "The input value Cache must be less than: +12345.00 EUR."}</pre>
     * @param locale The target locale for an argument format, the {@code null} locale will be replaced by the ENGLISH Locale.
     * @return The target message
     * @see java.util.Formatter
     */
    /**
     *
     * @param template Template message, see the method {@link #template(java.lang.Object[]) } for more information.
     * @param locale Rarget locale
     * @return Target message
     */
    public String getMessage(Object[] template, Locale locale) {
        return getMessage(service.template(template), locale);
    }

    /** @return the serialiable arguments */
    public Map<String, Object> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return getDefaultMessage();
    }
}
