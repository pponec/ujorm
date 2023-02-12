/*
 *  Copyright 2017-2022 Pavel Ponec
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
 * Database configuration
 * @author Pavel Ponec
 * @see org.ujorm.extensions.StringWraper
 */
public interface UjoDecorator<U extends Ujo> {

    /** Get database model */
    U getDomain();

    /** Get table model */
    KeyList<U> getKeys();

    /** Getter based on the Key */
    <VALUE> VALUE get(Key<? super U, VALUE> key);

    /** Setter based on the Key */
    <VALUE> void set(Key<? super U, VALUE> key, VALUE value);

}
