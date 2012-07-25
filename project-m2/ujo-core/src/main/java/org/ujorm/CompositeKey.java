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

import java.util.List;

/**
 * A <strong>CompositeKey</strong> interface is a composite of more Key objects.
 * The CompositeKey class can be used wherever is used Key - with a one important <strong>exception</strong>:
 * do not send the CompositeKey object to methods Ujo.readValue(...) and Ujo.writeValue(...) directly!!!
 * <p/>There is prefered two methods UjoManager.setValue(...) / UjoManager.getValue(...)
 * to write and read a value instead of this - or use some type safe solution by UjoExt or a method of Key.
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the property is not included 
 * in the list returned by Ujo.readProperties().
 * 
 * @author Pavel Ponec
 * @since 0.81
 */
public interface CompositeKey<UJO extends Ujo, VALUE> extends UjoProperty<UJO, VALUE> {

    /** Get the first property of the current object. The result is direct property always. */
    public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getLastProperty();

    /** Get the first property of the current object. The result is direct property always. */
    public <UJO_IMPL extends Ujo> Key<UJO_IMPL, VALUE> getFirstProperty();

    /** Get a semifinal value from an Ujo object by a chain of keys.
     * If any value (not getLastPartialProperty) is null, then the result is null.
     */
    public Ujo getSemifinalValue(UJO ujo);

    /** Export all <string>direct</strong> keys to the list from parameter. */
    public void exportProperties(List<Key> result);


}
