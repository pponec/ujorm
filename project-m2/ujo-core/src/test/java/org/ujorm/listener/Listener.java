/*
 *  Copyright 2007-2014 Pavel Ponec
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

package org.ujorm.listener;

import java.util.ArrayList;

/**
 * Listener
 * @author Pavel Ponec
 */
public class Listener implements UjoPropertyChangeListener {
    
    private final ArrayList<UjoPropertyChangeEvent> list = new ArrayList<>();

    @Override
    public void propertyChange(UjoPropertyChangeEvent evt) {
        list.add(evt);
    }

    public Object getOldValue(int i) {
        return list.get(i).getOldValue();
    }
    
    public Object getNewValue(int i) {
        return list.get(i).getNewValue();
    }
    
    public Object getLastOldValue() {
        return list.get(list.size()-1).getOldValue();
    }

    public Object getLastNewValue() {
        return list.get(list.size()-1).getNewValue();
    }

    public Object getLast2OldValue() {
        return list.get(list.size()-2).getOldValue();
    }
    
    
    public Object getLast2NewValue() {
        return list.get(list.size()-2).getNewValue();
    }
    
    
    public int size() {
        return list.size();
    }
    
}
