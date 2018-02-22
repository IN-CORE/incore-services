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
 * This is from NCSA's DataWolf
 */

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Person extends AbstractBean {
    /**
     * Used for serialization of object
     */
    private static final long serialVersionUID = 1L;

    /**
     * First Name of the person.
     */
    private String firstName = "";

    /**
     * First Name of the person.
     */
    private String lastName = "";

    /**
     * email of the person.
     */
    private String email = "";

    /**
     * Creates a person given first name, last name and email. This will result
     * in
     * a new bean generated, if a person exists with the given email it will
     * generate two instances of this person. Best is to make sure the DAO is
     * called first to see if a person with that email exists, if so use that
     * person object.
     *
     * @param firstName first name of the person.
     * @param lastName  last name of the person.
     * @param email     email address of the person.
     * @return a newly generated person object.
     */
    public static Person createPerson(String firstName, String lastName, String email) {
        Person p = new Person();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        p.setEmail(email);

        return p;
    }

    public Person() {
    }

    /**
     * Single function to set both first and last name of the person.
     *
     * @param firstName the first name of the person.
     * @param lastName  the last name of the person.
     */
    @JsonIgnore
    public void setName(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    /**
     * This will return the formatted name. The name will be lastname,
     * firstname.
     *
     * @return Returns a formatted lastname, firstname.
     */
    @JsonIgnore
    public String getName() {
        if ((lastName != null) && !lastName.equals("")) {
            if ((firstName != null) && !firstName.equals("")) {
                return lastName + ", " + firstName;
            } else {
                return lastName;
            }
        }
        if (firstName != null) {
            return firstName;
        }
        return "N/A";
    }

    /**
     * Sets the first name of the person.
     *
     * @param firstName the first name of the person
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the first name of the person.
     *
     * @return the first name of the person.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the last name of the person.
     *
     * @param lastName the last name of the person
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the last name of the person.
     *
     * @return the last name of the person.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the email of the person.
     *
     * @param email the email of the person
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the email of the person.
     *
     * @return the email of the person.
     */
    public String getEmail() {
        return email;
    }

    public String toString() {
        return firstName + " " + lastName + "<" + email + ">";
    }

    @Override
    public boolean equals(Object arg0) {
        return (arg0 instanceof Person) && this.toString().equals(arg0.toString());
    }
}
