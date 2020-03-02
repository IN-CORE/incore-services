/* *****************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.common.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Runtime exception for applications.
 * <p>
 * This class supports throwing an exception
 * with an explanation of the error situation,
 * and whether it is a temporary or permanent
 * condition. As specified in HTTP/1.1
 * https://tools.ietf.org/html/rfc7231#section-6.5
 *
 */
public class IncoreHTTPException extends WebApplicationException {
    /**
     * Construct a new instance with the supplied message and HTTP status.
     *
     * @param message the detail message
     * @param status  the HTTP status code that will be returned to the client.
     * @throws IllegalArgumentException if status is {@code null}.
     */
    public IncoreHTTPException(Response.Status status, String message) {
        super(Response.status(status)
            .entity(message).type(MediaType.TEXT_PLAIN).build());
    }

    /**
     * Construct a new instance with the supplied HTTP status.
     *
     * @param status  the HTTP status code that will be returned to the client.
     * @throws IllegalArgumentException if status is {@code null}.
     */
    public IncoreHTTPException(Response.Status status) {
        super(Response.status(status).build());
    }

}


