package org.ujorm.core.criterion;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * The Serializable Supplier interface
 * @author Pavel Ponec
 * @see Criterion#where(org.ujorm.Key, org.ujorm.criterion.Operator, Supplier)
 * @since 1.76
 */
@FunctionalInterface
public interface ProxyValue<T> extends Supplier<T>, Serializable {

}
