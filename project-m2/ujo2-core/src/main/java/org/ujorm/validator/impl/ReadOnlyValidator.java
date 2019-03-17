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
package org.ujorm.validator.impl;

import org.ujorm.Key;
import org.ujorm.Validator;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.tools.msg.MessageArg;
import org.ujorm.validator.ValidationError;


/**
 * Read only validator or empty validator - according to the constructor argument values.
 * @author Pavel Ponec
 */
public class ReadOnlyValidator<VALUE> extends AbstractValidator<VALUE> {

    /** Sign to read only / all enabled */
    public static final MessageArg<Boolean> READ_ONLY = new MessageArg<>("READ_ONLY");

    /** Sign to a state read-only / all enabled */
    private final boolean readOnly;

    /** Constructor
     * @param readOnly Sign to read only / all enabled
     */
    public ReadOnlyValidator(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** {@inheritDoc} */
    @Override
    public <UJO> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
        final boolean failed = readOnly;
        return failed ? createError
                ( input
                , key
                , bo
                , service.map())
                : null;
    }

    /** Default Message by template:
     * <br>Attribute KEY, is read-only, so the input value must not be assigned: INPUT
     */
    @Override
    protected String getDefaultTemplate() {
        return service.template("Attribute ", KEY, " is read-only, so the input value must not be assigned: ", INPUT);
    }

    /**
     * @return Returns a text accoding the constructor argument:
     * <ul>
     *   <li>org.ujorm..readOnly</li>
     *   <li>org.ujorm..allEnabledt</li>
     * </ul>
     */
    @Override
    public String getLocalizationKey() {
        return KEY_PREFIX + (readOnly ? "readOnly" : "allEnabledt");
    }

    /** Sign to a state read-only / all enabled */
    public boolean isReadOnly() {
        return readOnly;
    }

    /** Check if the validator is type of read-only */
    public static boolean isReadOnly(Validator<?> validator) {
        return validator instanceof ReadOnlyValidator
            && ((ReadOnlyValidator)validator).readOnly;
    }
}
