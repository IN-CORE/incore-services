package edu.illinois.ncsa.incore.service.project.models;

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

        public static Role fromString(String value) {
            if (value == null) return NONE;
            for (Role role : Role.values()) {
                if (value.equalsIgnoreCase(role.value)) {
                    return role;
                }
            }
            throw new IllegalArgumentException("Unknown role value: " + value);
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
}
