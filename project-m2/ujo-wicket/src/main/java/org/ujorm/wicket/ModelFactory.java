/*
 *  Copyright 2013-2026 Pavel Ponec
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
package org.ujorm.wicket;

import java.io.Serializable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;

public class ModelFactory<U extends Ujo & Serializable> extends Model<U> {

    /**
     * Constructor
     * @param domain A domain bean
     */
    public ModelFactory(U domain) {
        super(domain);
    }

    /** Create new model for the key */
    public <T> IModel<T> getModel(Key<U, T> key) {
        return new KeyModel(getObject(), key);
    }

}
