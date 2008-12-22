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

package org.ujoframework.core;

import java.util.List;

/**
 * List include metaInfo.
 * @author Pavel Ponec
 */
class ListElement {
    
    final private List<Object> list;
    final private Class itemType;
    
    public ListElement(List<Object> list, Class itemType) {
        this.list     = list;
        this.itemType = itemType;
    }

    /** Get List */
    public List getList() {
        return list;
    }

    /** Get Item type */
    public Class getItemType() {
        return itemType;
    }
    
    /** Add new item to Listl */
    public void add(Object item) {
        list.add(item);
    }
    
}
