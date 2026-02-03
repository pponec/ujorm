/*
 *  Copyright 2007-2026 Pavel Ponec
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

/**
 * The ValueAgent make reading or writing a key value.
 * The interface is designed for implementation to a Key.
 * @author Pavel Ponec
 */
public interface ValueAgent<UJO,VALUE> {

    /** WARNING: There is recommended to call the method from the method Ujo.writeValue(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    void writeValue(final UJO bean, final VALUE value);

    /** WARNING: There is recommended to call the method from the method <code>Ujo.readValue(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    VALUE readValue(final UJO bean);

}
