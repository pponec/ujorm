/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.inheritance.sample;

import org.ujoframework.orm.inheritance.sample.bo.User;

/**
 *
 * @author Pavel Ponec
 */
public class ServiceInterface {

    public void makeUser(User user) {
        // ...
    }

    public static ServiceInterface newInstance() {
        return new ServiceInterface();
    }

}
