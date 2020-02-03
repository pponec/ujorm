/*
 * Copyright 2007-2020 Pavel Ponec, https://github.com/pponec
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

package org.ujorm2;

import javax.annotation.concurrent.Immutable;
import org.ujorm2.core.KeyRing;

/**
 * This interface is a descriptor of the {@link Ujo} attribute. The Key contains only meta-data
 * and therefore the Property implementation never contains business data. 
 * Each instance of the Key must be located in the {@code public static final} field of some Ujo implementation.
 * The Key can't have a serializable feature never, because its instance is the unique for a related java field.
 * An appropriate solution solution for serialization is to use a decorator class KeyRing.
 * <br>See a <a href="package-summary.html#UJO">general information</a> about current framework or see some implementations.
 *
 * @author Pavel Ponec
 * @see Ujo
 * @opt attributes
 * @opt operations
 * @see KeyRing
 */
@Immutable
@SuppressWarnings("deprecation")
public interface KeyGroup<D, V> {


}
