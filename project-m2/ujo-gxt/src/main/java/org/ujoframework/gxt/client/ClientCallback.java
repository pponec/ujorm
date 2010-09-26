/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;

import com.google.gwt.user.client.Window;
import com.gwtincubator.security.client.SecuredAsyncCallback;
import com.gwtincubator.security.exception.ApplicationSecurityException;

/**
 * ClientCallback
 * @author Tomas Hampl
 */
public abstract class ClientCallback<T> extends SecuredAsyncCallback<T> {

    private boolean callEnd = false;

    @Override
    protected void onSecurityException(ApplicationSecurityException exception) {
        Window.alert("security exeption :" + exception.toString());
        callEnd = true;
    }

    @Override
    protected void onOtherException(Throwable exception) {
        Window.alert("other exeption :" + exception.toString());
        callEnd = true;
    }

    public Boolean isCallEnd() {
        return callEnd;
    }
    public void setCallEnd(boolean b) {
        callEnd = b;
    }

}
