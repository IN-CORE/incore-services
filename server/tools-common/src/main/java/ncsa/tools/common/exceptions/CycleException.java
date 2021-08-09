/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.exceptions;

public class CycleException extends BaseCommonException {
    private static final long serialVersionUID = 2001L;

    public CycleException() {
        super();
    }

    public CycleException(String message, Throwable cause) {
        super(message, cause);
    }

    public CycleException(String message) {
        super(message);
    }

    public CycleException(Throwable cause) {
        super(cause);
    }
}
