// this is for 2. Understand the situation
package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class SituationSubStep extends SubStep {
    // TODO here uses dataset model from data services
    // TODO need to think about if it's a good practice or not
    public List<Input> inputs;

    public List<Input> getDatasets() {
        return inputs;
    }

    public void setDatasets(List<Input> datasets) {
        this.inputs = inputs;
    }
}
