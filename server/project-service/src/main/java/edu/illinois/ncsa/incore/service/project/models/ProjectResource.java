package edu.illinois.ncsa.incore.service.project.models;

import dev.morphia.annotations.Embedded;
import org.bson.types.ObjectId;


@Embedded
public class ProjectResource {

//    // TODO: Enum for status
//    public enum Status {
//        DELETED,
//        UNAUTHORIZED,
//        EXISTING,
//        UNKNOWN
//    }

//    // TODO: Live calculate the status
//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
//    private Status status;
//    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
//    private List<String> spaces;


    private String id = new ObjectId().toString();

    public ProjectResource() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date date = new Date();

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

//    public Status getStatus() {
//        return status;
//    }
//
//    public void setStatus(Status status) {
//        this.status = status;
//    }
//
//    public List<String> getSpaces() {
//        return spaces;
//    }
//
//    public void setSpaces(List<String> spaces) {
//        this.spaces = spaces;
//    }
}

