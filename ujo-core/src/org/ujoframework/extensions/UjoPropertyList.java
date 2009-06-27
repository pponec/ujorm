/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.extensions;

import java.util.List;
import org.ujoframework.Ujo;

/**
 * A list property interface of Unified Data Object.
 * @author Pavel Ponec
 * @deprecated Use the ListUjoProperty interface instead of it.
 */
public interface UjoPropertyList<UJO extends Ujo, LIST extends List<ITEM>,ITEM> extends ListUjoProperty<UJO,LIST,ITEM> {
    
    
}
