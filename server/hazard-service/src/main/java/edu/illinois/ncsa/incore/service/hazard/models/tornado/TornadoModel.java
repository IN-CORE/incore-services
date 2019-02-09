/*******************************************************************************
 * Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the BSD-3-Clause which accompanies this distribution,
 * and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import edu.illinois.ncsa.incore.common.auth.Privileges;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.EFBox;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.util.List;

public class TornadoModel extends Tornado {
    private String tornadoModel;

    private TornadoParameters tornadoParameters;
    private List<Double> tornadoWidth;
    private List<EFBox> efBoxes;
    private String datasetId;

    public List<Double> getTornadoWidth() {
        return tornadoWidth;
    }

    public void setTornadoWidth(List<Double> tornadoWidth) {
        this.tornadoWidth = tornadoWidth;
    }


    public List<EFBox> getEfBoxes() {
        return efBoxes;
    }

    public void setEfBoxes(List<EFBox> efBoxes) {
        this.efBoxes = efBoxes;
    }

    public TornadoParameters getTornadoParameters() {
        return tornadoParameters;
    }

    public void setTornadoParameters(TornadoParameters tornadoParameters) {
        this.tornadoParameters = tornadoParameters;
    }

    public String getTornadoModel() {
        return tornadoModel;
    }

    public void setTornadoModel(String tornadoModel) {
        this.tornadoModel = tornadoModel;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}
