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
public class UjoStub {

    /** Object identifier */
    public static final Key<UjoStub, Long> ID = newKey();
    /** Name of the UjoStub instance */
    public static final Key<UjoStub, String> NAME = newKey();
    /** Birthday
     * of the STUB
     */
    public static final Key<UjoStub, Date> BIRTH_DAY = newKey();

    protected static <UJO,VALUE> Key<UJO, VALUE> newKey() {
        return new Key<UJO,VALUE>();
    }

    // ---------- GENERATED METHODS ------------------



}
