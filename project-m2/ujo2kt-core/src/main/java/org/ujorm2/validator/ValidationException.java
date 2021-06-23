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

import java.util.HashMap;

/**
 * UJO Validatoin Exception
 * @author Pavel Ponec
 */
public class ValidationException extends IllegalArgumentException {

    private final ValidationError error;

    /** Simple constructor */
    public ValidationException(String localizationKey, String defaultMessage) {
        this(new ValidationError(localizationKey
                , new HashMap<String,Object>()
                , defaultMessage)
                , null);
    }

    /** Full constructor */
    public ValidationException(ValidationError error, Throwable cause) {
        super(error.toString(), cause);
        this.error = error;
    }

    public ValidationError getError() {
        return error;
    }

}
