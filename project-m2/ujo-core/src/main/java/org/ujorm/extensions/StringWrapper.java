/*
 *  Copyright 2007-2022 Pavel Ponec
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


package org.ujorm.extensions;

import org.jetbrains.annotations.NotNull;

/**
 * The method {@link #exportToString()} replaces the original {@link ValueTextable#toString()}.
 * @author Pavel Ponec
 * @see ValueTextable#toString()
 */
public interface StringWrapper extends ValueTextable {

    /** Export the value as String.
     * The method replaces the original {@link ValueTextable#toString()}.
     */
    @NotNull String exportToString();

}
