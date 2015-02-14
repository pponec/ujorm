/*
 * Copyright 2015, Pavel Ponec
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
package org.ujorm.wicket.component.dialog;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.SmartUjo;

/**
 * Dummy Ujo object t
 * @author Pavel Ponec
 */
public class SelectUjo extends SmartUjo<SmartUjo> {

    /** Factory */
    private static final KeyFactory<SelectUjo> f = newFactory(SelectUjo.class);
    /** SELECT dummy action */
    public static final Key<SelectUjo, String> SELECT = f.newKey("Select");

    static { f.lock(); }


}
