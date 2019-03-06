/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Nathan Tolbert
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * I'm not sure this is the "best" solution going forward, but until we spend some time
 * further on it, this allows for different sets of configuration of incore.
 * <p>
 * By passing a java property -Dincore.properties, the system can specify a
 * specific java.properties file with incore configuration.
 * <p>
 * Otherwise, the default incore-develop.properties will be used
 */
public class Config {

    private static final Logger log = Logger.getLogger(Config.class);


    private static Properties defaultConfigProperties;

    public static Properties getConfigProperties() {
        Properties prop = new Properties();
        String configFile = System.getProperty("incore.properties");

        if (configFile == null) {
            return getDefaultConfigProperties();
        }

        try (InputStream in = new FileInputStream(configFile)) {
            prop.load(in);
        } catch (IOException e) {
            log.error("Could not read props file " + configFile + " specified in environment variable INCORE_CONFIG_FILE", e);
        }
        return prop;

    }

    public static Properties getDefaultConfigProperties() {
        String resourceName = "incore-develop.properties"; // could also be a constant
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Properties props = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            props.load(resourceStream);
        } catch (IOException e) {
            log.error("Could not read default incore-develop.properties", e);
        }
        return props;
    }
}
