/*******************************************************************************
 * Copyright (c) 2020 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard;

public class Job {
    private String service;
    private String objectId;
    private String executionId;
    private String eqJson;
    private String username;

    // 0 - needs to be submitted, 1 - running, 2 - done/failed
    private int state = 0;

    public Job() {

    }

    public Job(String username, String service, String objectId, String eqJson) {
        this.username = username;
        this.service = service;
        this.objectId = objectId;
        this.eqJson = eqJson;
    }

    public String getService() {
        return this.service;
    }

    public String getObjectId() {
        return this.objectId;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getExecutionId() {
        return this.executionId;
    }

    public void setEqJson(String eqJson) {
        this.eqJson = eqJson;
    }

    public String getEqJson() {
        return this.eqJson;
    }

    public String getUsername() {
        return this.username;
    }


}
