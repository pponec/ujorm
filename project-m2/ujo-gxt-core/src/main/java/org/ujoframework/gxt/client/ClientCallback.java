/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.gwtincubator.security.client.SecuredAsyncCallback;
import com.gwtincubator.security.exception.ApplicationSecurityException;

/**
 * ClientCallback
 * @author Tomas Hampl, Pavel Ponec
 */
public abstract class ClientCallback<T> extends SecuredAsyncCallback<T> {

    private boolean callEnd = false;
    final private CLoginRedirectable loginRedirectable;

    public ClientCallback(CLoginRedirectable loginRedirectable) {
        this.loginRedirectable = loginRedirectable;
    }

    public ClientCallback() {
        this(null);
    }
    
    @Override
    protected void onSecurityException(ApplicationSecurityException exception) {
        String msg = getSimpleName(exception) + ": " + exception.getMessage();
        GWT.log(msg, exception);
        Window.alert(msg);
        callEnd = true;
    }

    @Override
    final protected void onOtherException(Throwable exception) {
        if (exception instanceof CMessageException 
        && ((CMessageException)exception).getKey()==CMessageException.KEY_LOGIN_TIMEOUT) {
            onTimeout(exception);
        } else {
            onAnotherException(exception);
        }
    }

    protected void onAnotherException(Throwable exception) {
        String msg = getSimpleName(exception) + ": " + exception.getMessage();
        GWT.log(msg, exception);
        Window.alert(msg);
        callEnd = true;
    }

    protected void onTimeout(Throwable exception) {
        if (loginRedirectable!=null) {
            loginRedirectable.redirectToLogin();
        } else {
            GWT.log("Timeout", exception);
        }
    }

    private String getSimpleName(Throwable exception) {
        String className = exception.getClass().getName();
        int i = className.indexOf('.');
        if (i>0) {
            return className.substring(0, i);
        } else {
            return className;
        }
    }

    public Boolean isCallEnd() {
        return callEnd;
    }
    public void setCallEnd(boolean b) {
        callEnd = b;
    }

}
