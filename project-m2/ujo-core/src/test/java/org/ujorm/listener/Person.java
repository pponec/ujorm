/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm.listener;

import org.ujorm.Key;
import org.ujorm.implementation.registrar.*;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends RegistrarUjoExt<Person> {
    
    public static final Key<Person,Integer> ID  = newProperty("id", 0);
    public static final Key<Person,String> NAME = newProperty("name", "");
    public static final Key<Person,Double> CASH = newProperty("cash", 0.0);

}
