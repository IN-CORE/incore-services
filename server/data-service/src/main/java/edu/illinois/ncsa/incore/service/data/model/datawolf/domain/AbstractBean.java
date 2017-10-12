package edu.illinois.ncsa.incore.service.data.model.datawolf.domain;

/**
 * Created by ywkim on 9/26/2017.
 * This is from NCSA's DataWolf
 */

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import java.io.Serializable;
import java.util.UUID;

//@Entity
//@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
//@JsonIgnoreProperties("@id")
public class AbstractBean implements Serializable {

    /**
     * Used for serialization of object
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier for this bean, used by persistence layer
     */
    @Id
    @Property("_id")
    private String id;

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
        return id;
    }

    /**
     * Sets the id of the bean. This has to be a unique id since it is used as
     * the key in the database.
     *
     * @param id the id of the object.
     */
    public void setId(String id) {
        this.id = id;
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
        if (getId()!= null) {
            return getId().hashCode();
        } else {
            return super.hashCode();
        }
    }
}