package edu.illinois.ncsa.incore.service.maestro.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class Workflow {

    public String workflowId;
    public String workflowDescription;

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public void setWorkflowDescription(String workflowDescription) {
        this.workflowDescription = workflowDescription;
    }
}
