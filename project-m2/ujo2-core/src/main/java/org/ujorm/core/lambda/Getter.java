package org.ujorm.core.lambda;

/**
 * @see java.util.function.Function
 * @author Pavel Ponec
 */
@FunctionalInterface
public interface Getter<D,V> {

    public V get(D domain);

}
