/*
 * Copyright 2013 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.wicket.component.form;

import java.io.Serializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.KeyRing;
import org.ujorm.tools.Assert;
import org.ujorm.validator.ValidationError;

/**
 * Ujorm validator for the Wicket
 * @author Pavel Ponec
 */
public class UiValidator<T> implements IValidator<T>, INullAcceptingValidator<T>, Serializable {

    /** Localization key prefix */
    public static final String PROPERTY_PREFIX = "validator.";

    /** Native Ujorm validator */
    private Validator<T> validator;
    /** Required key */
    private KeyRing<Ujo> key;

    /**
     * Constructor for a special Wicket validator
     * @param validator Required validator
     * @param key Optional key
     */
    public UiValidator(Validator<T> validator, Key<Ujo,T> key) {
        this(validator, KeyRing.of(key));
    }

    /**
     * Constructor for a special Wicket validator
     * @param validator Required validator
     * @param key Optional key
     */
    public UiValidator(@NotNull Validator<T> validator, @Nullable KeyRing key) {
        this.validator = Assert.notNull(validator, "validator");
        this.key = key;
    }

    /** TODO: Localization */
    @Override
    public void validate(IValidatable<T> validatable) {
        final ValidationError error = validator.validate
                ( validatable.getValue()
                , key != null ? (Key) key.getFirstKey() : null
                , null);
        if (error != null) {
            org.apache.wicket.validation.ValidationError wicketErr = new org.apache.wicket.validation.ValidationError();
            wicketErr.setMessage(error.getDefaultMessage()
                    + " [" + error.getLocalizationKey() + "]");
            wicketErr.addKey(error.getLocalizationKey() + "." + key.getFirstKey().getName());
            wicketErr.addKey(error.getLocalizationKey());
            wicketErr.setVariables(error.getArguments());

            validatable.error(wicketErr);
        }
    }

    /** Returns an original validator */
    public Validator getValidator() {
        return validator;
    }

    /** A description of the validator */
    @Override
    public String toString() {
        return validator.toString();
    }
}
