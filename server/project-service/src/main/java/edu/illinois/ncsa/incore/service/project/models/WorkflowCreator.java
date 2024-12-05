package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.morphia.annotations.Embedded;

@Embedded
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowCreator {
    public String id;
    public boolean deleted;
    public String firstName;
    public String lastName;
    public String email;
}
