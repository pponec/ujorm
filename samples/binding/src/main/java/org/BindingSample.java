/**
  * Copyright (C) 2008, Paval Ponec, contact: http://ujorm.org/
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation version 2 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You may obtain a copy of the License at
  * http://www.gnu.org/licenses/gpl-2.0.txt
  */

package org;

import java.util.HashMap;
import java.util.Map;
import org.impl.*;
import org.ujorm.Ujo;
import org.ujorm.Key;

/**
 * Example of a binding the UJO object to an UI component(s).
 * @author Pavel Ponec
 */
public class BindingSample {

    protected Map<Key, UIComponent> binding = new HashMap<Key, UIComponent>();

     /** Plain mapping any UIComponent to UJO Property. 
      * The type unsafe solution.
      */
    public void plainMapping() {
        binding.put(Person.FIRSTNAME, new UIComponentString());
        binding.put(Person.CASH, new UIComponentString()); // A type inconsistency!
    }

    /** Better solution where each Property must have the same type like the UI coponent. */
    public void userMapping() {
        bind(Person.FIRSTNAME, new UIComponentString());
        bind(Person.SURNAME, new UIComponentString());
        bind(Person.AGE, new UIComponentInt());
        bind(Person.MALE, new UIComponentBoolean());
        bind(Person.CASH, new UIComponentDouble());

        /* The compiler detects a type inconsistency: */
        //bind(Person.CASH, new UIComponentString());
    }

    /** Create new UICOmponent by a Property type and bind it */
    public void simpleMapping() {
        bind(Person.FIRSTNAME);
        bind(Person.SURNAME);
        bind(Person.AGE);
        bind(Person.MALE);
        bind(Person.CASH);
    }

    /** Create an edit form for all Properties of the Person. */
    public void defaultMapping(Person person) {
        bind(person);
    }

    /** How to set some attributes? */
    public void modifyComponentAttributes() {
        getComponent(Person.FIRSTNAME).setStyle("width", "100px");
        getComponent(Person.SURNAME).setStyle("width", "100px");
        getComponent(Person.CASH).setStyle("color", "red");
    }

    // === GENERIC METHODS TO MOVE TO A PARENT CLASS ===

    /** Load data to a GUI panel */
    @SuppressWarnings("unchecked")
	public void initForm(Ujo ujo) {
        for (Key p : binding.keySet()) {
            UIComponent component = binding.get(p);
            component.setValue(p.getValue(ujo));
        }
    }

    /** Load data from GUI panel to a Person instance. */
    @SuppressWarnings("unchecked")
	public void copyFormValues(Ujo ujo) {
        for (Key p : binding.keySet()) {
            UIComponent component = binding.get(p);
            if (component.isValid()) {
               p.setValue(ujo, component.getValue());
            } else {
                throw new IllegalStateException("Invalid input for: " + p);
            }
        }
    }

    /** Type safe <build>getComponent binding</build> */
    public <T> void bind(Key<?, T> property, UIComponent<T> component) {
        binding.put(property, component);
    }

    /** Auto-binding implementation */
    public void bind(Key property) {
        if (property.isTypeOf(String.class)) {
             binding.put(property, new UIComponentString());
        } else if (property.isTypeOf(Integer.class)) {
             binding.put(property, new UIComponentInt());
        } else if (property.isTypeOf(Boolean.class)) {
             binding.put(property, new UIComponentBoolean());
        } else if (property.isTypeOf(Double.class)) {
             binding.put(property, new UIComponentDouble());
        }
    }

    /** Create UICOmponents for all properties from the Person. */
    public void bind(Ujo ujo) {
        for (Key p : ujo.readKeys()) {
            bind(p);
        }
    }

    /** Type safe <build>getComponent binding</build> */
    @SuppressWarnings("unchecked")
	public <T> UIComponent<T> getComponent(Key<?, T> property) {
        return binding.get(property);
    }
}
