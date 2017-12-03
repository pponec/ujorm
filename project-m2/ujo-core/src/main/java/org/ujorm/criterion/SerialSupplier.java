package org.ujorm.criterion;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * The Serializable Supplier interface
 * @author Pavel Ponec
 * @see Criterion#where(org.ujorm.Key, org.ujorm.criterion.Operator, java.util.function.Supplier) 
 */
public interface SerialSupplier<T> extends Supplier<T>, Serializable {
    
}
