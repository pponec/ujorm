/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujoframework.implementation.ujoExtension;

import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.map.MapProperty;
import org.ujoframework.implementation.map.MapUjoExt;
import org.ujoframework.extensions.PathProperty;

/**
 *
 * @author Pavel Ponec
 */
public class ExtPerson extends MapUjoExt<ExtPerson> {

    public static final UjoProperty<ExtPerson, String> NAME = newProperty("Name", String.class);
    public static final UjoProperty<ExtPerson, Boolean> MALE = newProperty("Male", false);
    public static final UjoProperty<ExtPerson, Double> CASH = newProperty("Cash", 0d);
    public static final UjoProperty<ExtPerson, ExtPerson> MOTHER = newProperty("Mother", ExtPerson.class);

    public void init() {
        set(NAME, "Jack");
        set(MOTHER, new ExtPerson());
        set(MOTHER, NAME, "Jane");
        set(MOTHER, CASH, 200d);

        String name = get(MOTHER, NAME);
        double cash = get(MOTHER, CASH);

        System.out.println(name + " " + cash);
    }

    public void init2() {
        set(NAME, "Jack").set(CASH, 50d);
        set(MOTHER, new ExtPerson());
        get(MOTHER).set(NAME, "Jackie").set(CASH, 10D);

        String name = get(MOTHER, NAME);
        double cash = get(MOTHER, CASH);

        System.out.println(name + " " + cash);
    }

    public void init3() {
        set(NAME, "Jack").set(CASH, 50D);
        set(MOTHER, new ExtPerson());
        set(MOTHER, MOTHER, new ExtPerson());
        set(MOTHER, MOTHER, CASH, 20D);
        set(MOTHER, MOTHER, MOTHER, new ExtPerson());
        get(MOTHER, MOTHER, MOTHER).set(NAME, "Jack").set(CASH, 10D);
        get(MOTHER, MOTHER, MOTHER).set(NAME, "Jack").set(CASH, 1.1);

        String name = get(PathProperty.newInstance(MOTHER, MOTHER, MOTHER, NAME));
        Double cash = get(PathProperty.newInstance(MOTHER, MOTHER, MOTHER, CASH));

        String name2 = get(MOTHER, MOTHER, NAME);
        Double cash2 = get(MOTHER, MOTHER, CASH);

        System.out.println(name + " " + cash);
    }
}
