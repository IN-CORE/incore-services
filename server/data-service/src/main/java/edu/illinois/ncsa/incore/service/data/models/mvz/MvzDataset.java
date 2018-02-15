/*
 * ******************************************************************************
 *   Copyright (c) 2017 University of Illinois and others.  All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the BSD-3-Clause which accompanies this distribution,
 *   and is available at https://opensource.org/licenses/BSD-3-Clause
 *
 *   Contributors:
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *  ******************************************************************************
 */

package edu.illinois.ncsa.incore.service.data.models.mvz;

import edu.illinois.ncsa.incore.service.data.models.FileDescriptor;
import edu.illinois.ncsa.incore.service.data.models.mvz.DatasetId;
import edu.illinois.ncsa.incore.service.data.models.mvz.MaevizMapping;
import edu.illinois.ncsa.incore.service.data.models.mvz.Metadata;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ywkim on 7/10/2017.
 */
@XmlRootElement
public class MvzDataset {
    @Id
    @Property("_id")
    private ObjectId id = new ObjectId();

    private String datasetPropertyName;
    private String name;
    private String version;
    private String dataFormat;
    private String typeId;
    private String featuretypeName;
    private String convertedFeatureTypeName;
    private String geometryType;
    public DatasetId datasetId = new DatasetId();
    public MaevizMapping maevizMapping = new MaevizMapping();
    private List<URI> replicaLocations = new LinkedList<URI>();
    private List<Property> properties = new LinkedList<Property>();
    private Metadata metadata;
    private List<FileDescriptor> fileDescriptors = null;

    public String getId() {return id.toString();}
    public void setId(String id) { this.id = new ObjectId(id); }

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

    public String getFeaturetypeName(){
        return featuretypeName;
    }
    public void setFeaturetypeName(String featuretypeName) {
        this.featuretypeName = featuretypeName;
    }

    public String getConvertedFeatureTypeName(){
        return convertedFeatureTypeName;
    }
    public void setConvertedFeatureTypeName(String convertedFeatureTypeName) {
        this.convertedFeatureTypeName = convertedFeatureTypeName;
    }

    public String getGeometryType(){
        return geometryType;
    }
    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public DatasetId getDatasetId() {
        return datasetId;
    }
    public void setDatasetId(DatasetId datasetId) {
        this.datasetId = datasetId;
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

    public List<FileDescriptor> getFileDescriptors() {
        if (fileDescriptors == null) {
            fileDescriptors = new ArrayList<FileDescriptor>();
        }
        return fileDescriptors;
    }

    public void setFileDescriptors(List<FileDescriptor> fileDescriptors) {
        this.fileDescriptors = fileDescriptors;
    }

    public void addFileDescriptor(FileDescriptor fileDescriptor) {
        if (fileDescriptor != null) {
            getFileDescriptors().add(fileDescriptor);
        }
    }

    public void removeFileDescriptor(FileDescriptor fileDescriptor) {
        getFileDescriptors().remove(fileDescriptor);
    }
}
