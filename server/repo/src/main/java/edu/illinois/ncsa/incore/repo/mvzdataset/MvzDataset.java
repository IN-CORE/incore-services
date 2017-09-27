package edu.illinois.ncsa.incore.repo.mvzdataset;

import edu.illinois.ncsa.incore.repo.mvzdataset.datasetobjects.DatasetId;
import edu.illinois.ncsa.incore.repo.mvzdataset.datasetobjects.MaevizMapping;
import edu.illinois.ncsa.incore.repo.mvzdataset.datasetobjects.Metadata;
import edu.illinois.ncsa.incore.repo.mvzdataset.datasetobjects.Property;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ywkim on 7/10/2017.
 */
@XmlRootElement
public class MvzDataset {
    public String datasetPropertyName;
    public String name;
    public String version;
    public String dataFormat;
    public String typeId;
    public String featuretypeName;
    public String convertedFeatureTypeName;
    public String geometryType;
    public DatasetId datasetId = new DatasetId();
    public MaevizMapping maevizMapping = new MaevizMapping();
    public List<URI> replicaLocations = new LinkedList<URI>();
    public List<Property> properties = new LinkedList<Property>();
    public Metadata metadata;

    @XmlAttribute
    public String getDatasetPropertyName() { return datasetPropertyName; }
    public void setDatasetPropertyName(String datasetPropertyName) { this.datasetPropertyName = datasetPropertyName; }

    @XmlAttribute
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    @XmlAttribute
    public String getDataFormat(){
        return dataFormat;
    }
    public void setDataFormat(String dataFormat){
        this.dataFormat = dataFormat;
    }

    @XmlAttribute
    public String getTypeId(){
        return typeId;
    }
    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    @XmlAttribute
    public String getFeaturetypeName(){
        return featuretypeName;
    }
    public void setFeaturetypeName(String featuretypeName) {
        this.featuretypeName = featuretypeName;
    }

    @XmlAttribute
    public String getConvertedFeatureTypeName(){
        return convertedFeatureTypeName;
    }
    public void setConvertedFeatureTypeName(String convertedFeatureTypeName) {
        this.convertedFeatureTypeName = convertedFeatureTypeName;
    }

    @XmlAttribute
    public String getGeometryType(){
        return geometryType;
    }
    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    @XmlElement
    public DatasetId getDatasetId() {
        return datasetId;
    }
    public void setDatasetId(DatasetId datasetId) {
        this.datasetId = datasetId;
    }

    @XmlElement
    public List<URI> getReplicaLocations() {
        return replicaLocations;
    }
    public void setReplicaLocations(List<URI> replicaLocations) {
        this.replicaLocations = replicaLocations;
    }

    @XmlElement
    public List<Property> getProperties(){
        return properties;
    }
    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    @XmlElement
    public Metadata getMetadata(){
        return metadata;
    }
    public void setMetadata(Metadata metadata) { this.metadata = metadata; }
}
