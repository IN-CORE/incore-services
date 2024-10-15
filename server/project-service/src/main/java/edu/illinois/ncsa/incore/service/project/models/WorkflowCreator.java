package edu.illinois.ncsa.incore.service.project.models;

import dev.morphia.annotations.Embedded;

@Embedded
public class WorkflowCreator {
    public String id;
    public boolean deleted;
    public String firstName;
    public String lastName;
    public String email;
}
