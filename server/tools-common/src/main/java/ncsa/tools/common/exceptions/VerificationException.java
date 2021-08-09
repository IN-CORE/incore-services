/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.exceptions;

public class VerificationException extends BaseCommonException {
    private static final long serialVersionUID = 2023L;

    public VerificationException() {
        super();
    }

    public VerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerificationException(String message) {
        super(message);
    }

    public VerificationException(Throwable cause) {
        super(cause);
    }
}
