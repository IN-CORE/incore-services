package edu.illinois.ncsa.incore.common.models;


import java.util.List;

public class DemandDefinition {
    public String demand_type;
    public List<String> demand_unit;
    public String description;

    public String getDemand_type() {
        return demand_type;
    }

    public void setDemand_type(String demand_type) {
        this.demand_type = demand_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public List<String> getDemand_unit() {
        return demand_unit;
    }

    public void setDemand_unit(List<String> demand_unit) {
        this.demand_unit = demand_unit;
    }
}
