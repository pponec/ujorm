/*
 *  Copyright 2007-2010 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm;

import org.ujorm.core.UjoActionImpl;

/**
 * An action constants of Ujorm.
 * @author Pavel Ponec
 * @see Ujo
 */
public interface UjoAction {

    /** This is an undefined action.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_UNDEFINED = 0;
    /** An authorization action (of a UjoProperty) for a XML export.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_XML_EXPORT = 2;
    /** An authorization action (of a UjoProperty) for a XML import.
     * <br>Note: the authoriazation is not implemented yet.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_XML_IMPORT = 3;
    /** Some properties can be exported (or imported) to (from) XML file like an attribute. If readAuthorization(...) method returns value FALSE,
     * then the property is <b>attribute</b>, otherwise TRUE value means <b>element</b>.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    @Deprecated
    public static final int ACTION_XML_ELEMENT = 4;
    /** An authorization action (of a UjoProperty) for a Resource Bundle export.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_RESBUNDLE_EXPORT = 10;
    /** An authorization action (of a UjoProperty) for a Resource Bundle import.
     * <br>Note: the authoriazation is not implemented yet.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_RESBUNDLE_IMPORT = 11;
    /** An authorization action (of a UjoProperty) for a CSV import.
     * <br>Note: the authoriazation is not implemented yet.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_CSV_IMPORT = 12;
    /** An authorization action (of a UjoProperty) for a CSV export.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_CSV_EXPORT = 13;
    /**
     * An authorization action (of a UjoProperty) for a method: UjoManager.clone(Ujo ujo, int depth) .
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_CLONE = 20;
    /**
     * An authorization action (of a UjoProperty) for a method: UjoManager.copy(Ujo source, Ujo target, UjoProperty... properties) .
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_COPY = 21;
    /**
     * An authorization action (of a UjoProperty) for a method: toString()
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_TO_STRING = 22;
    /** An authorization action (of a UjoProperty) for a Zero provider.
     * The constant enable/disable a Zero Provide management.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     * @deprecated use the UjoProperty.getDefault() value instead of.
     */
    public static final int ACTION_ZERO_REPLACE = 30;
    /** An authorization action for class UjoContentTabModel. Can by displayed the property in a JTable like a row?.
     * @see Ujo#readAuthorization(UjoAction, UjoProperty, Object)
     */
    public static final int ACTION_TABLE_SHOW = 100;
    
    
    // ========= DUMMY ACTION =========
    
    /** A dummy action have got an ACTION_UNDEFINED id and the null context. */
    public static final UjoAction DUMMY = new UjoActionImpl(null);
    
    // ========= METHODS =========
    
    /** Returns a type of the action. The default type is ACTION_UNDEFINED. 
     * <ul>
     * <li>Numbers are reserved in range (from 0 to 999 include) for an internal usage of the Ujorm</li>
     * <li>Zero is an undefined action</li>
     * <li>Negative values are free for general usage too</li>
     * </ul>
     * <br>The number can be useful for a resolution of an action for a different purpose (e.g. export to 2 different XML files).
     */
    public int getType();
    
    /** Returns a conetxt of the action. The value is dedicated to a user usage and the value can be null. */
    public Object getContext();
    
}
