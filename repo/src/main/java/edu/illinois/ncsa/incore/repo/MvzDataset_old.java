package edu.illinois.ncsa.incore.repo;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ywkim on 7/10/2017.
 */
@XmlRootElement
public class MvzDataset_old {
    public String datasetPropertyName;
    public String name;
    public String version;
    public DatasetId datasetId = new DatasetId();
    public String dataFormat;
    public String typeId;
    public List<URI> replicaLocations = new ArrayList<URI>();
    public List<Property> properties = new LinkedList<Property>();
    public Metadata metadata;

    public String getDatasetPropertyName() { return datasetPropertyName; }
    public void setDatasetPropertyName(String datasetPropertyName) { this.datasetPropertyName = datasetPropertyName; }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public DatasetId getDatasetId() {
        return datasetId;
    }
    public void setDatasetId(DatasetId datasetId) {
        this.datasetId = datasetId;
    }

    public String getDataFormat(){
        return dataFormat;
    }
    public void setDataFormat(String dataFormat){
        this.dataFormat = dataFormat;
    }

    public String getTypeId(){
        return typeId;
    }
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public List<URI> getReplicaLocations() {
        return replicaLocations;
    }
    public void setReplicaLocations(List<URI> replicaLocations) {
        this.replicaLocations = replicaLocations;
    }

    public List<Property> getProperties(){
        return properties;
    }
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public Metadata getMetadata(){
        return metadata;
    }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
}
