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

import java.util.regex.Pattern;
import org.ujorm2.Key;
import org.ujorm.tools.msg.MessageArg;
import org.ujorm2.validator.AbstractValidator;
import org.ujorm2.validator.ValidationError;


/**
 * Pattern alidator
 * @author Pavel Ponec
 */
public class PatternValidator<VALUE extends String> extends AbstractValidator<VALUE> {

    /** The simple email pattern. For a better regular expression see the <a href="http://ex-parrot.com/~pdw/Mail-RFC822-Address.html">next link</a> */
    public static final String EMAIL = "^[\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,3}$";

    public static final MessageArg<String> PATTERN = new MessageArg<String>("pattern");

    /** Regual expression */
    public final Pattern pattern;
    /** Is the pattern the instance of the {@link EMAIL} pattern */
    public final boolean mail;

    /**
     * Pattern validator
     * @param regexp
     */
    protected PatternValidator(Pattern pattern) {
        this.pattern = pattern;
        this.mail = EMAIL.endsWith(pattern.pattern());
    }

    /** {@inheritDoc} */
    public <D> ValidationError validate(VALUE input, Key<D, VALUE> key, D bo) {
            final boolean ok = input==null
                    || pattern.matcher(input).matches();
            return !ok ? createError
                    ( input
                    , key
                    , bo
                    , service.map
                    ( PATTERN, pattern.pattern()
                    ))
                    : null;
    }

    /** Default Message by template:
     * <br>Value for KEY must natch the regular expression PATTERN, but the input is: INPUT
     */
    @Override
    protected String getDefaultTemplate() {
        return mail
                ? service.template("An attribute ", KEY, " must be a well-formed email address, the the input is not: ", INPUT)
                : service.template("An attribute ", KEY, " must natch the regular expression '", PATTERN, "', but the input is: ", INPUT)
                ;
    }

    /** @return Returns: "ujorm.org.notNull" */
    public String getLocalizationKey() {
        return KEY_PREFIX + (mail ? "email" : "regexp");
    }

}
