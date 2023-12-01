/*******************************************************************************
 * Copyright (c) 2022 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.annotations.Embedded;
import org.apache.log4j.Logger;

@Embedded
public class UserLimits extends UserUsages{
    private static final Logger log = Logger.getLogger(UserUsages.class);

    public UserLimits() { }

    public static UserLimits fromJson(String limitJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(limitJson, UserLimits.class);
        } catch (Exception e) {
            log.error("Could not parse usage JSON. Returning Usage with zero values", e);
            return new UserLimits();
        }
    }
}
