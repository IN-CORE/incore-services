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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;

@XmlRootElement
@Embedded
public class FileDescriptor {
    /**
     * Used for serialization of object
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this bean, used by persistence layer
     */
    @Property("_id")
    private ObjectId id = new ObjectId();

    /**
     * Should the bean be assumed to be deleted and not be returned
     */
    private boolean deleted = false;

    /**
     * Return the id of the bean.
     *
     * @return id of the bean
     */
    public final String getId() {
        return id.toString();
    }

    /**
     * Sets the id of the bean. This has to be a unique id since it is used as
     * the key in the database.
     *
     * @param id the id of the object.
     */
    public void setId(String id) {
        this.id = new ObjectId(id);
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

    /**
     * Original filename
     */
    private String filename = null;

    /**
     * Mime type of the dataset data
     */
    private String mimeType = "";

    /**
     * size of the blob associated
     */
    private long size = -1;

    /**
     * url where the actual data is stored
     */
    private String dataURL;

    /**
     * md5 sum of the actual data
     */
    private String md5sum;

    public FileDescriptor() {
    }

    /**
     * Return the mime type of the artifact.
     *
     * @return mime type of the artifact
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets the mime type of the artifact
     *
     * @param mimetype sets the mime type of the artifact
     */
    public void setMimeType(String mimetype) {
        this.mimeType = mimetype;
    }

    /**
     * Returns the size of the blob, this is -2 if there is no blob associated
     * or the size has not been computed.
     *
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the size of the blob, this can be set to -2 if there is no blob
     * associated with this dataset.
     *
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * If this dataset originated in a filesystem, or is intended to be stored
     * in a filesystem, what filename is/should be used? If not, set to null.
     *
     * @param filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * If this dataset originated in a filesystem, or is intended to be stored
     * in a filesystem, what filename is/should be used? If not, will return
     * null.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the dataURL
     */
    public String getDataURL() {
        return dataURL;
    }

    /**
     * @param dataURL the dataURL to set
     */
    public void setDataURL(String dataURL) {
        this.dataURL = dataURL;
    }

    /**
     * @return the md5sum
     */
    public String getMd5sum() {
        return md5sum;
    }

    /**
     * Returns the md5sum as a string of length 32.
     *
     * @return the md5sum as a 32 char string.
     */
    @JsonIgnore
    public BigInteger getMd5sumAsBigInteger() {
        return new BigInteger(md5sum, 16);
    }

    @JsonIgnore
    public byte[] getMd5sumAsBytes() {
        return new BigInteger(md5sum, 16).toByteArray();
    }

    /**
     * @param md5sum the md5sum to set
     */
    @JsonProperty("md5sum")
    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    /**
     * @param md5sum the md5sum to set
     */
    @JsonIgnore
    public void setMd5sum(byte[] md5sum) {
        setMd5sum(new BigInteger(1, md5sum));
    }

    /**
     * @param md5sum the md5sum to set
     */
    @JsonIgnore
    public void setMd5sum(BigInteger md5sum) {
        if (md5sum.signum() < 0) {
            md5sum = new BigInteger(1, md5sum.toByteArray());
        }
        this.md5sum = md5sum.toString(16);
        if (this.md5sum.length() < 16) {
            this.md5sum = "00000000000000000000000000000000".substring(this.md5sum.length()) + this.md5sum;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getFilename();
    }
}
