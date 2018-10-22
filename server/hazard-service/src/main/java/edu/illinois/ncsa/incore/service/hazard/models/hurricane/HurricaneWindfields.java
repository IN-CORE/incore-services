package edu.illinois.ncsa.incore.service.hazard.models.hurricane;

import edu.illinois.ncsa.incore.common.auth.Privileges;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class HurricaneWindfields {

    @Id
    @Property("_id")
    private ObjectId id;

    private Privileges privileges;

    private String name;
    private String description;

    private int gridResolution;
    public final String gridResolutionUnits = "km";
    private double rasterResolution;

    public final String rasterResolutionUnits = "km";
    private double transD;
    public final String transDUnits = "degrees";
    private String landfallLocation;
    private String modelUsed;
    private String coast;
    private int category;
    public final String velocityUnits = "kt";

    private int gridPoints;
    private String rfMethod = "circular";

    private List<String> times = new ArrayList();
    private List<HurricaneSimulationDataset> hazardDatasets = new ArrayList<>();

    public String getId() {
        return id.toString();
    }

    public Privileges getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Privileges privileges) {
        this.privileges = privileges;
    }


    public List<String> getTimes() {
        return times;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }

    public List<HurricaneSimulationDataset> getHazardDatasets() {
        return hazardDatasets;
    }

    public void setHazardDatasets(List<HurricaneSimulationDataset> hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public String getCoast() {
        return coast;
    }

    public void setCoast(String coast) {
        this.coast = coast;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getGridResolution() {
        return gridResolution;
    }

    public void setGridResolution(int gridResolution) {
        this.gridResolution = gridResolution;
    }

    public double getTransD() {
        return transD;
    }

    public void setTransD(double transD) {
        this.transD = transD;
    }

    public String getLandfallLocation() {
        return landfallLocation;
    }

    public void setLandfallLocation(String landfallLocation) {
        this.landfallLocation = landfallLocation;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public double getRasterResolution() {
        return rasterResolution;
    }

    public void setRasterResolution(double rasterResolution) {
        this.rasterResolution = rasterResolution;
    }

    public int getGridPoints() {
        return gridPoints;
    }

    public void setGridPoints(int gridPoints) {
        this.gridPoints = gridPoints;
    }

    public String getRfMethod() {
        return rfMethod;
    }

    public void setRfMethod(String rfMethod) {
        this.rfMethod = rfMethod;
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
}
