package org.version2.bo;

/**
 * Account
 * @author Pavel Ponec
 */
public class Account {

    /** ID */
    private Integer id;
    /** Login */
    private String login;
    /** Password */
    private byte[] password;
    /** Account is enabled */
    private Boolean enabled;

    /**
     * ID
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * ID
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Login
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Login
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Password
     * @return the password
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * Password
     * @param password the password to set
     */
    public void setPassword(byte[] password) {
        this.password = password;
    }

    /**
     * Account is enabled
     * @return the enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Account is enabled
     * @param enabled the enabled to set
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }


}
