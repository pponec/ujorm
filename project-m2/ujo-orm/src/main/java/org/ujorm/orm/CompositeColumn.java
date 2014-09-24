package org.ujorm.orm;

import java.util.List;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.extensions.PathProperty;

/**
 * CompositeColumn
 * @author Pavel Ponec
 */
public class CompositeColumn<UJO extends Ujo, VALUE> extends PathProperty<UJO, VALUE> {

    public CompositeColumn(List<Key> keys) {
            super(keys.toArray(new Key[keys.size()]), NO_ALIAS, keys.get(keys.size()-1).isAscending());
    }



}
