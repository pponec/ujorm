/*
 *  Copyright 2007-2013 Pavel Ponec
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

import java.util.List;

/**
 * A <strong>CompositeProperty</strong> interface is a composite of more Key objects.
 * The CompositeProperty class can be used wherever is used Key - with a one important <strong>exception</strong>:
 * do not send the CompositeProperty object to methods Ujo.readValue(...) and Ujo.writeValue(...) directly!!!
 * <p/>There is prefered two methods UjoManager.setValue(...) / UjoManager.getValue(...)
 * to write and read a value instead of this - or use some type safe solution by UjoExt or a method of Key.
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the property is not included 
 * in the list returned by Ujo.readProperties().
 * 
 * @author Pavel Ponec
 * @since 0.81
 * @deprecated Use the interface {@link CompositeKey} rather.
 */
@Deprecated
public interface CompositeProperty<UJO extends Ujo, VALUE> extends CompositeKey<UJO, VALUE> {



}
