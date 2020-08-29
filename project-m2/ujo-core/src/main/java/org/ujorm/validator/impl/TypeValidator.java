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
import org.ujorm.Ujo;
import org.ujorm.tools.msg.MessageArg;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.validator.ValidationError;

/**
 * Validator check a type of value according the Key. If key is missing, thean the {@link NullPointerException} is throwed
 * @author Pavel Ponec
 */
public class TypeValidator<VALUE extends Object> extends AbstractValidator<VALUE> {

    /** Class value */
    public static final MessageArg TYPE = MessageArg.of("TYPE");

    /** Class value */
    private final Class<VALUE> type;

    public TypeValidator() {
        this(null);
    }

    public TypeValidator(Class<VALUE> type) {
        this.type = type;
    }

    /**
     * {@inheritDoc}
     * @param input Input value
     * @param key Key is the mandatory argument for this object!
     * @param bo domain objects
     * @return
     * @throws NullPointerException
     */
    @Override
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) throws NullPointerException {
            final boolean ok = input==null
                  || (type != null ? type : key.getType()).isInstance(input);
            return !ok ? createError
                    ( input
                    , key
                    , bo
                    , service.map
                    ( TYPE, (type != null ? type : key.getType())))
                    : null;
    }

    /** Default Message by template:
     * <br>An input INPUT of the KEY have got the wrong  class: TYPE
     */
    @Override
    protected String getDefaultTemplate() {
        return service.template("An input ", INPUT, " of the ", KEY, " have got the wrong  class: ", TYPE);
    }

    /** @return Returns: "ujorm.org.type" */
    @Override
    public String getLocalizationKey() {
        return KEY_PREFIX + "type";
    }
}
