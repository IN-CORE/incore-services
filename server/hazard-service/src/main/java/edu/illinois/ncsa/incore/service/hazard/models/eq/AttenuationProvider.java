/*******************************************************************************
 * Copyright (c) 2018 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.eq;

import edu.illinois.ncsa.incore.service.hazard.models.eq.attenuations.BaseAttenuation;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Provides Attenuation models available in the earthquake hazard service
 */
public class AttenuationProvider {
    private static final Logger log = Logger.getLogger(AttenuationProvider.class);

    private Map<String, BaseAttenuation> attenuations = new HashMap<String, BaseAttenuation>();
    private static AttenuationProvider instance;

    private AttenuationProvider() {
        // Property file with list of attenuation models to load
        String resourceName = "hazard-model.properties";
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            props.load(resourceStream);
            Enumeration<String> enumeration = (Enumeration<String>) props.propertyNames();
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                // Only load earthquake models
                if (key.startsWith("earthquake.attenuation")) {
                    String className = props.getProperty(key).trim();
                    BaseAttenuation attenuation = loadAttenuation(className);
                    if (attenuation == null) {
                        log.warn("Could not load attenuation with " + key + ", check properties file.");
                    } else {
                        attenuations.put(attenuation.getClass().getSimpleName(), attenuation);
                        log.debug("loaded attenuation key " + className);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Could not read hazard-model.properties", e);
        }
    }

    public static AttenuationProvider getInstance() {
        if (instance == null) {
            instance = new AttenuationProvider();
        }

        return instance;
    }

    /**
     * Helper method to populate an attenuation Map with actual models and their weights for a given earthquake model
     *
     * @param scenarioEarthquake
     * @return Map of attenuation models and weights
     */
    public Map<BaseAttenuation, Double> getAttenuations(EarthquakeModel scenarioEarthquake) {
        Map<BaseAttenuation, Double> attenuations = new HashMap<BaseAttenuation, Double>();
        Iterator<String> modelIterator = scenarioEarthquake.getAttenuations().keySet().iterator();
        while (modelIterator.hasNext()) {
            String model = modelIterator.next();
            double weight = scenarioEarthquake.getAttenuations().get(model);

            BaseAttenuation attenuation = AttenuationProvider.getInstance().getAttenuation(model);
            attenuation.setRuptureParameters(scenarioEarthquake.getEqParameters());

            attenuations.put(attenuation, weight);
        }

        return attenuations;
    }

    /**
     * Map of all available attenuation models (key - class simple name, value is actual model)
     *
     * @return Map of attenuation models
     */
    public Map<String, BaseAttenuation> getAttenuations() {
        return attenuations;
    }

    /**
     * Attenuation model referenced by model.
     *
     * @param model - simple class name of attenuation model being requested
     * @return Attenuation model if loaded by the provider, otherwise null
     */
    public BaseAttenuation getAttenuation(String model) {
        if (attenuations.containsKey(model)) {
            return attenuations.get(model);
        }

        return null;
    }

    private BaseAttenuation loadAttenuation(String className) {
        BaseAttenuation attenuation = null;
        try {
            attenuation = (BaseAttenuation) Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            log.error("Could not create attenuation model " + className);
        } catch (IllegalAccessException e) {
            log.error("Could no access attenuation model " + className);
        } catch (ClassNotFoundException e) {
            log.error(("Could not find attenuation model " + className));
        }

        return attenuation;
    }
}
