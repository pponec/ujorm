/*
 * Copyright 2012-2016 Pavel Ponec
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
package org.ujorm2.core;

/**
 * Common exception of the Ujorm framework
 * @author Pavel Ponec
 */
public class IllegalUjormException extends IllegalStateException {

    public IllegalUjormException(String s) {
        super(s);
    }

    public IllegalUjormException(Throwable cause) {
        super(cause);
    }

    public IllegalUjormException(String message, Throwable cause) {
        super(message, cause);
    }

}
