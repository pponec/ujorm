/*
 * Copyright 2014, Pavel Ponec
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
package org.ujorm.hotels.services;

import org.ujorm.KeyList;
import org.ujorm.extensions.UjoMiddle;
import org.ujorm.hotels.entity.enums.Module;

/**
 * Module Parameters interface
 * @author ponec
 */
public interface ModuleParams<U extends ModuleParams> extends UjoMiddle<U>  {

    /** Returns a module */
    public Module getModule();

    /** {@inheritDoc } */
    @Override
    public KeyList<U> readKeys();

}
