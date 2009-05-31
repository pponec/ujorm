/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.listener;

import org.ujoframework.implementation.registrar.*;
import org.ujoframework.UjoProperty;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends RegistrarUjoExt<Person> {
    
    public static final UjoProperty<Person,Integer> ID  = newProperty("id", 0);
    public static final UjoProperty<Person,String> NAME = newProperty("name", "");
    public static final UjoProperty<Person,Double> CASH = newProperty("cash", 0.0);

}
