/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ujorm.UjoCodeGenerator.templates;

import java.util.Date;

/**
 *
 * @author ponec
 */
public class Template {
    
    /** Object identifier */
    public static final Key<Template, Long> ID = newKey();
    /** Name of the object */
    public static final Key<Template, String> NAME = newKey();
    /** Birthday of the */
    public static final Key<Template, Date> BIRTH_DAY = newKey();

    protected static <UJO,VALUE> Key<UJO, VALUE> newKey() {
        return new Key<UJO,VALUE>();
    }
    
    // ---------- GENERATED METHODS ------------------

}
