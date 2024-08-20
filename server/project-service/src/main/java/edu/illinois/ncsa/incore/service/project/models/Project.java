package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
@Entity("Project")
public class Project {

    @Id
    @Property("_id")
    private ObjectId id;

    private String name = "";
    private String description = "";
    private Date date = new Date();
    private String creator = null;
    private String owner = null;
    private String region = null;

    private List<HazardResource> hazards = new ArrayList<>();
    private List<DFR3MappingResource> dfr3Mappings = new ArrayList<>();
    private List<DatasetResource> datasets = new ArrayList<>();
    private List<WorkflowResource> workflows = new ArrayList<>();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> spaces;

    public Project() {
    }

    public Project(String name) {
        this.name = name;
    }

    // Getters and Setters

    public String getId() {
        return (id == null) ? null : id.toString();
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public List<HazardResource> getHazards() {
        return hazards;
    }

    public void setHazards(List<HazardResource> hazards) {
        this.hazards = hazards;
    }

    public List<DFR3MappingResource> getDfr3Mappings() {
        return dfr3Mappings;
    }

    public void setDfr3Mappings(List<DFR3MappingResource> dfr3Mappings) {
        this.dfr3Mappings = dfr3Mappings;
    }

    public List<DatasetResource> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetResource> datasets) {
        this.datasets = datasets;
    }

    public List<WorkflowResource> getWorkflows() {
        return workflows;
    }

    public void setWorkflows(List<WorkflowResource> workflows) {
        this.workflows = workflows;
    }

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
    }
    
    // Methods to append, delete, and update each resource

    public void addHazardResource(HazardResource hazard) {
        this.hazards.add(hazard);
    }

    public void deleteHazardResource(HazardResource hazard) {
        this.hazards.remove(hazard);
    }

    public void updateHazardResource(int index, HazardResource hazard) {
        this.hazards.set(index, hazard);
    }

    public void addDFR3MappingResource(DFR3MappingResource dfr3Mapping) {
        this.dfr3Mappings.add(dfr3Mapping);
    }

    public void deleteDFR3MappingResource(DFR3MappingResource dfr3Mapping) {
        this.dfr3Mappings.remove(dfr3Mapping);
    }

    public void updateDFR3MappingResource(int index, DFR3MappingResource dfr3Mapping) {
        this.dfr3Mappings.set(index, dfr3Mapping);
    }

    public void addDatasetResource(DatasetResource dataset) {
        this.datasets.add(dataset);
    }

    public void deleteDatasetResource(DatasetResource dataset) {
        this.datasets.remove(dataset);
    }

    public void updateDatasetResource(int index, DatasetResource dataset) {
        this.datasets.set(index, dataset);
    }

    public void addWorkflowResource(WorkflowResource workflow) {
        this.workflows.add(workflow);
    }

    public void deleteWorkflowResource(WorkflowResource workflow) {
        this.workflows.remove(workflow);
    }

    public void updateWorkflowResource(int index, WorkflowResource workflow) {
        this.workflows.set(index, workflow);
    }
}
