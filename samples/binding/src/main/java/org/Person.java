/**
  * Copyright (C) 2008, Paval Ponec, contact: http://ujoframework.org/
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
import org.ujoframework.*;

/** Person BO */
public class Person extends org.ujoframework.implementation.map.MapUjoExt<Person> {

  public static final UjoProperty<Person,Integer> ID        = newProperty("ID", Integer.class);
  public static final UjoProperty<Person,String>  FIRSTNAME = newProperty("FirstName", String.class);
  public static final UjoProperty<Person,String>  SURNAME   = newProperty("Surname", String.class);
  public static final UjoProperty<Person,Integer> AGE       = newProperty("Age" , Integer.class);
  public static final UjoProperty<Person,Boolean> MALE      = newProperty("Male", Boolean.class);
  public static final UjoProperty<Person,Double>  CASH      = newProperty("Cash", 0d);

  public void addCash(double cash) {
    double newCash = get(CASH) + cash;
    set(CASH, newCash);
  }
}