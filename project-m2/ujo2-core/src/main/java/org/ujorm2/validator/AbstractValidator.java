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
package org.ujorm2.validator;

import java.io.Serializable;
import java.util.Map;
import org.ujorm.tools.msg.MessageArg;
import org.ujorm.tools.msg.MessageService;
import org.ujorm2.Key;
import org.ujorm2.Validator;
import org.ujorm2.criterion.BinaryOperator;
import org.ujorm2.validator.impl.CompositeValidator;

/**
 * Abstract Validator
 * @author Pavel Ponec
 */
public abstract class AbstractValidator<VALUE> implements Validator<VALUE>, Serializable {

    /** Ujo-key */
    public static final MessageArg<String> KEY = new MessageArg<>("KEY");
    /** Input value */
    public static final MessageArg<Object> INPUT = new MessageArg<>("INPUT");
    /** Two-character mark ("${") to introducing a template argument.
     * @see MessageService#PARAM_BEG */
    public static final MessageArg<Object> MARK = new MessageArg<>("MARK");

    /** Validator service */
    protected static final MessageService service = new MessageService();

    /** Localization Key Prefix */
    public static final String KEY_PREFIX = "validator.";

    /** {@inheritDoc} */
    @Override
    public final <D> ValidationError validate(Key<D, VALUE> key, D bo) {
        return validate(key.of(bo), key, bo);
    }

    /** {@inheritDoc} */
    @Override
    public final <D> void checkValue(final Key<D, VALUE> key, final D bo) throws ValidationException {
        checkValue(key.of(bo), key, bo);
    }

    /** Check the value without context */
    public final <D> void checkValue(VALUE input) throws ValidationException {
        checkValue(input, null, null);
    }

    /** Check the value with a context */
    @Override
    public <D> void checkValue(VALUE input, Key<D, VALUE> key, D bo) throws ValidationException {
        final ValidationError result = validate(input, key, bo);
        if (result!=null) {
            throw new ValidationException(result, null);
        }
    }

    /**
     * Create an error object.
     * @param <D> Domain object type
     * @param input Input value (required)
     * @param key Ujo Key (required)
     * @param bo Domain objet (required)
     * @param params A map of arguments (required)
     * @return An instance of the class {@link ValidationError}.
     */
    protected <D> ValidationError createError
            ( final VALUE input
            , final Key<D, VALUE> key
            , final D bo
            , final Map<String, Object> params) {

                throw new UnsupportedOperationException("TODO");

//        return new ValidationError
//        ( input
//        , key
//        , bo
//        , getClass()
//        , getLocalizationKey()
//        , getDefaultTemplate()
//        , params);
    }

   /** Returns a default message template without parametes
    * @see String#format(java.lang.String, java.lang.Object[])
    */
    protected abstract String getDefaultTemplate();

    /** {@inheritDoc} */
    @Override
    public final Validator<VALUE> and(Validator<VALUE> validator) {
        return new CompositeValidator<>(this, BinaryOperator.AND, validator);
    }

    /** {@inheritDoc} */
    @Override
    public final Validator<VALUE> or(Validator<VALUE> validator) {
        return new CompositeValidator<>(this, BinaryOperator.OR, validator);
    }

    /** Returns a localization key. */
    @Override
    public String toString() {
        return getLocalizationKey();
    }

}