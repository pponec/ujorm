/*
 *  Copyright 2020-2020 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujorm2.core;

import org.ujorm.tools.Assert;
import org.ujorm2.Key;

/**
 * Proxy Domain
 * @author Pavel Ponec
 */
public class MKey<V> {

    private Key<?, V> model;

    public <T extends Key<?, V>> T get() {
        return (T) model;
    }

    void close(AbstractDomainModel model) {
        Assert.validState(this.model == null, "Object is closed");
        this.model = Assert.notNull(model, "model");
    }
}
