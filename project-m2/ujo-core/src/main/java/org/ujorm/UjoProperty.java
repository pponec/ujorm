/*
 *  Copyright 2007-2010 Pavel Ponec
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

import org.ujorm.core.KeyRing;
import org.ujorm.core.annot.Immutable;

/**
 * This interface is a descriptor of the {@link Ujo} attribute. The property contains only property meta-data
 * and therefore the UjoPropertry implementation never contains business data. 
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
 * @deprecated Use the inteface {@link Key} rather.
 * The interface name will not be deleted, but it will be modified as a parent of the Ujorm Key interface.
 * I'll do no later than version 1.40.
 */
@Deprecated
@Immutable
public interface UjoProperty <UJO extends Ujo,VALUE> extends Key<UJO,VALUE> {


}
