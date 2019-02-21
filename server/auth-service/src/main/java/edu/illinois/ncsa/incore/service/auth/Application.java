/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Indira Gutierrez (NCSA) - initial API and implementation
 * Nathan Tolbert (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.auth;

import edu.illinois.ncsa.incore.service.auth.CorsFilter;
import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {
    private static final Logger log = Logger.getLogger(Application.class);

    public Application() {
        super.register(new CorsFilter());
    }
}
