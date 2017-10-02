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
