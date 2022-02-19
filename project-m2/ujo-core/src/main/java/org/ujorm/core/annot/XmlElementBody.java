/*
 *  Copyright 2008-2022 Pavel Ponec
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

package org.ujorm.core.annot;
import java.lang.annotation.*;

/**
 * The annotation select an key containing a <strong>body of the element</strong>.
 * There is recommended that only one key was signed by the anoatation in the class.
 * If more annotated keys are identified, than the framework will be considered the valid key with the highest index.
 * <br>NOTE: If a key has an annotation {@link XmlAttribute} than the {@link XmlElementBody} is ignored.
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface XmlElementBody {

}
