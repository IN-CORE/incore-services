package edu.illinois.ncsa.incore.repo.mvzdataset.datasetobjects;

/**
 * Created by ywkim on 7/31/2017.
 */
public class Property {
    public String name;
    public String value;
    public String type;
    public String category;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
}
