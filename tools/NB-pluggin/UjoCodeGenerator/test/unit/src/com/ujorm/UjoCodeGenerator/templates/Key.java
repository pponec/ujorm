/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ujorm.UjoCodeGenerator.templates;

/**
 * KeyTemplate
 * @author ponec
 */
public class Key<UJO,VALUE>  {
    
    private VALUE value;

    public void setValue(UJO ujo, VALUE value) {
        this.value = value;
    }
    
    public VALUE of(UJO ujo) {
        return value;        
    }
    
}
