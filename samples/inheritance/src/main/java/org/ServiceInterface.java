/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org;

import org.bo.User;

/**
 *
 * @author pavel
 */
public class ServiceInterface {

    public void makeUser(User user) {
        // ...
    }

    public static ServiceInterface newInstance() {
        return new ServiceInterface();
    }

}
