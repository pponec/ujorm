/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.listener;

import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.registrar.*;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends RegistrarUjoExt<Person> {
    
    public static final Property<Person,Integer> ID  = newProperty("id", 0);
    public static final Property<Person,String> NAME = newProperty("name", "");
    public static final Property<Person,Double> CASH = newProperty("cash", 0.0);

}
