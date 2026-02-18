package org.ujorm.mapper.demo;

import org.ujorm.core.Key;
import org.ujorm.mapper.generated.City_;

/**
 * Example demonstration of a type-safe attribute container based on the Object array.
 *
 * <p>This implementation guarantees <b>type-safe write and read operations</b> at compile time.
 * By using {@link Key} as a descriptor, it ensures that the type of the value being stored
 * strictly matches the type of the value being retrieved, eliminating the need for
 * manual casting or runtime type checks.</p>
 *
 * <p>Unlike a standard {@code Map<String, Object>}, the association between a key and
 * its value type is statically defined and enforced.</p>
 *
 * @see Key
 */
public class CityUjo {

    public static final City_ meta = new City_();
    public static final Key<City, Long> keyId = meta.getKey("id", Long.class);
    public static final Key<City, String> keyName = meta.getKey("name", String.class);
    public static final Key<City, String> keyCountryCode = meta.getKey("countryCode", String.class);
    public static final Key<City, Double> keyLatitude = meta.getKey("latitude", Double.class);
    public static final Key<City, Double> keyLongitude = meta.getKey("longitude", Double.class);
    public final Object[] array = new Object[meta.count()];

    /** Access by a key index */
    public <V> void setValue(Key<City,V> key, V value) {
        array[key.getIndex()] = value;
    }

    /** Access by a key index */
    public <V> V getValue(Key<City,V> key) {
        return (V) array[key.getIndex()];
    }

    public City newInstance() {
        return meta.newDomain(array);
    }

}
