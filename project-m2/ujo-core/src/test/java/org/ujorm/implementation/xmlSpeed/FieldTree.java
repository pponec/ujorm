/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xmlSpeed;

import java.util.List;
import java.util.Date;
import java.util.List;
import org.ujorm.extensions.ValueAgent;
import org.ujorm.implementation.field.FieldPropertyList;
import org.ujorm.implementation.field.FieldUjoImplChild;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class FieldTree extends FieldUjoImplChild {
    
    private List<FieldTree> children;
    
    /** (List) */
    public static final FieldPropertyList<FieldTree,FieldTree> PRO_CHILDREN = newListKey("CHILDREN", new ValueAgent<FieldTree,List<FieldTree>>() {
        public void writeValue(FieldTree ujo, List<FieldTree> value) {
            ujo.children = value; 
        }
        public List<FieldTree> readValue(FieldTree ujo) {
            return ujo.children; 
        }
    });
    
    public void setChilds(List<FieldTree> children) {
        this.children = children;
    }
    public List<FieldTree> getChilds() {
        return children;
    }

    public List<FieldTree> addChild(FieldTree child) {
        getChilds().add(child);
        return getChilds();
    }    
    
    
    public int size() {
        int result = 0;
        for (FieldTree tree : PRO_CHILDREN.getList(this)) {
            result += tree.size() + 1;
        }
        return result;
    }
    
    
    // * * * * * * * * * * *
    
    public void init(ZCounter counter, int deep) {
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        
        PRO_P0.setValue(this, o0);
        PRO_P1.setValue(this, o1);
        PRO_P2.setValue(this, o2);
        PRO_P3.setValue(this, o3);
        PRO_P4.setValue(this, o4);
        PRO_P5.setValue(this, o0);
        PRO_P6.setValue(this, o1);
        PRO_P7.setValue(this, o2);
        PRO_P8.setValue(this, o3);
        PRO_P9.setValue(this, o4);
        
        for (int i=0; i<10; i++) {
            if (deep<=0 || counter.substract()){
                return;
            }
            FieldTree item = new FieldTree();
            item.init(counter, deep-1);
            PRO_CHILDREN.addItem(this, item);
        }
        
    }
    
    
}


