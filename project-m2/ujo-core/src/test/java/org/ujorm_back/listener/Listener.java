/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm_back.listener;

import java.util.ArrayList;
import org.ujorm.listener.UjoPropertyChangeEvent;
import org.ujorm.listener.UjoPropertyChangeEvent;
import org.ujorm.listener.UjoPropertyChangeListener;
import org.ujorm.listener.UjoPropertyChangeListener;

/**
 * Listener
 * @author Pavel Ponec
 */
public class Listener implements UjoPropertyChangeListener {
    
    private ArrayList<UjoPropertyChangeEvent> list = new ArrayList<UjoPropertyChangeEvent>();

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
