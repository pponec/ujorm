/*
 *  Copyright 2017-2017 Pavel Ponec
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

import javax.annotation.Nonnull;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoDecorator;
import org.ujorm.core.IllegalUjormException;

/**
 * Native implementation of UjoDecorator
 * @author Pavel Ponec
 * @see org.ujorm.extensions.StringWraper
 */
public class NativeUjoDecorator<U extends Ujo> implements UjoDecorator<U> {

    /** Instance of the configuration */
    protected final U domain;

    public <U extends Ujo> NativeUjoDecorator(@Nonnull Class<U> domainClass) {
        this.domain = getInstance(domainClass);
    }

    /** Create an instance from the class */
    private U getInstance(@Nonnull Class<?> domainClass) {
        try {
            return (U) domainClass.newInstance();
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("Can't create instance of " + domainClass, e);
        }
    }

    @Override
    public U getDomain() {
        return domain;
    }

    @Override
    public KeyList<U> getKeys() {
        return domain.readKeys();
    }

    @Override
    public <VALUE> VALUE get(@Nonnull final Key<? super U, VALUE> key) {
        return key.of(domain);
    }

    @Override
    public <VALUE> void set(@Nonnull final Key<? super U, VALUE> key, VALUE value) {
        key.setValue(domain, value);
    }

    /** Create new instance */
    public static <U extends Ujo> UjoDecorator<U> of(@Nonnull final Class<U> domainClass) {
        return new NativeUjoDecorator<U>(domainClass);
    }
}
