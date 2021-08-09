/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.exceptions;

import java.io.Serializable;

public class BaseCommonException extends Exception implements Serializable {
    private static final long serialVersionUID = 1031L;

    public BaseCommonException() {
        super();
    }

    public BaseCommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseCommonException(String message) {
        super(message);
    }

    public BaseCommonException(Throwable cause) {
        super(cause);
    }
}
