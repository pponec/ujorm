/*
 * Copyright 2012-2017 Pavel Ponec, https://github.com/pponec
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
package org.ujorm;

import org.ujorm.validator.ValidationError;
import org.ujorm.validator.ValidationException;
import org.ujorm.validator.impl.ValidatorFactory;

/**
 * Input Validator interface where the interface will be an immutable objects always.
 * See how to use the Validators:
 * <pre class="pre">{@code
 * import org.ujorm.Validator.Build.*;
 *
 * public static final Key<Bo, Long> PID = f.newKey(notNull());
 * public static final Key<Bo, Integer> CODE = f.newKey(between(0, 10));
 * public static final Key<Bo, String> NAME = f.newKey(regexp("T.*T"));
 * public static final Key<Bo, Double> CASH = f.newKey(min(0.0));
 * }</pre>
 *
 * and how to manage exception:
 * <pre class="pre">{@code
 *  try {
 *      Bo ujo = new Eo();
 *      Bo.CODE.setValue(ujo, 10.1);
 *  } catch (ValidationException e) {
 *      String defaultlMsg = e.getError().toString();
 *      String localizedMsg = e.getError().getMessage("My Localized Message: ${INPUT}.", Locale.ENGLISH);
 *      String localizedMsg = e.getError().getMessage(getTemplate(e.g), Locale.ENGLISH);
 *  }
 * }</pre>

 * @author Pavel Ponec
 */
public interface Validator<VALUE> {

    /** Validate the input value and return an non-null result, if the input ís not valid;
     * @param key UJO Key
     * @param bo Target Domain object is not mandatory
     * @return the ValidationError instance or the {
     * @null value} if the result is ok.
     */
    public <UJO extends Ujo> ValidationError validate(Key<UJO, VALUE> key, UJO bo);

    /** Validate the input value and return an non-null result, if the input ís not valid;
     * @param input The input value to validation
     * @param key UJO Key
     * @param bo Target Domain object is not mandatory
     * @return the ValidationError instance or the {
     * @null value} if the result is ok.
     */
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo);

    /** Throw an exception if input value is not valid.
     * @param value Value to validation
     * @param key UJO Key
     * @param bo Target Domain object @null value} if the result is ok.
     */
    public <UJO extends Ujo> void checkValue(VALUE value, Key<UJO, VALUE> key, UJO bo) throws ValidationException;

    /** Throw an exception if input value is not valid.
     * @param key UJO Key
     * @param bo Target Domain object @null value} if the result is ok.
     */
    public <UJO extends Ujo> void checkValue(Key<UJO, VALUE> key, UJO bo) throws ValidationException;

    /** Returns a unique localization key for a constrain type. Two instances
     * of the same Validator class may returns two different keys
     * according the constructor parameter */
    public String getLocalizationKey();

    /** Join current validator with parameter using operator AND */
    public Validator<VALUE> and(Validator<VALUE> validator);

    /** Join current validator with parameter using operator OR.
     * <br>Take note, please, that this operation may have higher requirements for processing.
     * Use the method carefully, or better you create a new implementation of the interface Validate.
     */
    public Validator<VALUE> or(Validator<VALUE> validator);

    /** Validator Factory */
    public static abstract class Build extends ValidatorFactory{}

}
