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

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.tools.msg.MessageArg;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.validator.ValidationError;

/**
 * Data validator compare the input date to the <strong>current local</strong> date-time.
 * @author Pavel Ponec
 */
public class DateValidator<VALUE extends Date> extends AbstractValidator<VALUE> {

    /** TODAY date*/
    public static final MessageArg<Date> NOW = MessageArg.of("NOW");
    /** A sing for Past {@code true} / Future {@code false} */
    public static final MessageArg<Boolean> PAST = MessageArg.of("PAST");

    /** A sing for Past {@code true} / Future {@code false} */
    private final boolean past;
    private final String pastWord;

    /**
     * Between validator
     * @param past Past
     */
    public DateValidator(boolean past) {
        this.past = past;
        this.pastWord = past ? "past" : "future";
    }

    /** {@inheritDoc} */
    @Override
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {

            final Long now = System.currentTimeMillis();
            final boolean ok = input==null
                    || (input.getTime() <= now == past)
                    ;
            return !ok ? createError
                    ( input
                    , key
                    , bo
                    , service.map
                    ( PAST, past
                    , NOW, now
                    ))
                    : null;
    }

    /** Default Message by template:
     * <br>Value for KEY is out the past/future at time NOW, but the input is: INPUT
     */
    @Override
    protected String getDefaultTemplate() {
        return service.template("An attribute ", KEY, " is out the ", pastWord, " at time "
                , NOW, ", but the input is: ", INPUT);
    }


    /**
     * @return Returns a text accoding the constructor argument:
     * <ul>
     *   <li>org.ujorm..dateLimit.past</li>
     *   <li>org.ujorm..dateLimit.future</li>
     * </ul>
     */
    @Override
    public String getLocalizationKey() {
        return KEY_PREFIX + "dateLimit." + pastWord;
    }


}
