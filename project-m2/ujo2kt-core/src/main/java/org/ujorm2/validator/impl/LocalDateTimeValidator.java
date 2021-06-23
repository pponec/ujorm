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
package org.ujorm2.validator.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.ujorm2.Key;
import org.ujorm.tools.msg.MessageArg;
import org.ujorm2.validator.AbstractValidator;
import org.ujorm2.validator.ValidationError;


/**
 * Data validator compare the input date to the <strong>current local</strong> date-time.
 * @author Pavel Ponec
 */
public class LocalDateTimeValidator<VALUE extends LocalDateTime> extends AbstractValidator<VALUE> {

    /** TODAY date*/
    public static final MessageArg<LocalDate> NOW = new MessageArg<>("NOW");
    /** A sing for Past {@code true} / Future {@code false} */
    public static final MessageArg<Boolean> PAST = new MessageArg<>("PAST");

    /** A sing for Past {@code true} / Future {@code false} */
    private final boolean past;
    private final String pastWord;
    private final ZoneId zoneId;

    /**
     * Between validator
     * @param past Serializable maximum valuem (inclusive)
     */
    public LocalDateTimeValidator(boolean past) {
        this(past, ZoneId.systemDefault());
    }

    /**
     * Between validator
     * @param past Serializable maximum valuem (inclusive)
     */
    public LocalDateTimeValidator(boolean past, ZoneId zoneId) {
        this.past = past;
        this.pastWord = past ? "past" : "future";
        this.zoneId = zoneId;
    }

    /** {@inheritDoc} */
    @Override
    public <D> ValidationError validate(VALUE input, Key<D, VALUE> key, D bo) {

            final LocalDateTime now = LocalDateTime.now(zoneId);
            final boolean ok = input==null
                    || ((input.compareTo(now) <= 0) == past)
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
