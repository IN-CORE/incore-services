/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 * Contributors:
 * Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.hazard.models.tornado;

import dev.morphia.annotations.Entity;
import edu.illinois.ncsa.incore.service.hazard.models.tornado.types.EFBox;

import java.util.LinkedList;
import java.util.List;

@Entity("TornadoModel")
public class TornadoModel extends Tornado {
    private String tornadoModel;

    private TornadoParameters tornadoParameters;
    private List<Double> tornadoWidth;
    private List<EFBox> efBoxes;

    private List<TornadoHazardDataset> hazardDatasets;

    public TornadoModel(){
        this.hazardDatasets = new LinkedList<>();
    }

    public List<TornadoHazardDataset> getHazardDatasets() {
        return hazardDatasets;
    }

    public void setHazardDatasets(List<TornadoHazardDataset> hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public void addTornadoHazardDataset(TornadoHazardDataset hazardDataset) { this.hazardDatasets.add(hazardDataset); }

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

}
