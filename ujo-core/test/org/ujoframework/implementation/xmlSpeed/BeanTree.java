/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xmlSpeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.ujoframework.implementation.bean.*;

/**
 * An UnifiedDataObject Imlpementation
 * @author pavel
 */
public class BeanTree extends BeanUjoImplChild {
    
    public static final BeanPropertyList<BeanTree, BeanTree> PRO_CHILDS = newPropertyList("Child", BeanTree.class);
    
    
    private ArrayList<PojoTree> childs;
    
    public void setChilds(ArrayList<PojoTree> childs) {
        this.childs = childs;
    }
    public ArrayList<PojoTree> getChilds() {
        return childs;
    }

    public List<PojoTree> addChild(PojoTree child) {
        getChilds().add(child);
        return getChilds();
    }    
    
    
    
    public int size() {
        int result = 0;
        for (BeanTree tree : PRO_CHILDS.getList(this)) {
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
            BeanTree item = new BeanTree();
            item.init(counter, deep-1);
            PRO_CHILDS.addItem(this, item);
        }
        
    }
    
    
}


