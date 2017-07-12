package org.ujorm.extensions;

/**
 *
 * @author Pavel Ponec
 */
public class ProxyKey<U extends Ujo,V> extends Property<U,V> {

    public ProxyKey(Key index) {
        super(index);
    }

}
