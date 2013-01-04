/*
 *  Copyright 2013-2013 Pavel Ponec
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
package org.ujorm;

/**
 * The static methods to check an arguments
 * @author Pavel Ponec
 */
public final class Checks {

    /**
     * Check the {@code not-null} value
     */
    public static <T extends Object> void checkNotNull(T t) throws IllegalArgumentException {
        checkNotNull(t, null);
    }

    /**
     * Check the {@code not-null} value
     */
    public static <T extends Object> void checkNotNull(T t, String message, Object... args) throws IllegalArgumentException {
        if (t == null) {
            if (message == null || message.isEmpty()) {
                message = "Argument must be not null";
            }
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    /**
     * Check the {@code null} value
     */
    public static <T extends Object> void checkTheNull(T t) throws IllegalArgumentException {
        checkNull(t, null);
    }

    /**
     * Check the {@code null} value
     */
    public static <T extends Object> void checkNull(T t, String message, Object... args) throws IllegalArgumentException {
        if (t != null) {
            if (message == null || message.isEmpty()) {
                message = "Argument must be null but the value is: " + t;
            }
            throw new IllegalArgumentException(String.format(message, args));
        }
    }
}
