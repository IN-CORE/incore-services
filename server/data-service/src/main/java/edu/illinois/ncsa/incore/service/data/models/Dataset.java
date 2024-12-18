/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Chris Navarro (NCSA) - initial API and implementation
 *   Yong Wook Kim (NCSA) - initial API and implementation
 *******************************************************************************/
package edu.illinois.ncsa.incore.service.data.models;

/**
 * Created by ywkim on 9/26/2017.
 * This is from NCSA's DataWolf
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import edu.illinois.ncsa.incore.common.data.models.jackson.JsonDateSerializer;
import org.bson.types.ObjectId;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@XmlRootElement
@Entity("Dataset")
public class Dataset {
    /**
     * Used for serialization of object
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this bean, used by persistence layer
     */
    @Id
    @Property("_id")
    private ObjectId id;

    /**
     * Should the bean be assumed to be deleted and not be returned
     */
    private boolean deleted = false;

    /**
     * Title of the artifact
     */
    private String title = "";

    /**
     * Description of the artifact
     */
    private String description = "";

    /**
     * Date the artifact is created
     */
    private Date date = new Date();

    /**
     * creator of the artifact
     */
    private String creator = null;

    /**
     * owner of the artifact
     */
    private String owner = null;

    /**
     * spaces the object belongs to. Calculated at runtime.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<String> spaces;

    /**
     * List of contributors to the artifact.
     */
    private List<String> contributors = null;

    /**
     * all blobs associated with this dataset
     */
    private List<FileDescriptor> fileDescriptors = null;

    /**
     * type of the artifact
     */
    private String dataType = "";

    /**
     * stored url of the artifact
     */
    private String storedUrl = "";

    /**
     * format of the artifact
     */
    private String format = "";

    /**
     * source dataset of the artifact
     */
    private String sourceDataset = "";

    /**
     * Bounding box array
     */
    private double[] boundingBox = null;

    /**
     * NetworkDataset networkDataset
     */
    private NetworkDataset networkDataset = null;

    public Dataset() {
    }

    /**
     * Return the title of the artifact.
     *
     * @return title of the artifact
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the artifact
     *
     * @param title sets the title of the artifact.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Return the description of the artifact.
     *
     * @return description of the artifact
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the artifact
     *
     * @param description sets the description of the artifact
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Return the date when the artifact was created.
     *
     * @return date the artifact was created.
     */
    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDate() {
        return date;
    }

    /**
     * Sets the date when the artifact was created.
     *
     * @param date sets the date when the artifact was created.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Return the creator of the artifact
     *
     * @return creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Sets the creator of the artifact.
     *
     * @param creator sets the creator of the artifact.
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * Return the PersonBean that is the owner of the artifact
     *
     * @return PersonBean that represents the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the PersonBean that represents the owner of the artifact.
     *
     * @param owner sets the PersonBeans that represents the owner of the
     *                artifact.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
    }

    /**
     * Return the set of PersonBeans that represents those that are contributors
     * to the artifact.
     *
     * @return set of PersonBeans that represents all the users that have
     * contributes to the artifact.
     */
    public List<String> getContributors() {
        if (contributors == null) {
            contributors = new ArrayList<String>();
        }
        return contributors;
    }

    /**
     * Set the set of PersonBeans that represents those that contributed to the
     * artifact.
     *
     * @param contributors the set of contributors to the artifact.
     */
    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    /**
     * Add the contributor to the set of contributors to the artifact.
     *
     * @param contributor the PersonBean of the contributor to be added.
     */
    public void addContributor(String contributor) {
        if (contributor != null) {
            getContributors().add(contributor);
        }
    }

    /**
     * Remove the contributor from the set of contributors to the dataset.
     *
     * @param contributor the PersonBean of the contributor to be removed.
     */
    public void removeContributor(Person contributor) {
        getContributors().remove(contributor);
    }

    /**
     * Return the set of file descriptors associated with the dataset.
     *
     * @return set of file descriptors associated with the dataset.
     */
    public List<FileDescriptor> getFileDescriptors() {
        if (fileDescriptors == null) {
            fileDescriptors = new ArrayList<FileDescriptor>();
        }
        return fileDescriptors;
    }


    /**
     * Set the set of file descriptors associated with the dataset.
     *
     * @param fileDescriptors the set of file descriptors to the dataset.
     */
    public void setFileDescriptors(List<FileDescriptor> fileDescriptors) {
        this.fileDescriptors = fileDescriptors;
        // getFileDescriptors().clear();
        // if (fileDescriptors != null) {
        // getFileDescriptors().addAll(fileDescriptors);
        // }
    }

    /**
     * get bounding box information
     *
     * @return
     */
    public double[] getBoundingBox() {
        return boundingBox;
    }

    /**
     * set bounding box information
     *
     * @param boundingBox
     */
    public void setBoundingBox(double[] boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * get network networkDataset information
     */
    public NetworkDataset getNetworkDataset() {
        return networkDataset;
    }

    /**
     * set network networkDataset information
     */
    public void setNetworkDataset(NetworkDataset networkDataset) {
        this.networkDataset = networkDataset;
    }

    /**
     * Return the string that is the type of the artifact
     *
     * @return type that represents the type of the artifact
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the string that represents the type of the artifact.
     *
     * @param dataType sets the string that represents the type of the
     *                 artifact.
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Return the string that is the format of the artifact
     *
     * @return format that represents the format of the artifact
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the string that represents the format of the artifact.
     *
     * @param format sets the string that represents the format of the
     *               artifact.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Return the string that is the source dataset of the artifact
     *
     * @return storedUrl that represents the url of the stored file of the artifact
     */
    public String getStoredUrl() {
        return storedUrl;
    }

    /**
     * Sets the string that represents the type of the artifact.
     *
     * @param storedUrl sets the string that represents the stored url of the
     *                  artifact.
     */
    public void setStoredUrl(String storedUrl) {
        this.storedUrl = storedUrl;
    }

    /**
     * Return the string that is the source dataset of the artifact
     *
     * @return source dataset that represents the source dataset of the artifact
     */
    public String getSourceDataset() {
        return sourceDataset;
    }

    /**
     * Sets the string that represents the type of the artifact.
     *
     * @param sourceDataset sets the string that represents the type of the
     *                      artifact.
     */
    public void setSourceDataset(String sourceDataset) {
        this.sourceDataset = sourceDataset;
    }

    /**
     * Add the file descriptor to the set of file descriptors to the dataset.
     *
     * @param fileDescriptor the file descriptors to be added.
     */
    public void addFileDescriptor(FileDescriptor fileDescriptor) {
        if (fileDescriptor != null) {
            getFileDescriptors().add(fileDescriptor);
        }
    }

    /**
     * Remove the file descriptor from the set of files of the dataset.
     *
     * @param fileDescriptor the file descriptor to be removed.
     */
    public void removeFileDescriptor(FileDescriptor fileDescriptor) {
        getFileDescriptors().remove(fileDescriptor);
    }

    public String toString() {
        return title;
    }

    /**
     * Return the id of the bean.
     *
     * @return id of the bean
     */
    public String getId() {
        return (id == null) ? null : id.toString();
    }

    /**
     * Should the bean be assumed to be deleted. Only a handfule rest api calls
     * right now will use this value.
     *
     * @return true if the bean is deleted, false otherwise.
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * Should the bean be assumed to be deleted. Only a handfule rest api calls
     * right now will use this value.
     *
     * @param deleted true if the bean is deleted, false otherwise.
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Compares two objects with each other. If the object is an AbstractBean it
     * will compare id's, otherwise it will return false.
     *
     * @param obj the object that should be compared to this AbstractBean
     * @return true if the two beans are the same (i.e. the id's are the same),
     * false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractBean) {
            return ((AbstractBean) obj).getId().equals(getId());
        }
        return false;
    }

    /**
     * Returns the hashcode of this object, which is the hashcode of the id.
     *
     * @return hashcode based on the id of the bean.
     */
    @Override
    public int hashCode() {
        if (getId() != null) {
            return getId().hashCode();
        } else {
            return super.hashCode();
        }
    }
}
