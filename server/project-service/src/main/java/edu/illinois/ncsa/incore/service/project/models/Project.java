package edu.illinois.ncsa.incore.service.project.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
@Entity("Project")
public class Project {

    @Id
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
    private List<VisualizationResource> visualizations = new ArrayList<>();

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

    @JsonSerialize(using = JsonDateSerializer.class)
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

    public List<VisualizationResource> getVisualizations() {
        return visualizations;
    }

    public VisualizationResource getVisualization(String id) {
        return visualizations.stream()
            .filter(visualization -> visualization.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public WorkflowResource getWorkflow(String id) {
        return workflows.stream()
            .filter(workflow -> workflow.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    public boolean finalizeWorkflow(String workflowId) {
        WorkflowResource workflow = getWorkflow(workflowId);
        if (workflow != null) {
            workflow.isFinalized = true;
            return true;
        }
        return false;
    }

    public void setVisualizations(List<VisualizationResource> visualizations) {
        this.visualizations = visualizations;
    }

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
    }

    // Methods to append, delete, and update each resource

    public void addHazardResource(HazardResource hazard) {
        boolean exists = hazards.stream()
            .anyMatch(existingHazard -> existingHazard.getId().equals(hazard.getId()));

        if (!exists) {
            this.hazards.add(hazard);
        }
    }

    public void deleteHazardResource(String hazardId) {
        int indexToRemove = -1;
        for (int i = 0; i < hazards.size(); i++) {
            if (hazardId.equals(hazards.get(i).getId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            hazards.remove(indexToRemove);
        }
    }

    public void addDFR3MappingResource(DFR3MappingResource dfr3Mapping) {
        boolean exists = dfr3Mappings.stream()
            .anyMatch(existingMapping -> existingMapping.getId().equals(dfr3Mapping.getId()));

        if (!exists) {
            this.dfr3Mappings.add(dfr3Mapping);
        }
    }

    public void deleteDFR3MappingResource(String dfr3mappingId) {
        int indexToRemove = -1;
        for (int i = 0; i < dfr3Mappings.size(); i++) {
            if (dfr3mappingId.equals(dfr3Mappings.get(i).getId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            dfr3Mappings.remove(indexToRemove);
        }
    }

    public void addDatasetResource(DatasetResource dataset) {
        boolean exists = datasets.stream()
            .anyMatch(existingDataset -> existingDataset.getId().equals(dataset.getId()));

        if (!exists) {
            this.datasets.add(dataset);
        }
    }

    public void deleteDatasetResource(String datasetId) {
        // Find the index of the DatasetResource with the matching ID
        int indexToRemove = -1;
        for (int i = 0; i < datasets.size(); i++) {
            if (datasetId.equals(datasets.get(i).getId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            datasets.remove(indexToRemove);
        }
    }

    public void addWorkflowResource(WorkflowResource workflow) {
        boolean exists = workflows.stream()
            .anyMatch(existingWorkflow -> existingWorkflow.getId().equals(workflow.getId()));

        if (!exists) {
            this.workflows.add(workflow);
        }
    }

    public void deleteWorkflowResource(String workflowId) {
        int indexToRemove = -1;
        for (int i = 0; i < workflows.size(); i++) {
            if (workflowId.equals(workflows.get(i).getId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            workflows.remove(indexToRemove);
        }
    }

    public void addVisualizationResource(VisualizationResource visualization) {
        boolean exists = visualizations.stream()
            .anyMatch(existingVisualizations -> existingVisualizations.getId().equals(visualization.getId()));

        if (!exists) {
            this.visualizations.add(visualization);
        }
    }

    public void deleteVisualizationResource(String visualizationId) {
        int indexToRemove = -1;
        for (int i = 0; i < visualizations.size(); i++) {
            if (visualizationId.equals(visualizations.get(i).getId())) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) {
            visualizations.remove(indexToRemove);
        }
    }

}
