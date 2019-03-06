/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

import edu.illinois.ncsa.incore.common.auth.Privileges;
import org.json.simple.JSONObject;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HistoricHurricane {

    @XmlID
    private String model;

    private Privileges privileges;

    private JSONObject parameters;

    public String getHurricaneModel() {
        return model;
    }

    public void setHurricaneModel(String model) {
        this.model = model;
    }

    public JSONObject getHurricaneParameters() {
        return parameters;
    }

    public void setHurricaneParameters(JSONObject parameters) {
        this.parameters = parameters;
    }

    public Privileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Privileges privileges) {
        this.privileges = privileges;
    }
}
