/*
 *  Copyright 2013-2022 Pavel Ponec
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
package org.ujorm.wicket.component.form.fields;

import org.ujorm.Key;
import org.ujorm.Ujo;

/**
 * Input textfield with a Label including a feedback message.
 * @author Pavel Ponec
 */
public class TextField<T extends String> extends Field<T> {

    public <U extends Ujo> TextField(Key<U, T> key) {
        super(key);
    }

    public <U extends Ujo> TextField(String componentId, Key<U, T> key, String cssClass) {
        super(componentId, key, cssClass);
    }

}
