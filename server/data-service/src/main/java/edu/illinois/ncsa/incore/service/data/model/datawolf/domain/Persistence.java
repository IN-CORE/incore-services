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

package edu.illinois.ncsa.incore.service.data.model.datawolf.domain;

/**
 * Created by ywkim on 9/26/2017.
 */

import com.google.inject.Injector;

/**
 * Provides an instance of the injector where needed. Most objects should get
 * what they need via injection; however in cases where new instances of objects
 * are required at runtime, this class can provide that.
 *
 * @author Chris Navarro <cmnavarr@illinois.edu>
 */
public class Persistence {
    private static Injector injector;

    public static <T> T getBean(Class<T> requiredType) {
        return injector.getInstance(requiredType);
    }

    public static void setInjector(Injector injector) {
        Persistence.injector = injector;
    }
}
