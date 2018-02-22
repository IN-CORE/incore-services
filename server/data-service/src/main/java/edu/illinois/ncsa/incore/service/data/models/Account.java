/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Chris Navarro (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.models;

/**
 * Created by ywkim on 9/26/2017.
 */

public class Account extends AbstractBean {
    /**
     * Used for serialization of object
     */
    private static final long serialVersionUID = 1L;

    /**
     * userid of the person, often email address.
     */
    private String userid = "";

    /**
     * password associated with the userid.
     */
    // TODO RK : password should really be encrypted
    private String password = "";

    private String token = "";

    /**
     * email of the person.
     */
    private Person person = null;

    private boolean active = false;

    private boolean admin = false;

    public Account() {
    }

    @Override
    public boolean equals(Object arg0) {
        return (arg0 instanceof Account) && this.toString().equals(arg0.toString());
    }

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return token for user account
     */
    public String getToken() {
        return token;
    }

    /**
     * Associate a token with a user account
     *
     * @param token token to associate with user account
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }

    /**
     * Check if user account is active
     *
     * @return true if account is enabled, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set the user account as active
     *
     * @param active - if true, user account is enabled, if false, user account is
     *               disabled
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Check if user account is an admin
     *
     * @return true if account is an administrative account, false otherwose
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * Set the administrative privileges of the account
     *
     * @param admin
     */
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

}
