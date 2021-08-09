/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.exceptions;

public class AuthenticationException extends Exception {
    private final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    private static final long serialVersionUID = 2030L;

    private String actualMessage;

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
        setActualMessage(arg0);
    }

    public AuthenticationException(String arg0) {
        super(arg0);
        setActualMessage(arg0);
    }

    public AuthenticationException(Throwable arg0) {
        super(arg0);
    }

    public void setActualMessage(String message) {
        logger.debug("Setting message to: " + message);

        this.actualMessage = message;
    }

    public String getActualMessage() {
        return actualMessage;
    }
}
