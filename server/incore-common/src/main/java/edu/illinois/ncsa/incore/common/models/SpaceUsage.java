package edu.illinois.ncsa.incore.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.morphia.annotations.Embedded;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.XmlRootElement;

@Embedded
public class SpaceUsage {
    private static final Logger log = Logger.getLogger(SpaceUsage.class);

//    @JsonProperty("datasets")
//    private int datasets;

    public int datasets;
    public int hazards;
    public int hazardDatasets;
    public int dfr3;
    public long datasetSize;
    public long hazardDatasetSize;

    public SpaceUsage() { }

    public static SpaceUsage fromJson(String usageJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(usageJson, SpaceUsage.class);
        } catch (Exception e) {
            log.error("Could not parse usage JSON. Returning Usage with zero values", e);
            return new SpaceUsage();
        }
    }

    public int getDatasets() {
        return this.datasets;
    }

    public void setDatasets(int datasets) {
        this.datasets = datasets;
    }

    public int getHazards() {
        return this.hazards;
    }

    public void setHazards(int hazards) {
        this.hazards = hazards;
    }

    public int getHazardDatasets() {
        return this.hazardDatasets;
    }

    public void setHazardDatasets(int hazardDatasets) {
        this.hazardDatasets = hazardDatasets;
    }

    public int getDfr3() {
        return this.dfr3;
    }

    public void setDfr3(int dfr3) {
        this.dfr3 = dfr3;
    }

    public long getDatasetSize() {
        return this.datasetSize;
    }

    public void setDatasetSize(int datasetSize) {
        this.datasetSize = datasetSize;
    }

    public long getHazardDatasetSize() {
        return this.hazardDatasetSize;
    }

    public void setHazardDatasetSize(int hazardDatasetSize) {
        this.hazardDatasetSize = hazardDatasetSize;
    }
}
