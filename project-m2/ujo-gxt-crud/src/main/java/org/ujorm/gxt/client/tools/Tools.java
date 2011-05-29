/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.tools;

/**
 *
 * @author Ponec
 */
final public class Tools {

    public static boolean isValid(CharSequence text) {
        return text!=null && text.length()>0;
    }

    public static String getCammelName(CharSequence name) {
        if (name==null) {
            return null;
        }
        if (name.length()==0) {
            return "";
        }
        String result =
            Character.toUpperCase(name.charAt(0)) +
            name.subSequence(1, name.length()).toString();
        
        return result;
    }

    /** Returns a short Class name to Display. */
    public static String getSimpleName(Class type) {
        String result = type.getName();
        result = result.substring(1 + result.lastIndexOf('.'));
        return result;
    }

}
