/*******************************************************************************
 * Copyright (c) 2024 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Nathan Tolbert (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.project;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.log4j.Logger;


/**
 * This isn't something we really want to keep around forever...it should be handled better.
 * But this sitting here will force jetty to show error messages for routes that
 * aren't working right, instead of silently ignoring them.
 */

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger logger = Logger.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        logger.error(exception);
        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            return webEx.getResponse();
        }
        return Response.serverError().build();
    }
}
