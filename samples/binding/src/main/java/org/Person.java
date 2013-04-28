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
import org.ujorm.*;

/** Person BO. */
public class Person extends org.ujorm.implementation.map.MapUjoExt<Person> {

  public static final Key<Person,Integer> ID        = newKey("ID");
  public static final Key<Person,String>  FIRSTNAME = newKey("FirstName");
  public static final Key<Person,String>  SURNAME   = newKey("Surname");
  public static final Key<Person,Integer> AGE       = newKey("Age");
  public static final Key<Person,Boolean> MALE      = newKey("Male");
  public static final Key<Person,Double>  CASH      = newKey("Cash", 0d);

  public void addCash(double cash) {
    double newCash = get(CASH) + cash;
    set(CASH, newCash);
  }
}