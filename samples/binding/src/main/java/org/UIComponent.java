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

/**
 *
 * @author Ponec
 */
public interface UIComponent<T> {

    /** Get component value */
    T getValue();

    /** Get component value */
    void setValue(T value);

    /** Set HTML stype */
    void setStyle(String attr, String value);

    /** Is the input value valid? */
    boolean isValid();

}
