/*
 *  Copyright 2020-2020 Pavel Ponec
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
package org.ujorm2.core;

import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;

/**
 *
 * @author Pavel Ponec
 */
public abstract class DirectKeyRing<D> {

    private ModelProvider modelProvider;

    @Nonnull
    public abstract KeyFactory<? super D> getKeyFactory();

    public ModelProvider getContext() {
        return modelProvider;
    }

    public void setContext(@Nonnull ModelProvider ujoContext) {
        Assert.validState(this.modelProvider == null, "Context was assigned");
        this.modelProvider = Assert.notNull(ujoContext);
    }

}
