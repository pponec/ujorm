package org.ujorm2.core.lambda;

/**
 *
 * @author Pavel Ponec
 */
@FunctionalInterface
public interface Setter<D,V> {

    public void set(D domain, V value);

}
