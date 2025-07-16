package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Embedded;


@Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowMetadata {

    private String workflowId;
    private String executionId;
    private Role role;

    public enum Role {
        INPUT("input"),
        OUTPUT("output"),
        IO("i/o"),
        NONE(null);

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Role fromString(String value) {
            if (value == null) return NONE;
            switch (value.toLowerCase()) {
                case "input": return INPUT;
                case "output": return OUTPUT;
                case "i/o":
                case "io": return IO;
                default: throw new IllegalArgumentException("Unknown role: " + value);
            }
        }
    }

    public WorkflowMetadata() {}

    public WorkflowMetadata(String workflowId, String executionId, Role role) {
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.role = role;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public WorkflowMetadata.Role mergeRoles(WorkflowMetadata.Role existing, WorkflowMetadata.Role incoming) {
        if (existing == WorkflowMetadata.Role.INPUT && incoming == WorkflowMetadata.Role.OUTPUT ||
            existing == WorkflowMetadata.Role.OUTPUT && incoming == WorkflowMetadata.Role.INPUT ||
            existing == WorkflowMetadata.Role.IO) {
            return WorkflowMetadata.Role.IO;
        }

        return (existing == null || existing == WorkflowMetadata.Role.NONE) ? incoming : existing;
    }
}
