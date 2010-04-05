/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.bo;

/** The Customer interface */
public interface ICustomer extends IUser {

    public String getCompany();

    public void setCompany(String company);

    public int getDiscount();

    public void setDiscount(int discount);


}