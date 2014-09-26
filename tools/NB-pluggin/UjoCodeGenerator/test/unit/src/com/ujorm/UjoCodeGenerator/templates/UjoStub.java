/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ujorm.UjoCodeGenerator.templates;

import java.util.Date;

/**
 * Stub of the UJO object for tests.
 * @author Pavel Ponec
 */
public class UjoStub {

    /** Object identifier */
    public static final Key<UjoStub, Long> ID = newKey();
    /** Name of the UjoStub instance */
    public static final Key<UjoStub, String> NAME = newKey();
    /** Birthday
     * of the STUB
     * where the <strong>key name</strong> have got a camelCase
     */
    public static final Key<UjoStub, Date> BIRTH_DAY = newKey();

    // ---------- COMPOSITE KEYS -----

    /** Name of the UjoStub instance
     * where the <strong>key name</strong> have got a camelCase */
    public static final Key<UjoStub, String> compositeKey = newKey();

    protected static <UJO,VALUE> Key<UJO, VALUE> newKey() {
        return new Key<UJO,VALUE>();
    }

    // ---------- GENERATED METHODS ------------------

    

}
