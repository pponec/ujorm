/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.set;

import javax.annotation.Nullable;

/**
 * Implementation of the Map with customize hash and equals functions.
 * @author Pavel Ponec
 */
public interface MapKeyProxy<K> {

    /** Calculate hash code */
    @Override
    public int hashCode();

    /** Make equals */
    @Override
    public boolean equals(@Nullable Object o);

    /** Get the original key */
    public K getOriginal() ;

}
