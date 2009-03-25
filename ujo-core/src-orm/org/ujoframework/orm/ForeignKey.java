/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm;

/**
 * A foreign key of a table
 * @author pavel
 */
public class ForeignKey {

    private final Object value;

    public ForeignKey(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value) + '\u00b4';
    }

}
