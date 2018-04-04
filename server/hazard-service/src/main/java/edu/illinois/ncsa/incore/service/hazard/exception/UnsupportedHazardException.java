/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.exception;

public class UnsupportedHazardException extends Exception {
    public UnsupportedHazardException() {

    }

    public UnsupportedHazardException(String message)
    {
        super(message);
    }

    public UnsupportedHazardException(Throwable cause) {
        super(cause);
    }

    public UnsupportedHazardException(String message, Throwable cause) {
        super(message, cause);
    }
}
