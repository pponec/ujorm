/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujoframework.implementation.criterion;

import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.map.MapUjoExt;
import org.ujoframework.extensions.PathProperty;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends MapUjoExt<Person> {

    public static final UjoProperty<Person, String> NAME = newProperty("Name", String.class);
    public static final UjoProperty<Person, Boolean> MALE = newProperty("Male", false);
    public static final UjoProperty<Person, Double> CASH = newProperty("Cash", 0d);
    public static final UjoProperty<Person, Person> MOTHER = newProperty("Mother", Person.class);

    public void init() {
        set(NAME, "Jack");
        set(MOTHER, new Person());
        set(MOTHER, NAME, "Jane");
        set(MOTHER, CASH, 200d);

        String name = get(MOTHER, NAME);
        double cash = get(MOTHER, CASH);

        System.out.println(name + " " + cash);
    }

    public void init2() {
        set(NAME, "Jack").set(CASH, 50d);
        set(MOTHER, new Person());
        get(MOTHER).set(NAME, "Jackie").set(CASH, 10D);

        String name = get(MOTHER, NAME);
        double cash = get(MOTHER, CASH);

        System.out.println(name + " " + cash);
    }

    public void init3() {
        set(NAME, "Jack").set(CASH, 50D);
        set(MOTHER, new Person());
        set(MOTHER, MOTHER, new Person());
        set(MOTHER, MOTHER, CASH, 20D);
        set(MOTHER, MOTHER, MOTHER, new Person());
        get(MOTHER, MOTHER, MOTHER).set(NAME, "Jack").set(CASH, 10D);
        get(MOTHER, MOTHER, MOTHER).set(NAME, "Jack").set(CASH, 1.1);

        String name = get(PathProperty.newInstance(MOTHER, MOTHER, MOTHER, NAME));
        Double cash = get(PathProperty.newInstance(MOTHER, MOTHER, MOTHER, CASH));

        String name2 = get(MOTHER, MOTHER, NAME);
        Double cash2 = get(MOTHER, MOTHER, CASH);

        System.out.println(name + " " + cash);
    }
}
