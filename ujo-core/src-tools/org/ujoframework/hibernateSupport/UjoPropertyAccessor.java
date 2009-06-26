/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujoframework.hibernateSupport;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;

/**
 *
 * @author meatfly
 */
public class UjoPropertyAccessor implements PropertyAccessor {

    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {

        return new UjoPropertyGetter(propertyName,theClass);

    }

    public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new UjoPropertySetter(propertyName);
    }
}
