/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ujorm.UjoCodeGenerator.bo;

import com.sun.source.tree.VariableTree;

/**
 * The Ujo Key Description
 * @author Pavel Ponec
 */
public class KeyItem {
    
    /** Name of the Key */
    private final String keyName;
    /** JavaDoc text */
    private final String javaDoc;
    /** Variable Tree */
    private final VariableTree variableTree;

    public KeyItem(VariableTree var, String javaDoc) {
        assert var!=null : "The variableTree must not be null";
        
        this.variableTree = var;
        this.keyName = var.getName().toString();
        this.javaDoc = javaDoc;
    }

    /** Name of the Key */
    public String getKeyName() {
        return keyName;
    }

    /** JavaDoc text */
    public String getJavaDoc() {
        return javaDoc;
    }
    
    public VariableTree getVariableTree() {
        return variableTree;
    }

    @Override
    public String toString() {
        return keyName 
             + (javaDoc!=null && javaDoc.length()>0 
             ? " - " + javaDoc 
             : "");
    }
    
    
    
}
