package org.ujorm2.core;

/**
 * This context provides instances of the meta model including direct keys
 * @author Pavel Ponec
 */
public abstract class UjoContext {

    private MetaDomainStore store;

    public void save(MetaDomainStore store) {
        this.store = store;
        store.close();
    }

    public MetaDomainStore getStore$() {
        return store;
    }

    public void close() {

    }
}
