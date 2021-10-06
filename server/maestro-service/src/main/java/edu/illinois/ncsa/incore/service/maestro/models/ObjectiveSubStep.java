// this is for 3. Determine Goals & Objectives

package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

import java.util.List;

@Embedded
public class ObjectiveSubStep extends SubStep {
    public List<Hazard> hazards;
    public List<Input> inputs;
    public List<Workflow> workflows;
}
