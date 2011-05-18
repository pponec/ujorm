/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010-2011 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Validation Exception
 * @author Ponec
 */
public class CMessageException extends RuntimeException implements Serializable {
    
    public static final int KEY_UNDEFINED = 0;
    public static final int KEY_SESSION_TIMEOUT = 1;
    public static final int KEY_COLUMN_IS_NOT_SORTABLE = 2;

    // ...... more constants .............

    /** Exception Key */
    private int key = 0;

    /** Exceptin Context */
    private HashMap<String, String> context = new HashMap<String, String>();

    public CMessageException() {
    }

    public CMessageException(int key, String message) {
        super(message);
        this.key = key;
    }

    public CMessageException(Throwable cause) {
        super(cause);
    }

    public CMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Set context property */
    public void set(String key, String value) {
        context.put(key, value);
    }

    /** Get Context */
    public HashMap<String, String> getContext() {
        return context;
    }

    /** Get Key */
    public int getKey() {
        return key;
    }

    /** Test if the exception is a sign to redirect. */
    public static boolean isSessionTimeout(Throwable exception) {
        final boolean result
                = exception instanceof CMessageException
                && ((CMessageException)exception).getKey()==KEY_SESSION_TIMEOUT
                ;
        return result;
    }


}
