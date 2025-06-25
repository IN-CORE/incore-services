package edu.illinois.ncsa.incore.service.project.models;
import java.util.List;

public class BldInventoryRequest {

    private String title;
    private String description;
    // TODO set to enum later
    private String hazardType;
    private List<String> fips_list;

    public BldInventoryRequest() {
    }

    public BldInventoryRequest(String title, String description, List<String> fips_list) {
        this.title = title;
        this.description = description;
        this.fips_list = fips_list;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String hazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getFips_list() {
        return fips_list;
    }

    public void setFips_list(List<String> fips_list) {
        this.fips_list = fips_list;
    }
}
