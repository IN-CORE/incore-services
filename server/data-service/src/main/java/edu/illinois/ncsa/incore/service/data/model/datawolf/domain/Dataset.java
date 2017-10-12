package edu.illinois.ncsa.incore.service.data.model.datawolf.domain;

/**
 * Created by ywkim on 9/26/2017.
 * This is from NCSA's DataWolf
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.illinois.ncsa.incore.service.data.model.datawolf.jackson.JsonDateSerializer;
import edu.illinois.ncsa.incore.service.data.model.datawolf.domain.FileDescriptor;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

public class Dataset extends AbstractBean {
    @Id
    @Property("_id")
    private ObjectId datasetId;

    /**
     * Used for serialization of object
     */
    private static final long serialVersionUID = 1L;

    /**
     * Title of the artifact
     */
    private String title = ""; //$NON-NLS-1$

    /**
     * Description of the artifact
     */
    private String description = ""; //$NON-NLS-1$

    /**
     * Date the artifact is created
     */
    private Date date = new Date();

    /**
     * creator of the artifact
     */
    private Person creator = null;

    /**
     * List of contributors to the artifact.
     */
    private List<Person> contributors = null;

    /**
     * all blobs associated with this dataset
     */
    private List<FileDescriptor> fileDescriptors = null;

    /**
     * type of the artifact
     */
    private String type = "";

    /**
     * stored url of the artifact
     */
    private String storedUrl = "";

    /**
     * format of the artifact
     */
    private String format = "";  //$NON-NLS-1$

    /**
     * source dataset of the artifact
     */
    private String sourceDataset = "";  //$NON-NLS-1$

    /**
     * List of spaces to the artifact.
     */
    private List<String> spaces = null;

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
     * Return the PersonBean that is the creator of the artifact
     *
     * @return PersonBean that represents the creator
     */
    public Person getCreator() {
        return creator;
    }

    /**
     * Sets the PersonBean that represents the creator of the artifact.
     *
     * @param creator sets the PersonBeans that represents the creator of the
     *                artifact.
     */
    public void setCreator(Person creator) {
        this.creator = creator;
    }

    /**
     * Return the set of PersonBeans that represents those that are contributors
     * to the artifact.
     *
     * @return set of PersonBeans that represents all the users that have
     * contributes to the artifact.
     */
    public List<Person> getContributors() {
        if (contributors == null) {
            contributors = new ArrayList<Person>();
        }
        return contributors;
    }

    /**
     * Set the set of PersonBeans that represents those that contributed to the
     * artifact.
     *
     * @param contributors the set of contributors to the artifact.
     */
    public void setContributors(List<Person> contributors) {
        this.contributors = contributors;
    }

    /**
     * Add the contributor to the set of contributors to the artifact.
     *
     * @param contributor the PersonBean of the contributor to be added.
     */
    public void addContributor(Person contributor) {
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
     * Return the string that is the type of the artifact
     *
     * @return type that represents the type of the artifact
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the string that represents the type of the artifact.
     *
     * @param type sets the string that represents the type of the
     *             artifact.
     */
    public void setType(String type) {
        this.type = type;
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
     * Sets the string that represents the type of the artifact.
     *
     * @param storedUrl sets the string that represents the stored url of the
     *                      artifact.
     */
    public void setStoredUrl(String storedUrl) {
        this.storedUrl = storedUrl;
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
     * @param datasetId sets the string that represents the type of the
     *                      artifact.
     */
    public void setDatasetId(ObjectId datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * Return the string that is the source dataset of the artifact
     *
     * @return datasetId that represents the source dataset of the artifact
     */
    public ObjectId getDatasetId() {
        return datasetId;
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
     * Return the string that is the source dataset of the artifact
     *
     * @return source dataset that represents the source dataset of the artifact
     */
    public List<String> getSpaces() {
        return spaces;
    }

    /**
     * Sets the string that represents the type of the artifact.
     *
     * @param spaces sets the string that represents the type of the
     *               artifact.
     */
    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
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
}